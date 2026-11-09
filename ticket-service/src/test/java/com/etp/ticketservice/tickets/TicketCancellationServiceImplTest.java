package com.etp.ticketservice.tickets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.etp.ticketservice.messaging.TicketEventPublisher;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderItem;
import com.etp.ticketservice.payments.RefundRequest;
import com.etp.ticketservice.tickets.exception.TicketAlreadyCancelledException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Pure Mockito unit tests. This is the transactional layer that TicketServiceImpl (not
// @Transactional any more -- see its own class comment) delegates to: cancelAndPersist and
// recordRefundOutcome are the two independent commits either side of the Stripe refund call,
// each always re-fetching by domainId rather than trusting a caller-passed entity, same
// precedent as TicketOrderServiceImpl.
@ExtendWith(MockitoExtension.class)
class TicketCancellationServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketEventPublisher ticketEventPublisher;

    @InjectMocks private TicketCancellationServiceImpl ticketCancellationService;

    private static final UUID TICKET_ID = UUID.randomUUID();

    @Test
    void cancelAndPersist_vanishedTicket_throws() {
        when(ticketRepository.findByDomainIdWithLock(TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        ticketCancellationService.cancelAndPersist(
                                TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, "note"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelAndPersist_noOrderItem_setsCancelledFieldsAndReturnsNullRefundContext() {
        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        when(ticketRepository.findByDomainIdWithLock(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.findByDomainIdForCancellation(TICKET_ID))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketCancellationOutcome outcome =
                ticketCancellationService.cancelAndPersist(
                        TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, "Changed my mind");

        assertThat(ticket.getRefundStatus()).isNull();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
        assertThat(ticket.getCancelledAt()).isNotNull();
        assertThat(ticket.getCancelReason()).isEqualTo(TicketCancelReasonEnum.ATTENDEE_REQUEST);
        assertThat(ticket.getCancelNote()).isEqualTo("Changed my mind");
        verify(ticketEventPublisher).publishTicketCancelled(ticket);

        assertThat(outcome.getTicket()).isEqualTo(ticket);
        assertThat(outcome.getRefundAmountMinorUnits()).isNull();
        assertThat(outcome.getProviderCheckoutSessionId()).isNull();
    }

    @Test
    void cancelAndPersist_withOrderItem_resolvesRefundAmountAndProviderCheckoutSessionId() {
        TicketOrder ticketOrder = new TicketOrder();
        ticketOrder.setProviderCheckoutSessionId("cs_test_123");
        TicketOrderItem orderItem = new TicketOrderItem();
        orderItem.setTicketOrder(ticketOrder);
        orderItem.setUnitPriceAtCheckoutMinorUnits(1999L);

        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        ticket.setOrderItem(orderItem);
        when(ticketRepository.findByDomainIdWithLock(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.findByDomainIdForCancellation(TICKET_ID))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketCancellationOutcome outcome =
                ticketCancellationService.cancelAndPersist(
                        TICKET_ID, TicketCancelReasonEnum.ORGANIZER_ACTION, null);

        assertThat(outcome.getRefundAmountMinorUnits()).isEqualTo(1999L);
        assertThat(outcome.getProviderCheckoutSessionId()).isEqualTo("cs_test_123");
        assertThat(ticket.getRefundStatus()).isEqualTo(RefundStatusEnum.PENDING);
    }

    @Test
    void cancelAndPersist_alreadyCancelled_throwsSoASecondRefundCannotStart() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        when(ticketRepository.findByDomainIdWithLock(TICKET_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                        ticketCancellationService.cancelAndPersist(
                                TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, null))
                .isInstanceOf(TicketAlreadyCancelledException.class);

        verify(ticketRepository, never()).save(any());
        verify(ticketEventPublisher, never()).publishTicketCancelled(any());
    }

    @Test
    void cancelAndPersist_alreadyCancelledDuringEventCascade_isNoOpWithoutRefund() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        when(ticketRepository.findByDomainIdWithLock(TICKET_ID)).thenReturn(Optional.of(ticket));

        TicketCancellationOutcome outcome =
                ticketCancellationService.cancelAndPersist(
                        TICKET_ID, TicketCancelReasonEnum.EVENT_CANCELLED, null);

        assertThat(outcome.getRefundAmountMinorUnits()).isNull();
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void recordRefundAttemptFailed_staysPendingUntilAttemptsRunOut() {
        Ticket ticket = new Ticket();
        ticket.setRefundStatus(RefundStatusEnum.PENDING);
        ticket.setRefundAttempts(RefundRequest.MAX_ATTEMPTS - 2);
        when(ticketRepository.findByDomainId(TICKET_ID)).thenReturn(Optional.of(ticket));

        assertThat(ticketCancellationService.recordRefundAttemptFailed(TICKET_ID))
                .isEqualTo(RefundStatusEnum.PENDING);
        assertThat(ticketCancellationService.recordRefundAttemptFailed(TICKET_ID))
                .isEqualTo(RefundStatusEnum.FAILED);
    }

    @Test
    void applyProviderRefundUpdate_updatesMatchingTicket() {
        Ticket ticket = new Ticket();
        ticket.setRefundStatus(RefundStatusEnum.PENDING);
        when(ticketRepository.findByProviderRefundId("re_1")).thenReturn(Optional.of(ticket));

        ticketCancellationService.applyProviderRefundUpdate("re_1", RefundStatusEnum.SUCCEEDED);

        assertThat(ticket.getRefundStatus()).isEqualTo(RefundStatusEnum.SUCCEEDED);
    }

    @Test
    void applyProviderRefundUpdate_unknownRefund_isIgnored() {
        when(ticketRepository.findByProviderRefundId("re_x")).thenReturn(Optional.empty());

        ticketCancellationService.applyProviderRefundUpdate("re_x", RefundStatusEnum.FAILED);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void recordRefundOutcome_vanishedTicket_throws() {
        when(ticketRepository.findByDomainId(TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        ticketCancellationService.recordRefundOutcome(
                                TICKET_ID, RefundStatusEnum.SUCCEEDED, "re_test_123"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recordRefundOutcome_writesStatusAndProviderRefundId() {
        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        when(ticketRepository.findByDomainId(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketCancellationService.recordRefundOutcome(
                TICKET_ID, RefundStatusEnum.SUCCEEDED, "re_test_123");

        assertThat(ticket.getRefundStatus()).isEqualTo(RefundStatusEnum.SUCCEEDED);
        assertThat(ticket.getProviderRefundId()).isEqualTo("re_test_123");
        verify(ticketRepository).save(ticket);
    }
}
