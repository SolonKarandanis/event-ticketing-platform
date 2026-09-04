package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.TicketAlreadyCancelledException;
import com.etp.ticketservice.domain.exception.TicketAlreadyValidatedException;
import com.etp.ticketservice.domain.exception.TicketEventAlreadyCompletedException;
import com.etp.ticketservice.domain.exception.TicketNotFoundException;
import com.etp.ticketservice.domain.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly rather than only through TicketController's mocked-service
// slice. guardCancellable is shared by both cancel entry points; its three branches are
// exercised once via cancelTicketForUser, and cancelTicketForOrganizer gets its own
// happy-path test mainly to confirm it applies the guard too and records the right
// cancel reason -- not a full re-run of every guard branch.
@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketEventPublisher ticketEventPublisher;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORGANIZER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID TICKET_ID = UUID.randomUUID();

    @Test
    void cancelTicketForUser_notFound_throws() {
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void cancelTicketForUser_alreadyCancelled_throws() {
        Ticket ticket = purchasableTicket();
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketAlreadyCancelledException.class);

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancelTicketForUser_eventAlreadyCompleted_throws() {
        Ticket ticket = purchasableTicket();
        ticket.getTicketType().getEvent().setStatus(EventStatusEnum.COMPLETED);
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketEventAlreadyCompletedException.class);
    }

    @Test
    void cancelTicketForUser_alreadyValidated_throws() {
        Ticket ticket = purchasableTicket();
        TicketValidation validation = new TicketValidation();
        validation.setStatus(TicketValidationStatusEnum.VALID);
        ticket.addValidation(validation);
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null))
                .isInstanceOf(TicketAlreadyValidatedException.class);
    }

    @Test
    void cancelTicketForUser_invalidatedValidationDoesNotBlockCancellation() {
        Ticket ticket = purchasableTicket();
        TicketValidation validation = new TicketValidation();
        validation.setStatus(TicketValidationStatusEnum.INVALID);
        ticket.addValidation(validation);
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, null);

        assertThat(cancelled.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
    }

    @Test
    void cancelTicketForUser_happyPath_setsAttendeeRequestReasonAndPublishesEvent() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndPurchaserDomainId(TICKET_ID, USER_ID)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket cancelled = ticketService.cancelTicketForUser(USER_ID, TICKET_ID, "Changed my mind");

        assertThat(cancelled.getStatus()).isEqualTo(TicketStatusEnum.CANCELLED);
        assertThat(cancelled.getCancelReason()).isEqualTo(TicketCancelReasonEnum.ATTENDEE_REQUEST);
        assertThat(cancelled.getCancelNote()).isEqualTo("Changed my mind");
        assertThat(cancelled.getCancelledAt()).isNotNull();
        verify(ticketEventPublisher).publishTicketCancelled(cancelled);
    }

    @Test
    void cancelTicketForOrganizer_notFound_throws() {
        when(ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(TICKET_ID, EVENT_ID, ORGANIZER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.cancelTicketForOrganizer(ORGANIZER_ID, EVENT_ID, TICKET_ID, null))
                .isInstanceOf(TicketNotFoundException.class);
    }

    @Test
    void cancelTicketForOrganizer_happyPath_setsOrganizerActionReason() {
        Ticket ticket = purchasableTicket();
        when(ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(TICKET_ID, EVENT_ID, ORGANIZER_ID))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Ticket cancelled = ticketService.cancelTicketForOrganizer(ORGANIZER_ID, EVENT_ID, TICKET_ID, "Event rescheduled");

        assertThat(cancelled.getCancelReason()).isEqualTo(TicketCancelReasonEnum.ORGANIZER_ACTION);
        assertThat(cancelled.getCancelNote()).isEqualTo("Event rescheduled");
    }

    private Ticket purchasableTicket() {
        Event event = new Event();
        event.setStatus(EventStatusEnum.PUBLISHED);
        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);

        Ticket ticket = new Ticket();
        ticket.setDomainId(TICKET_ID);
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setTicketType(ticketType);
        return ticket;
    }
}
