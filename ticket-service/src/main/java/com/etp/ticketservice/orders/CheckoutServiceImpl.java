package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutResponseDto;
import com.etp.ticketservice.payments.CheckoutLineItem;
import com.etp.ticketservice.payments.CheckoutSessionRequest;
import com.etp.ticketservice.payments.CheckoutSessionResult;
import com.etp.ticketservice.payments.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Deliberately NOT @Transactional -- this orchestrates two short, independent
// TicketOrderService transactions around one external Stripe HTTP call, so that call
// never runs with a DB lock held. See TicketOrderServiceImpl for the transaction split
// itself; don't "fix" this by adding @Transactional back here.
@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final TicketOrderService ticketOrderService;
    private final PaymentGatewayService paymentGatewayService;
    private final String successUrl;
    private final String cancelUrlBase;

    public CheckoutServiceImpl(
            TicketOrderService ticketOrderService,
            PaymentGatewayService paymentGatewayService,
            @Value("${app.checkout.success-url}") String successUrl,
            @Value("${app.checkout.cancel-url-base}") String cancelUrlBase) {
        this.ticketOrderService = ticketOrderService;
        this.paymentGatewayService = paymentGatewayService;
        this.successUrl = successUrl;
        this.cancelUrlBase = cancelUrlBase;
    }

    @Override
    public CreateCheckoutResponseDto createCheckout(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request) {
        TicketOrder order = ticketOrderService.reserve(userId, eventDomainId, request);

        // Safe to read these relationships after reserve()'s transaction has committed --
        // purchaser/event/each item's ticketType were all set from already-loaded entity
        // instances during that transaction, never from an unresolved lazy proxy, and
        // scalar columns load eagerly with their row regardless of which getter is
        // called. Same reasoning EventController#createEvent already relies on.
        List<CheckoutLineItem> lineItems = order.getItems().stream()
                .map(item -> CheckoutLineItem.builder()
                        .name(item.getTicketType().getName())
                        .unitAmount(item.getUnitPriceAtCheckoutMinorUnits())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        CheckoutSessionRequest sessionRequest = CheckoutSessionRequest.builder()
                .orderDomainId(order.getDomainId())
                .successUrl(successUrl)
                .cancelUrl(cancelUrlBase + eventDomainId)
                .lineItems(lineItems)
                .build();

        CheckoutSessionResult result = paymentGatewayService.createCheckoutSession(sessionRequest);

        TicketOrder updated = ticketOrderService.attachProviderCheckoutSession(order.getDomainId(), result.getProviderSessionId());

        return new CreateCheckoutResponseDto(updated.getDomainId(), result.getRedirectUrl(), updated.getExpiresAt());
    }
}
