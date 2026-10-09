package com.etp.ticketservice.orders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// No interface -- infrastructure glue nothing else needs to mock/replace, same
// precedent as RabbitMqTicketEventListener.
@Component
@RequiredArgsConstructor
@Slf4j
public class TicketOrderExpirySweeper {

    private static final long SWEEP_INTERVAL_MS = 300_000; // 5 minutes

    private final TicketOrderRepository ticketOrderRepository;
    private final TicketOrderService ticketOrderService;

    @Scheduled(fixedDelay = SWEEP_INTERVAL_MS)
    public void sweep() {
        List<UUID> expiredOrderIds = ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(
                OrderStatusEnum.PENDING, LocalDateTime.now());

        // Per-item try/catch -- an unattended batch job with no caller waiting on a
        // result, so one row's unexpected failure must not block the rest of the batch
        // from expiring on schedule. Each expireOrder call is its own short
        // transaction/lock, same as any other cross-bean call through this interface.
        for (UUID orderId : expiredOrderIds) {
            try {
                ticketOrderService.expireOrder(orderId);
            } catch (Exception ex) {
                log.error("Failed to expire TicketOrder {}", orderId, ex);
            }
        }
    }
}
