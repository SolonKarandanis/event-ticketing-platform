package com.etp.ticketservice.payments;

import com.etp.ticketservice.orders.OrderStatusEnum;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderRepository;
import com.etp.ticketservice.orders.TicketOrderService;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.etp.ticketservice.tickets.RefundStatusEnum;
import com.etp.ticketservice.tickets.TicketRepository;
import com.etp.ticketservice.tickets.TicketService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// Drives every refund that has been promised but not yet handed to Stripe: event-cancellation
// tickets (never attempted inline), single-ticket refunds whose first attempt failed or was
// interrupted, and whole-order refunds for payments that couldn't be fulfilled. Triggered by
// PendingRefundJob, same split as TicketOrderExpiryJob/TicketOrderExpirySweeper.
@Component
@RequiredArgsConstructor
@Slf4j
public class PendingRefundSweeper {

    // Leaves room for an in-flight inline attempt to finish, so the two don't collide on
    // the same idempotency key.
    private static final long MIN_AGE_MINUTES = 2;

    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final TicketService ticketService;
    private final TicketOrderService ticketOrderService;
    private final PaymentGatewayService paymentGatewayService;

    public void sweep() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(MIN_AGE_MINUTES);
        for (UUID ticketId :
                ticketRepository.findDomainIdsAwaitingRefund(RefundStatusEnum.PENDING, cutoff)) {
            try {
                ticketService.retryPendingRefund(ticketId);
            } catch (Exception ex) {
                log.error("Failed to retry refund for Ticket {}", ticketId, ex);
            }
        }

        for (UUID orderId :
                ticketOrderRepository.findDomainIdsByStatus(OrderStatusEnum.REFUND_PENDING)) {
            try {
                refundOrder(orderId);
            } catch (Exception ex) {
                log.error("Failed to refund TicketOrder {}", orderId, ex);
            }
        }
    }

    private void refundOrder(UUID orderId) {
        TicketOrder order =
                ticketOrderRepository
                        .findByDomainId(orderId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "TicketOrder " + orderId + " vanished before refund"));

        if (null == order.getProviderCheckoutSessionId()) {
            log.error("TicketOrder {} has no checkout session -- cannot refund", orderId);
            ticketOrderService.recordRefundOutcome(orderId, RefundStatusEnum.FAILED, null);
            return;
        }

        try {
            RefundResult result =
                    paymentGatewayService.refund(
                            RefundRequest.builder()
                                    .providerCheckoutSessionId(order.getProviderCheckoutSessionId())
                                    .idempotencyKey("order-refund-" + orderId)
                                    .build());
            ticketOrderService.recordRefundOutcome(
                    orderId,
                    RefundStatusEnum.fromProviderStatus(result.getStatus()),
                    result.getProviderRefundId());
        } catch (PaymentGatewayException ex) {
            log.warn("Refund attempt failed for TicketOrder {}", orderId, ex);
            ticketOrderService.recordRefundAttemptFailed(orderId);
        }
    }
}
