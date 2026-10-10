package com.etp.ticketservice.tickets;

import com.etp.ticketservice.messaging.TicketEventPublisher;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests. This is the transactional layer that TicketServiceImpl (not
// @Transactional any more -- see its own class comment) delegates to: cancelAndPersist and
// recordRefundOutcome are the two independent commits either side of the Stripe refund call,
// each always re-fetching by domainId rather than trusting a caller-passed entity, same
// precedent as TicketOrderServiceImpl.
@ExtendWith(MockitoExtension.class)
class TicketCancellationServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketEventPublisher ticketEventPublisher;

    @InjectMocks
    private TicketCancellationServiceImpl ticketCancellationService;

    private static final UUID TICKET_ID = UUID.randomUUID();

    @Test
    void cancelAndPersist_vanishedTicket_throws() {
        when(ticketRepository.findByDomainIdForCancellation(TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketCancellationService.cancelAndPersist(
                TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, "note"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelAndPersist_noOrderItem_setsCancelledFieldsAndReturnsNullRefundContext() {
        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        when(ticketRepository.findByDomainIdForCancellation(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketCancellationOutcome outcome = ticketCancellationService.cancelAndPersist(
                TICKET_ID, TicketCancelReasonEnum.ATTENDEE_REQUEST, "Changed my mind");

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
        when(ticketRepository.findByDomainIdForCancellation(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketCancellationOutcome outcome = ticketCancellationService.cancelAndPersist(
                TICKET_ID, TicketCancelReasonEnum.ORGANIZER_ACTION, null);

        assertThat(outcome.getRefundAmountMinorUnits()).isEqualTo(1999L);
        assertThat(outcome.getProviderCheckoutSessionId()).isEqualTo("cs_test_123");
    }

    @Test
    void recordRefundOutcome_vanishedTicket_throws() {
        when(ticketRepository.findByDomainId(TICKET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketCancellationService.recordRefundOutcome(
                TICKET_ID, RefundStatusEnum.SUCCEEDED, "re_test_123"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recordRefundOutcome_writesStatusAndProviderRefundId() {
        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        when(ticketRepository.findByDomainId(TICKET_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        ticketCancellationService.recordRefundOutcome(TICKET_ID, RefundStatusEnum.SUCCEEDED, "re_test_123");

        assertThat(ticket.getRefundStatus()).isEqualTo(RefundStatusEnum.SUCCEEDED);
        assertThat(ticket.getProviderRefundId()).isEqualTo("re_test_123");
        verify(ticketRepository).save(ticket);
    }
}
