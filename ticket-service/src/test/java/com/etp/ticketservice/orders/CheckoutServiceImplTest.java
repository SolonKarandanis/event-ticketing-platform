package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CheckoutLineItemRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutResponseDto;
import com.etp.ticketservice.payments.CheckoutSessionRequest;
import com.etp.ticketservice.payments.CheckoutSessionResult;
import com.etp.ticketservice.payments.PaymentGatewayService;
import com.etp.ticketservice.tickettypes.TicketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit test for the orchestration itself -- see CheckoutServiceImpl's own
// class comment for why reserve()/the Stripe call/attachProviderCheckoutSession() are
// three separate steps rather than one @Transactional method. This pins down that
// sequencing and the TicketOrderItem -> CheckoutLineItem translation, not the reservation
// logic itself (see TicketOrderServiceImplTest) or the real Stripe call (see
// StripePaymentGatewayService, which this test mocks out entirely).
//
// Constructed directly, not via @InjectMocks -- successUrl/cancelUrlBase are
// constructor-injected Strings (same @Value-via-constructor convention as
// EventImageServiceImpl), which @InjectMocks has no mock to satisfy.
@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    private static final String SUCCESS_URL = "https://app.test/success";
    private static final String CANCEL_URL_BASE = "https://app.test/browse/";

    @Mock
    private TicketOrderService ticketOrderService;
    @Mock
    private PaymentGatewayService paymentGatewayService;

    private CheckoutServiceImpl checkoutService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        checkoutService = new CheckoutServiceImpl(ticketOrderService, paymentGatewayService, SUCCESS_URL, CANCEL_URL_BASE);
    }

    @Test
    void createCheckout_reservesThenCallsGatewayThenAttachesSession() {
        CreateCheckoutRequestDto request = new CreateCheckoutRequestDto(
                List.of(new CheckoutLineItemRequestDto(UUID.randomUUID(), 2)));

        TicketOrder reserved = new TicketOrder();
        reserved.setDomainId(UUID.randomUUID());
        TicketType ticketType = new TicketType();
        ticketType.setName("General");
        TicketOrderItem item = new TicketOrderItem();
        item.setTicketType(ticketType);
        item.setQuantity(2);
        item.setUnitPriceAtCheckout(15.0);
        reserved.addItem(item);
        when(ticketOrderService.reserve(USER_ID, EVENT_ID, request)).thenReturn(reserved);

        CheckoutSessionResult gatewayResult = new CheckoutSessionResult("cs_test_123", "https://checkout.stripe.com/test");
        when(paymentGatewayService.createCheckoutSession(any(CheckoutSessionRequest.class))).thenReturn(gatewayResult);

        TicketOrder attached = new TicketOrder();
        attached.setDomainId(reserved.getDomainId());
        attached.setProviderCheckoutSessionId("cs_test_123");
        attached.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        when(ticketOrderService.attachProviderCheckoutSession(reserved.getDomainId(), "cs_test_123")).thenReturn(attached);

        CreateCheckoutResponseDto response = checkoutService.createCheckout(USER_ID, EVENT_ID, request);

        assertThat(response.getOrderId()).isEqualTo(reserved.getDomainId());
        assertThat(response.getCheckoutUrl()).isEqualTo("https://checkout.stripe.com/test");
        assertThat(response.getExpiresAt()).isEqualTo(attached.getExpiresAt());

        ArgumentCaptor<CheckoutSessionRequest> captor = ArgumentCaptor.forClass(CheckoutSessionRequest.class);
        verify(paymentGatewayService).createCheckoutSession(captor.capture());
        CheckoutSessionRequest sessionRequest = captor.getValue();
        assertThat(sessionRequest.getOrderDomainId()).isEqualTo(reserved.getDomainId());
        assertThat(sessionRequest.getSuccessUrl()).isEqualTo(SUCCESS_URL);
        assertThat(sessionRequest.getCancelUrl()).isEqualTo(CANCEL_URL_BASE + EVENT_ID);
        assertThat(sessionRequest.getLineItems()).hasSize(1);
        assertThat(sessionRequest.getLineItems().get(0).getName()).isEqualTo("General");
        assertThat(sessionRequest.getLineItems().get(0).getUnitAmount()).isEqualTo(15.0);
        assertThat(sessionRequest.getLineItems().get(0).getQuantity()).isEqualTo(2);
    }
}
