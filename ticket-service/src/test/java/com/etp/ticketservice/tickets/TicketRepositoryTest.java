package com.etp.ticketservice.tickets;

import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.orders.OrderStatusEnum;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderItem;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.venues.Venue;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.common.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as EventRepositoryTest. Doesn't cover every
// method on TicketRepository -- findByPurchaserDomainId, findByEventDomainIdAndOrganizerDomainId,
// and findByOrganizerDomainId are the same "to-one fetch join + Pageable" shape already
// proven safe by findByDomainIdAndPurchaserDomainId and EventRepositoryTest's
// findByOrganizerDomainId, so re-testing them here would just be repetition, not new
// coverage.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void countByTicketTypeId_countsCancelledTicketsToo() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);
        persistTicket(ticketType, purchaser, TicketStatusEnum.CANCELLED);

        assertThat(ticketRepository.countByTicketTypeId(ticketType.getId())).isEqualTo(2);
    }

    @Test
    void countActiveByTicketTypeId_excludesCancelled() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);
        persistTicket(ticketType, purchaser, TicketStatusEnum.CANCELLED);

        int active = ticketRepository.countActiveByTicketTypeId(ticketType.getId(), TicketStatusEnum.CANCELLED);

        assertThat(active).isEqualTo(1);
    }

    @Test
    void findByDomainIdAndPurchaserDomainId_fetchJoinsThroughToVenue() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        Ticket ticket = persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);

        entityManager.clear();

        Optional<Ticket> found = ticketRepository.findByDomainIdAndPurchaserDomainId(
                ticket.getDomainId(), purchaser.getDomainId());

        assertThat(found).isPresent();
        // Reading all the way down to venue without a LazyInitializationException is the
        // actual point -- GetTicketResponseDto needs ticket -> ticketType -> event ->
        // venue, and this method's three chained LEFT JOIN FETCHes are what make that
        // safe after the session's gone.
        assertThat(found.get().getTicketType().getEvent().getVenue().getName()).isEqualTo("Main Hall");
    }

    @Test
    void findByDomainIdAndEventDomainIdAndOrganizerDomainId_returnsEmpty_whenWrongOrganizer() {
        Event event = persistPublishedEvent();
        TicketType ticketType = persistTicketType(event, "General", 25.0);
        Ticket ticket = persistTicket(ticketType, persistUser("Jane Attendee"), TicketStatusEnum.PURCHASED);
        User someoneElse = persistUser("Someone Else");

        Optional<Ticket> asOwner = ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(
                ticket.getDomainId(), event.getDomainId(), event.getOrganizer().getDomainId());
        Optional<Ticket> asSomeoneElse = ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(
                ticket.getDomainId(), event.getDomainId(), someoneElse.getDomainId());

        assertThat(asOwner).isPresent();
        assertThat(asSomeoneElse).isEmpty();
    }

    @Test
    void findByReferenceCode_returnsTicket_whenFound() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        Ticket ticket = persistTicket(ticketType, persistUser("Jane Attendee"), TicketStatusEnum.PURCHASED);

        Optional<Ticket> found = ticketRepository.findByReferenceCode(ticket.getReferenceCode());

        assertThat(found).contains(ticket);
    }

    @Test
    void findByEventIdAndStatusNotWithValidations_excludesCancelledTickets() {
        Event event = persistPublishedEvent();
        TicketType ticketType = persistTicketType(event, "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        Ticket activeTicket = persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);
        persistTicket(ticketType, purchaser, TicketStatusEnum.CANCELLED);

        List<Ticket> tickets = ticketRepository.findByEventIdAndStatusNotWithValidations(
                event.getId(), TicketStatusEnum.CANCELLED);

        assertThat(tickets).extracting(Ticket::getDomainId).containsExactly(activeTicket.getDomainId());
    }

    @Test
    void findByDomainIdForCancellation_withOrderItem_fetchJoinsThroughToProviderCheckoutSessionId() {
        Event event = persistPublishedEvent();
        TicketType ticketType = persistTicketType(event, "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        TicketOrder ticketOrder = persistTicketOrder(event, purchaser, OrderStatusEnum.PAID, LocalDateTime.now().plusMinutes(30));
        ticketOrder.setProviderCheckoutSessionId("cs_test_123");
        entityManager.persistAndFlush(ticketOrder);
        TicketOrderItem orderItem = persistTicketOrderItem(ticketOrder, ticketType, 1, 25.0);
        Ticket ticket = persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED, orderItem);

        entityManager.clear();

        Optional<Ticket> found = ticketRepository.findByDomainIdForCancellation(ticket.getDomainId());

        assertThat(found).isPresent();
        // The point is reading all the way through orderItem -> ticketOrder (for the
        // refund call) and ticketType -> event -> organizer / purchaser (for
        // TicketEventPublisher#publishTicketCancelled) without a LazyInitializationException.
        assertThat(found.get().getOrderItem().getTicketOrder().getProviderCheckoutSessionId()).isEqualTo("cs_test_123");
        assertThat(found.get().getTicketType().getEvent().getOrganizer().getName()).isEqualTo("Jane Organizer");
        assertThat(found.get().getPurchaser().getName()).isEqualTo("Jane Attendee");
    }

    @Test
    void findByDomainIdForCancellation_withoutOrderItem_leavesOrderItemNull() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        Ticket ticket = persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);

        entityManager.clear();

        Optional<Ticket> found = ticketRepository.findByDomainIdForCancellation(ticket.getDomainId());

        assertThat(found).isPresent();
        assertThat(found.get().getOrderItem()).isNull();
        assertThat(found.get().getPurchaser().getName()).isEqualTo("Jane Attendee");
    }

    @Test
    void findByDomainIdAndPurchaserDomainIdForCancellation_fetchJoinsValidations() {
        TicketType ticketType = persistTicketType(persistPublishedEvent(), "General", 25.0);
        User purchaser = persistUser("Jane Attendee");
        Ticket ticket = persistTicket(ticketType, purchaser, TicketStatusEnum.PURCHASED);

        entityManager.clear();

        Optional<Ticket> found = ticketRepository.findByDomainIdAndPurchaserDomainIdForCancellation(
                ticket.getDomainId(), purchaser.getDomainId());

        assertThat(found).isPresent();
        // No validations persisted here -- the point is that reading the collection at all
        // doesn't throw LazyInitializationException post-clear; guardCancellable's own
        // "already validated" check is covered against real data via Mockito instead.
        assertThat(found.get().getValidations()).isEmpty();
    }

    private Event persistPublishedEvent() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        return persistEvent("Test Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
    }
}
