package com.etp.ticketservice.orders;

import com.etp.ticketservice.common.AbstractPostgresContainerTest;
import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.venues.Venue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as TicketTypeRepositoryTest -- the query under
// test joins TicketOrderItem -> TicketOrder and filters on both a status equality and a
// timestamp comparison, exactly the kind of thing worth proving against a real database
// rather than trusting the JPQL by inspection.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketOrderItemRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private TicketOrderItemRepository ticketOrderItemRepository;

    @Test
    void sumReservedQuantityByTicketTypeId_sumsAcrossMultipleLivePendingOrders() {
        TicketType ticketType = publishedTicketType();
        User purchaser = persistUser("Alice");
        TicketOrder orderA = persistTicketOrder(ticketType.getEvent(), purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().plusMinutes(30));
        persistTicketOrderItem(orderA, ticketType, 2, 10.0);
        TicketOrder orderB = persistTicketOrder(ticketType.getEvent(), purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().plusMinutes(30));
        persistTicketOrderItem(orderB, ticketType, 3, 10.0);

        int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());

        assertThat(reserved).isEqualTo(5);
    }

    @Test
    void sumReservedQuantityByTicketTypeId_excludesOrderPastItsExpiresAt() {
        TicketType ticketType = publishedTicketType();
        User purchaser = persistUser("Bob");
        // Still PENDING, but its hold already lapsed -- nothing (until #21's sweep)
        // flips this to EXPIRED, so the query itself is the only thing excluding it.
        TicketOrder expiredHold = persistTicketOrder(ticketType.getEvent(), purchaser, OrderStatusEnum.PENDING, LocalDateTime.now().minusMinutes(1));
        persistTicketOrderItem(expiredHold, ticketType, 4, 10.0);

        int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());

        assertThat(reserved).isZero();
    }

    @Test
    void sumReservedQuantityByTicketTypeId_excludesOrdersInADifferentStatus() {
        TicketType ticketType = publishedTicketType();
        User purchaser = persistUser("Carol");
        TicketOrder paidOrder = persistTicketOrder(ticketType.getEvent(), purchaser, OrderStatusEnum.PAID, LocalDateTime.now().plusMinutes(30));
        persistTicketOrderItem(paidOrder, ticketType, 7, 10.0);

        int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());

        assertThat(reserved).isZero();
    }

    @Test
    void sumReservedQuantityByTicketTypeId_returnsZero_whenNoOrdersExist() {
        TicketType ticketType = publishedTicketType();

        int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());

        assertThat(reserved).isZero();
    }

    private TicketType publishedTicketType() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        Event event = persistEvent("Test Event", venue, organizer, EventStatusEnum.PUBLISHED, LocalDateTime.now().plusDays(1));
        return persistTicketType(event, "General", 10.0);
    }
}
