package com.etp.ticketservice.orders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOrderExpirySweeperTest {

    @Mock
    private TicketOrderRepository ticketOrderRepository;
    @Mock
    private TicketOrderService ticketOrderService;

    @InjectMocks
    private TicketOrderExpirySweeper sweeper;

    @Test
    void sweep_expiresEveryCandidateOrder() {
        UUID orderA = UUID.randomUUID();
        UUID orderB = UUID.randomUUID();
        when(ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(eq(OrderStatusEnum.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(orderA, orderB));

        sweeper.sweep();

        verify(ticketOrderService).expireOrder(orderA);
        verify(ticketOrderService).expireOrder(orderB);
    }

    @Test
    void sweep_oneFailureDoesNotStopTheRestOfTheBatch() {
        UUID orderA = UUID.randomUUID();
        UUID orderB = UUID.randomUUID();
        when(ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(eq(OrderStatusEnum.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(orderA, orderB));
        doThrow(new RuntimeException("db blip")).when(ticketOrderService).expireOrder(orderA);

        sweeper.sweep();

        verify(ticketOrderService).expireOrder(orderA);
        verify(ticketOrderService).expireOrder(orderB);
    }
}
