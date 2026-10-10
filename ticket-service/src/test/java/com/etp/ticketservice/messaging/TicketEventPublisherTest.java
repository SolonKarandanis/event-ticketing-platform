package com.etp.ticketservice.messaging;

import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.orders.TicketOrder;
import com.etp.ticketservice.orders.TicketOrderItem;
import com.etp.ticketservice.tickets.Ticket;
import com.etp.ticketservice.tickets.TicketCancelReasonEnum;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

// Pure Mockito unit tests -- this class had no direct coverage before #24 widened both
// events with orderId, which introduced the one piece of branching logic worth pinning
// down here: a ticket's orderId is null exactly when its orderItem is (the old direct
// ticket-type-purchase path, see TicketTypeServiceImpl), same nullability as
// Ticket.orderItem itself.
@ExtendWith(MockitoExtension.class)
class TicketEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    // Constructed in @BeforeEach, not as a field initializer -- @Mock fields are
    // injected by MockitoExtension after construction, so capturing one in a field
    // initializer would capture null.
    private TicketEventPublisher ticketEventPublisher;

    @BeforeEach
    void setUp() {
        ticketEventPublisher = new TicketEventPublisher(applicationEventPublisher);
    }

    @Test
    void publishTicketPurchased_withOrderItem_setsOrderIdFromTicketOrder() {
        Ticket ticket = purchasedTicket(orderItem(UUID.randomUUID()));

        ticketEventPublisher.publishTicketPurchased(ticket);

        TicketPurchasedEvent event = capturePurchasedEvent();
        assertThat(event.ticketId()).isEqualTo(ticket.getDomainId());
        assertThat(event.orderId()).isEqualTo(ticket.getOrderItem().getTicketOrder().getDomainId());
        assertThat(event.price()).isEqualTo(2500L);
        assertThat(event.currency()).isEqualTo("usd");
    }

    @Test
    void publishTicketPurchased_withoutOrderItem_leavesOrderIdNull() {
        Ticket ticket = purchasedTicket(null);

        ticketEventPublisher.publishTicketPurchased(ticket);

        assertThat(capturePurchasedEvent().orderId()).isNull();
    }

    @Test
    void publishTicketCancelled_withOrderItem_setsOrderIdFromTicketOrder() {
        Ticket ticket = cancelledTicket(orderItem(UUID.randomUUID()));

        ticketEventPublisher.publishTicketCancelled(ticket);

        TicketCancelledEvent event = captureCancelledEvent();
        assertThat(event.ticketId()).isEqualTo(ticket.getDomainId());
        assertThat(event.orderId()).isEqualTo(ticket.getOrderItem().getTicketOrder().getDomainId());
        assertThat(event.cancelReason()).isEqualTo(TicketCancelReasonEnum.ATTENDEE_REQUEST);
    }

    @Test
    void publishTicketCancelled_withoutOrderItem_leavesOrderIdNull() {
        Ticket ticket = cancelledTicket(null);

        ticketEventPublisher.publishTicketCancelled(ticket);

        assertThat(captureCancelledEvent().orderId()).isNull();
    }

    private Ticket purchasedTicket(TicketOrderItem orderItem) {
        Ticket ticket = baseTicket(orderItem);
        ticket.setCreatedAt(LocalDateTime.now());
        return ticket;
    }

    private Ticket cancelledTicket(TicketOrderItem orderItem) {
        Ticket ticket = baseTicket(orderItem);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setCancelReason(TicketCancelReasonEnum.ATTENDEE_REQUEST);
        return ticket;
    }

    private Ticket baseTicket(TicketOrderItem orderItem) {
        User organizer = new User();
        organizer.setDomainId(UUID.randomUUID());
        Event event = new Event();
        event.setDomainId(UUID.randomUUID());
        event.setOrganizer(organizer);
        TicketType ticketType = new TicketType();
        ticketType.setDomainId(UUID.randomUUID());
        ticketType.setEvent(event);
        ticketType.setPriceMinorUnits(2500L);
        ticketType.setCurrency("usd");
        User purchaser = new User();
        purchaser.setDomainId(UUID.randomUUID());

        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setTicketType(ticketType);
        ticket.setPurchaser(purchaser);
        ticket.setOrderItem(orderItem);
        return ticket;
    }

    private TicketOrderItem orderItem(UUID orderDomainId) {
        TicketOrder ticketOrder = new TicketOrder();
        ticketOrder.setDomainId(orderDomainId);
        TicketOrderItem orderItem = new TicketOrderItem();
        orderItem.setTicketOrder(ticketOrder);
        return orderItem;
    }

    private TicketPurchasedEvent capturePurchasedEvent() {
        ArgumentCaptor<TicketPurchasedEvent> captor = ArgumentCaptor.forClass(TicketPurchasedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    private TicketCancelledEvent captureCancelledEvent() {
        ArgumentCaptor<TicketCancelledEvent> captor = ArgumentCaptor.forClass(TicketCancelledEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }
}
