package com.etp.ticketservice.payments;

import com.etp.ticketservice.orders.TicketOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceImplTest {

    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private ProcessedWebhookEventService processedWebhookEventService;
    @Mock
    private TicketOrderService ticketOrderService;

    @InjectMocks
    private StripeWebhookServiceImpl stripeWebhookService;

    private static final String PAYLOAD = "{}";
    private static final String SIGNATURE = "t=123,v1=fake";

    @Test
    void handle_checkoutSessionCompleted_completesOrder() {
        UUID orderId = UUID.randomUUID();
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(
                WebhookEvent.builder().providerEventId("evt_1").eventType("checkout.session.completed").orderDomainId(orderId).build());
        when(processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_1", "checkout.session.completed")).thenReturn(true);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService).completeOrder(orderId);
        verify(ticketOrderService, never()).expireOrder(any());
    }

    @Test
    void handle_checkoutSessionExpired_expiresOrder() {
        UUID orderId = UUID.randomUUID();
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(
                WebhookEvent.builder().providerEventId("evt_2").eventType("checkout.session.expired").orderDomainId(orderId).build());
        when(processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_2", "checkout.session.expired")).thenReturn(true);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService).expireOrder(orderId);
        verify(ticketOrderService, never()).completeOrder(any());
    }

    @Test
    void handle_unrecognizedEventType_doesNothing() {
        UUID orderId = UUID.randomUUID();
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(
                WebhookEvent.builder().providerEventId("evt_3").eventType("payment_intent.created").orderDomainId(orderId).build());
        when(processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_3", "payment_intent.created")).thenReturn(true);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService, never()).completeOrder(any());
        verify(ticketOrderService, never()).expireOrder(any());
    }

    @Test
    void handle_alreadyProcessed_shortCircuitsBeforeDispatch() {
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(
                WebhookEvent.builder().providerEventId("evt_4").eventType("checkout.session.completed").orderDomainId(UUID.randomUUID()).build());
        when(processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_4", "checkout.session.completed")).thenReturn(false);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService, never()).completeOrder(any());
        verify(ticketOrderService, never()).expireOrder(any());
    }

    @Test
    void handle_nullOrderDomainId_doesNothing() {
        when(paymentGatewayService.verifyWebhookSignature(PAYLOAD, SIGNATURE)).thenReturn(
                WebhookEvent.builder().providerEventId("evt_5").eventType("checkout.session.completed").orderDomainId(null).build());
        when(processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_5", "checkout.session.completed")).thenReturn(true);

        stripeWebhookService.handle(PAYLOAD, SIGNATURE);

        verify(ticketOrderService, never()).completeOrder(any());
        verify(ticketOrderService, never()).expireOrder(any());
    }
}
