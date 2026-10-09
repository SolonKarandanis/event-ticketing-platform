package com.etp.ticketservice.orders;

import com.etp.ticketservice.common.AbstractPostgresContainerTest;
import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.venues.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as TicketTypeRepositoryTest -- only proves
// findByDomainIdWithLock resolves the right row, not the @Lock itself (see that class's
// own comment for why a genuinely concurrent test would be a different, flakier kind of
// test). findDomainIdsByStatusAndExpiresAtBefore is the sweep's entire candidate-selection
// query, worth proving against a real database rather than trusting the JPQL by inspection.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketOrderRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private TicketOrderRepository ticketOrderRepository;

    @Test
    void findByDomainIdWithLock_returnsOrder_whenFound() {
        Event event = publishedEvent();
        User purchaser = persistUser("Alice");
        TicketOrder order = persistTicketOrder(event, purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().plusMinutes(30));

        Optional<TicketOrder> found = ticketOrderRepository.findByDomainIdWithLock(order.getDomainId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatusEnum.PENDING);
    }

    @Test
    void findByDomainIdWithLock_returnsEmpty_whenNotFound() {
        assertThat(ticketOrderRepository.findByDomainIdWithLock(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findDomainIdsByStatusAndExpiresAtBefore_returnsOnlyPendingPastDeadline() {
        Event event = publishedEvent();
        User purchaser = persistUser("Bob");

        TicketOrder pastDeadline = persistTicketOrder(event, purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().minusMinutes(1));
        TicketOrder notYetExpired = persistTicketOrder(event, purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().plusMinutes(30));
        TicketOrder paidPastDeadline = persistTicketOrder(event, purchaser, OrderStatusEnum.PAID, LocalDateTime.now().minusMinutes(1));

        List<UUID> candidates = ticketOrderRepository.findDomainIdsByStatusAndExpiresAtBefore(
                OrderStatusEnum.PENDING, LocalDateTime.now());

        assertThat(candidates).containsExactly(pastDeadline.getDomainId());
        assertThat(candidates).doesNotContain(notYetExpired.getDomainId(), paidPastDeadline.getDomainId());
    }

    private Event publishedEvent() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        return persistEvent("Test Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
    }
}
