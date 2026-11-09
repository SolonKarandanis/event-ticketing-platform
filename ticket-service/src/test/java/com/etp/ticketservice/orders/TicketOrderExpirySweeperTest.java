package com.etp.ticketservice.orders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.payments.CheckoutSessionStatus;
import com.etp.ticketservice.payments.PaymentGatewayService;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketOrderExpirySweeperTest {

    @Mock private TicketOrderRepository ticketOrderRepository;
    @Mock private TicketOrderService ticketOrderService;
    @Mock private PaymentGatewayService paymentGatewayService;

    @InjectMocks private TicketOrderExpirySweeper sweeper;

    private void staleOrders(UUID... orderIds) {
        when(ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(
                        eq(OrderStatusEnum.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(orderIds));
    }

    private void orderWithSession(UUID orderId, String sessionId) {
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setProviderCheckoutSessionId(sessionId);
        when(ticketOrderRepository.findByDomainId(orderId)).thenReturn(Optional.of(order));
    }

    @Test
    void sweep_stripeSaysExpired_expiresOrder() {
        UUID orderId = UUID.randomUUID();
        staleOrders(orderId);
        orderWithSession(orderId, "cs_1");
        when(paymentGatewayService.getCheckoutSessionStatus("cs_1"))
                .thenReturn(CheckoutSessionStatus.EXPIRED);

        sweeper.sweep();

        verify(ticketOrderService).expireOrder(orderId);
    }

    @Test
    void sweep_stripeSaysPaid_completesInsteadOfExpiring() {
        UUID orderId = UUID.randomUUID();
        staleOrders(orderId);
        orderWithSession(orderId, "cs_1");
        when(paymentGatewayService.getCheckoutSessionStatus("cs_1"))
                .thenReturn(CheckoutSessionStatus.PAID);

        sweeper.sweep();

        verify(ticketOrderService).completeOrder(orderId, "cs_1");
        verify(ticketOrderService, never()).expireOrder(any());
    }

    @Test
    void sweep_sessionStillOpen_leavesOrderForNextPass() {
        UUID orderId = UUID.randomUUID();
        staleOrders(orderId);
        orderWithSession(orderId, "cs_1");
        when(paymentGatewayService.getCheckoutSessionStatus("cs_1"))
                .thenReturn(CheckoutSessionStatus.OPEN);

        sweeper.sweep();

        verify(ticketOrderService, never()).expireOrder(any());
        verify(ticketOrderService, never()).completeOrder(any(), any());
    }

    @Test
    void sweep_noSessionAttached_expiresWithoutAskingStripe() {
        UUID orderId = UUID.randomUUID();
        staleOrders(orderId);
        orderWithSession(orderId, null);

        sweeper.sweep();

        verify(ticketOrderService).expireOrder(orderId);
        verify(paymentGatewayService, never()).getCheckoutSessionStatus(any());
    }

    @Test
    void sweep_stripeUnreachable_doesNotExpireAndContinuesBatch() {
        UUID unreachable = UUID.randomUUID();
        UUID next = UUID.randomUUID();
        staleOrders(unreachable, next);
        orderWithSession(unreachable, "cs_1");
        orderWithSession(next, "cs_2");
        when(paymentGatewayService.getCheckoutSessionStatus("cs_1"))
                .thenThrow(new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR));
        when(paymentGatewayService.getCheckoutSessionStatus("cs_2"))
                .thenReturn(CheckoutSessionStatus.EXPIRED);

        sweeper.sweep();

        verify(ticketOrderService, never()).expireOrder(unreachable);
        verify(ticketOrderService).expireOrder(next);
    }

    @Test
    void sweep_oneFailureDoesNotStopTheRestOfTheBatch() {
        UUID orderA = UUID.randomUUID();
        UUID orderB = UUID.randomUUID();
        staleOrders(orderA, orderB);
        orderWithSession(orderA, null);
        orderWithSession(orderB, null);
        doThrow(new RuntimeException("db blip")).when(ticketOrderService).expireOrder(orderA);

        sweeper.sweep();

        verify(ticketOrderService).expireOrder(orderB);
    }
}
