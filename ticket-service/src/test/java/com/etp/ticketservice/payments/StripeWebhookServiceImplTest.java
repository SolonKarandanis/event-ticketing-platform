package com.etp.ticketservice.payments;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.etp.ticketservice.orders.TicketOrderService;
import com.etp.ticketservice.tickets.RefundStatusEnum;
import com.etp.ticketservice.tickets.TicketCancellationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceImplTest {

    @Mock private PaymentGatewayService paymentGatewayService;
    @Mock private ProcessedWebhookEventService processedWebhookEventService;
    @Mock private TicketOrderService ticketOrderService;
    @Mock private TicketCancellationService ticketCancellationService;

    @InjectMocks private StripeWebhookServiceImpl stripeWebhookService;

    private static final String PAYLOAD = "{}";
    private static final String SIGNATURE = "t=123,v1=fake";

    private void verifiedEvent(WebhookEvent event) {
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(event);
    }

    private WebhookEvent sessionEvent(String eventId, String type, UUID orderId) {
        return WebhookEvent.builder()
                .providerEventId(eventId)
                .eventType(type)
                .orderDomainId(orderId)
                .providerSessionId("cs_1")
                .build();
    }

    @Test
    void handle_checkoutSessionCompleted_completesOrderThenRecords() {
        UUID orderId = UUID.randomUUID();
        verifiedEvent(sessionEvent("evt_1", "checkout.session.completed", orderId));

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService).completeOrder(orderId, "cs_1");
        verify(ticketOrderService, never()).expireOrder(any());
        verify(processedWebhookEventService)
                .recordIfNew(PaymentProvider.STRIPE, "evt_1", "checkout.session.completed");
    }

    @Test
    void handle_completionFails_propagatesAndLeavesEventUnrecordedSoStripeCanRetry() {
        UUID orderId = UUID.randomUUID();
        verifiedEvent(sessionEvent("evt_1", "checkout.session.completed", orderId));
        doThrow(new RuntimeException("db blip"))
                .when(ticketOrderService)
                .completeOrder(orderId, "cs_1");

        assertThatThrownBy(() -> stripeWebhookService.handle(PAYLOAD, SIGNATURE))
                .isInstanceOf(RuntimeException.class);

        verify(processedWebhookEventService, never()).recordIfNew(any(), any(), any());
    }

    @Test
    void handle_redelivery_reprocessesBecauseCompletionIsIdempotent() {
        UUID orderId = UUID.randomUUID();
        verifiedEvent(sessionEvent("evt_4", "checkout.session.completed", orderId));
        when(processedWebhookEventService.recordIfNew(
                        PaymentProvider.STRIPE, "evt_4", "checkout.session.completed"))
                .thenReturn(false);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService).completeOrder(orderId, "cs_1");
    }

    @Test
    void handle_checkoutSessionExpired_expiresOrder() {
        UUID orderId = UUID.randomUUID();
        verifiedEvent(sessionEvent("evt_2", "checkout.session.expired", orderId));

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService).expireOrder(orderId);
        verify(ticketOrderService, never()).completeOrder(any(), any());
    }

    @Test
    void handle_refundFailed_updatesTicketRefundStatus() {
        verifiedEvent(
                WebhookEvent.builder()
                        .providerEventId("evt_6")
                        .eventType("refund.failed")
                        .providerRefundId("re_1")
                        .providerRefundStatus("failed")
                        .build());

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketCancellationService)
                .applyProviderRefundUpdate("re_1", RefundStatusEnum.FAILED);
    }

    @Test
    void handle_unrecognizedEventType_doesNothing() {
        verifiedEvent(sessionEvent("evt_3", "payment_intent.created", UUID.randomUUID()));

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService, never()).completeOrder(any(), any());
        verify(ticketOrderService, never()).expireOrder(any());
    }

    @Test
    void handle_nullOrderDomainId_doesNothing() {
        verifiedEvent(sessionEvent("evt_5", "checkout.session.completed", null));

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService, never()).completeOrder(any(), any());
        verify(ticketOrderService, never()).expireOrder(any());
    }
}
