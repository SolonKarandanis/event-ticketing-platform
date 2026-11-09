package com.etp.ticketservice.payments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.orders.OrderStatusEnum;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderRepository;
import com.etp.ticketservice.orders.TicketOrderService;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.etp.ticketservice.tickets.RefundStatusEnum;
import com.etp.ticketservice.tickets.TicketRepository;
import com.etp.ticketservice.tickets.TicketService;
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
class PendingRefundSweeperTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketOrderRepository ticketOrderRepository;
    @Mock private TicketService ticketService;
    @Mock private TicketOrderService ticketOrderService;
    @Mock private PaymentGatewayService paymentGatewayService;

    @InjectMocks private PendingRefundSweeper sweeper;

    private void ticketsAwaiting(UUID... ticketIds) {
        when(ticketRepository.findDomainIdsAwaitingRefund(
                        eq(RefundStatusEnum.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(ticketIds));
    }

    private void orderAwaiting(UUID orderId, String sessionId) {
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setProviderCheckoutSessionId(sessionId);
        when(ticketOrderRepository.findDomainIdsByStatus(OrderStatusEnum.REFUND_PENDING))
                .thenReturn(List.of(orderId));
        when(ticketOrderRepository.findByDomainId(orderId)).thenReturn(Optional.of(order));
    }

    @Test
    void sweep_oneTicketFailureDoesNotStopTheRest() {
        UUID ticketA = UUID.randomUUID();
        UUID ticketB = UUID.randomUUID();
        ticketsAwaiting(ticketA, ticketB);
        doThrow(new RuntimeException("db blip")).when(ticketService).retryPendingRefund(ticketA);

        sweeper.sweep();

        verify(ticketService).retryPendingRefund(ticketB);
    }

    @Test
    void sweep_order_refundsWholePaymentWithStableKey() {
        UUID orderId = UUID.randomUUID();
        ticketsAwaiting();
        orderAwaiting(orderId, "cs_1");
        when(paymentGatewayService.refund(any()))
                .thenReturn(
                        RefundResult.builder()
                                .providerRefundId("re_1")
                                .status("succeeded")
                                .build());

        sweeper.sweep();

        verify(paymentGatewayService)
                .refund(
                        argThat(r ->
                                "cs_1".equals(r.getProviderCheckoutSessionId())
                                        && null == r.getAmountMinorUnits()
                                        && ("order-refund-" + orderId)
                                                .equals(r.getIdempotencyKey())));
        verify(ticketOrderService).recordRefundOutcome(orderId, RefundStatusEnum.SUCCEEDED, "re_1");
    }

    @Test
    void sweep_order_gatewayFailureCountsAnAttempt() {
        UUID orderId = UUID.randomUUID();
        ticketsAwaiting();
        orderAwaiting(orderId, "cs_1");
        when(paymentGatewayService.refund(any()))
                .thenThrow(new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR));

        sweeper.sweep();

        verify(ticketOrderService).recordRefundAttemptFailed(orderId);
        verify(ticketOrderService, never()).recordRefundOutcome(any(), any(), any());
    }

    @Test
    void sweep_order_withoutSessionIsMarkedFailedForFollowUp() {
        UUID orderId = UUID.randomUUID();
        ticketsAwaiting();
        orderAwaiting(orderId, null);

        sweeper.sweep();

        verify(paymentGatewayService, never()).refund(any());
        verify(ticketOrderService).recordRefundOutcome(orderId, RefundStatusEnum.FAILED, null);
    }
}
