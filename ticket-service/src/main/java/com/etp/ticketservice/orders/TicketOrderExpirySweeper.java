package com.etp.ticketservice.orders;

import com.etp.ticketservice.payments.PaymentGatewayService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// No interface -- infrastructure glue nothing else needs to mock/replace, same
// precedent as RabbitMqTicketEventListener. Triggered by Quartz's TicketOrderExpiryJob
// (see com.etp.ticketservice.config.QuartzConfiguration) rather than @Scheduled directly,
// so this class stays a plain constructor-injected Spring bean with its existing tests
// unaffected by Quartz's own instantiation/DI requirements for Job classes.
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketOrderExpirySweeper {

    private final TicketOrderRepository ticketOrderRepository;
    private final TicketOrderService ticketOrderService;
    private final PaymentGatewayService paymentGatewayService;

    public void sweep() {
        List<UUID> expiredOrderIds =
                ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(
                        OrderStatusEnum.PENDING, LocalDateTime.now());

        // Per-item try/catch -- an unattended batch job with no caller waiting on a
        // result, so one row's unexpected failure must not block the rest of the batch
        // from expiring on schedule. Each order is settled in its own short
        // transaction/lock, same as any other cross-bean call through this interface.
        for (UUID orderId : expiredOrderIds) {
            try {
                settle(orderId);
            } catch (Exception ex) {
                log.error("Failed to settle stale TicketOrder {}", orderId, ex);
            }
        }
    }

    // Stripe is the source of truth for whether money moved -- a late or lost webhook must
    // not turn a paid order into an expired one. If Stripe can't be reached the exception
    // leaves the order PENDING for the next pass.
    private void settle(UUID orderId) {
        String sessionId =
                ticketOrderRepository
                        .findByDomainId(orderId)
                        .map(TicketOrder::getProviderCheckoutSessionId)
                        .orElse(null);

        if (null == sessionId) {
            // Session creation never succeeded, so there's nothing to pay.
            ticketOrderService.expireOrder(orderId);
            return;
        }

        switch (paymentGatewayService.getCheckoutSessionStatus(sessionId)) {
            case PAID -> ticketOrderService.completeOrder(orderId, sessionId);
            case EXPIRED -> ticketOrderService.expireOrder(orderId);
            case OPEN -> {
                /* still payable -- check again next pass */
            }
        }
    }
}
