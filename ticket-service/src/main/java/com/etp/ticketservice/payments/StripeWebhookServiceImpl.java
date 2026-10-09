package com.etp.ticketservice.payments;

import com.etp.ticketservice.orders.TicketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Deliberately NOT @Transactional -- orchestrates recordIfNew/completeOrder/expireOrder,
// each its own short transaction on a separate bean, same reasoning as
// CheckoutServiceImpl orchestrating TicketOrderServiceImpl's reserve/
// attachProviderCheckoutSession around the Stripe call in #20.
@Service
@RequiredArgsConstructor
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    private static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";

    private final PaymentGatewayService paymentGatewayService;
    private final ProcessedWebhookEventService processedWebhookEventService;
    private final TicketOrderService ticketOrderService;

    @Override
    public void handle(String payload, String signatureHeader) {
        WebhookEvent event = paymentGatewayService.verifyWebhookSignature(payload, signatureHeader);

        boolean isNew = processedWebhookEventService.recordIfNew(
                PaymentProvider.STRIPE, event.getProviderEventId(), event.getEventType());
        if (!isNew) {
            return; // already processed, or a concurrent delivery is processing it right now
        }

        if (null == event.getOrderDomainId()) {
            return; // a type we don't act on, or an unparseable/missing client_reference_id
        }

        switch (event.getEventType()) {
            case CHECKOUT_SESSION_COMPLETED -> ticketOrderService.completeOrder(event.getOrderDomainId());
            case CHECKOUT_SESSION_EXPIRED -> ticketOrderService.expireOrder(event.getOrderDomainId());
            default -> { /* acknowledged, ignored */ }
        }
    }
}
