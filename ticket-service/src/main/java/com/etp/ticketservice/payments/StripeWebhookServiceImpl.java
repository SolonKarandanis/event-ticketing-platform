package com.etp.ticketservice.payments;

import com.etp.ticketservice.orders.TicketOrderService;
import com.etp.ticketservice.tickets.RefundStatusEnum;
import com.etp.ticketservice.tickets.TicketCancellationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Deliberately NOT @Transactional -- orchestrates dispatch/recordIfNew, each its own short
// transaction on a separate bean, same reasoning as CheckoutServiceImpl orchestrating
// TicketOrderServiceImpl's reserve/attachProviderCheckoutSession around the Stripe call in
// #20.
@Service
@RequiredArgsConstructor
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    private static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";
    private static final String REFUND_UPDATED = "refund.updated";
    private static final String REFUND_FAILED = "refund.failed";
    private static final String CHARGE_REFUND_UPDATED = "charge.refund.updated";

    private final PaymentGatewayService paymentGatewayService;
    private final ProcessedWebhookEventService processedWebhookEventService;
    private final TicketOrderService ticketOrderService;
    private final TicketCancellationService ticketCancellationService;

    @Override
    public void handle(String payload, String signatureHeader) {
        WebhookEvent event = paymentGatewayService.verifyWebhookSignature(payload, signatureHeader);

        // Work first, ledger second. If the work fails, the exception turns into a non-2xx
        // and Stripe redelivers; recording first would make that retry look already handled
        // and drop it. Every dispatch target is safe to repeat (order status is re-checked
        // under lock), so the ledger is an audit trail, not the duplicate guard.
        dispatch(event);

        processedWebhookEventService.recordIfNew(
                PaymentProvider.STRIPE, event.getProviderEventId(), event.getEventType());
    }

    private void dispatch(WebhookEvent event) {
        switch (event.getEventType()) {
            case CHECKOUT_SESSION_COMPLETED -> {
                if (null != event.getOrderDomainId()) {
                    ticketOrderService.completeOrder(
                            event.getOrderDomainId(), event.getProviderSessionId());
                }
            }
            case CHECKOUT_SESSION_EXPIRED -> {
                if (null != event.getOrderDomainId()) {
                    ticketOrderService.expireOrder(event.getOrderDomainId());
                }
            }
            case REFUND_UPDATED, REFUND_FAILED, CHARGE_REFUND_UPDATED -> {
                if (null != event.getProviderRefundId()) {
                    ticketCancellationService.applyProviderRefundUpdate(
                            event.getProviderRefundId(),
                            RefundStatusEnum.fromProviderStatus(event.getProviderRefundStatus()));
                }
            }
            default -> {
                /* acknowledged, ignored */
            }
        }
    }
}
