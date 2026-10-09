package com.etp.ticketservice.orders;

import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.events.EventRepository;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.events.exception.EventNotFoundException;
import com.etp.ticketservice.orders.dto.CheckoutLineItemRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.payments.PaymentProvider;
import com.etp.ticketservice.tickets.TicketRepository;
import com.etp.ticketservice.tickets.TicketService;
import com.etp.ticketservice.tickets.TicketStatusEnum;
import com.etp.ticketservice.tickets.exception.TicketsSoldOutException;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.tickettypes.TicketTypeRepository;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotInEventException;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests, same rationale as TicketTypeServiceImplTest/EventServiceImplTest:
// the sold-out-accounting-for-reservations arithmetic, the duplicate-cart-line merge, the
// cross-event ticket-type guard, and the lock-ordering rule are all branching logic that
// lives here and is worth pinning down directly, rather than only through a controller- or
// repository-level test.
@ExtendWith(MockitoExtension.class)
class TicketOrderServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketOrderRepository ticketOrderRepository;
    @Mock
    private TicketOrderItemRepository ticketOrderItemRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketOrderServiceImpl ticketOrderService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();

    @Test
    void reserve_userNotFound_throws() {
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(UUID.randomUUID(), 1)))
                .isInstanceOf(UserNotFoundException.class);

        verify(eventRepository, never()).findByDomainIdAndStatus(any(), any());
    }

    @Test
    void reserve_eventNotFoundOrNotPublished_throws() {
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(UUID.randomUUID(), 1)))
                .isInstanceOf(EventNotFoundException.class);

        verify(ticketTypeRepository, never()).findByDomainIdWithLock(any());
    }

    @Test
    void reserve_ticketTypeNotFound_throws() {
        UUID ticketTypeId = UUID.randomUUID();
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(eventWithId(10L)));
        when(ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(ticketTypeId, 1)))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }

    @Test
    void reserve_ticketTypeBelongsToDifferentEvent_throws() {
        UUID ticketTypeId = UUID.randomUUID();
        Event requestedEvent = eventWithId(10L);
        Event otherEvent = eventWithId(99L);
        TicketType ticketType = ticketTypeWithCapacity(otherEvent, null, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(requestedEvent));
        when(ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));

        assertThatThrownBy(() -> ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(ticketTypeId, 1)))
                .isInstanceOf(TicketTypeNotInEventException.class);
    }

    @Test
    void reserve_atCapacityAccountingForExistingReservations_throwsSoldOut() {
        UUID ticketTypeId = UUID.randomUUID();
        Event event = eventWithId(10L);
        TicketType ticketType = ticketTypeWithCapacity(event, 5, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));
        // 3 active + 2 already held by another pending order = 5, already at the cap of
        // 5 -- requesting 1 more must push it over even though no real Ticket exists yet.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(3);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(2);

        assertThatThrownBy(() -> ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(ticketTypeId, 1)))
                .isInstanceOf(TicketsSoldOutException.class);

        verify(ticketOrderRepository, never()).save(any());
    }

    @Test
    void reserve_mergesDuplicateCartLinesForSameTicketType() {
        UUID ticketTypeId = UUID.randomUUID();
        Event event = eventWithId(10L);
        TicketType ticketType = ticketTypeWithCapacity(event, null, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(0);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(0);
        when(ticketOrderRepository.save(any(TicketOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCheckoutRequestDto request = new CreateCheckoutRequestDto(List.of(
                new CheckoutLineItemRequestDto(ticketTypeId, 2),
                new CheckoutLineItemRequestDto(ticketTypeId, 3)));

        TicketOrder order = ticketOrderService.reserve(USER_ID, EVENT_ID, request);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().iterator().next().getQuantity()).isEqualTo(5);
        // One merged line means one lock, not two -- proves the merge happens before
        // locking, not just before the availability check.
        verify(ticketTypeRepository, times(1)).findByDomainIdWithLock(ticketTypeId);
    }

    @Test
    void reserve_success_buildsPendingOrderWithSnapshottedPrice() {
        UUID ticketTypeId = UUID.randomUUID();
        User purchaser = new User();
        Event event = eventWithId(10L);
        TicketType ticketType = ticketTypeWithCapacity(event, null, 100L);
        ticketType.setPrice(25.0);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(purchaser));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(0);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(0);
        when(ticketOrderRepository.save(any(TicketOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketOrder order = ticketOrderService.reserve(USER_ID, EVENT_ID, requestFor(ticketTypeId, 2));

        assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PENDING);
        assertThat(order.getProvider()).isEqualTo(PaymentProvider.STRIPE);
        assertThat(order.getPurchaser()).isEqualTo(purchaser);
        assertThat(order.getEvent()).isEqualTo(event);
        assertThat(order.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(order.getItems()).hasSize(1);
        TicketOrderItem item = order.getItems().iterator().next();
        assertThat(item.getQuantity()).isEqualTo(2);
        // Snapshot at checkout time, not a live reference -- see TicketOrderItem's own
        // comment on unitPriceAtCheckout.
        assertThat(item.getUnitPriceAtCheckout()).isEqualTo(25.0);
    }

    @Test
    void reserve_locksMultipleTicketTypesInAscendingDomainIdOrder() {
        // Derived from UUID's own natural ordering (signed most-significant-bits
        // comparison, NOT lexical string order) rather than assumed from literals --
        // the production code sorts via UUID::compareTo, so the test must determine
        // "low"/"high" the same way to actually exercise that ordering.
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID lowId = a.compareTo(b) <= 0 ? a : b;
        UUID highId = a.compareTo(b) <= 0 ? b : a;
        Event event = eventWithId(10L);
        TicketType lowType = ticketTypeWithCapacity(event, null, 100L);
        TicketType highType = ticketTypeWithCapacity(event, null, 101L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(eventRepository.findByDomainIdAndStatus(EVENT_ID, EventStatusEnum.PUBLISHED)).thenReturn(Optional.of(event));
        when(ticketTypeRepository.findByDomainIdWithLock(lowId)).thenReturn(Optional.of(lowType));
        when(ticketTypeRepository.findByDomainIdWithLock(highId)).thenReturn(Optional.of(highType));
        when(ticketRepository.countActiveByTicketTypeId(any(), eq(TicketStatusEnum.CANCELLED))).thenReturn(0);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(any(), eq(OrderStatusEnum.PENDING), any())).thenReturn(0);
        when(ticketOrderRepository.save(any(TicketOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Submitted high-then-low -- the service must still lock low-then-high to avoid
        // deadlocking against a concurrent checkout that locks the same two ticket types.
        CreateCheckoutRequestDto request = new CreateCheckoutRequestDto(List.of(
                new CheckoutLineItemRequestDto(highId, 1),
                new CheckoutLineItemRequestDto(lowId, 1)));

        ticketOrderService.reserve(USER_ID, EVENT_ID, request);

        InOrder inOrder = inOrder(ticketTypeRepository);
        inOrder.verify(ticketTypeRepository).findByDomainIdWithLock(lowId);
        inOrder.verify(ticketTypeRepository).findByDomainIdWithLock(highId);
    }

    @Test
    void attachProviderCheckoutSession_setsSessionIdAndSaves() {
        UUID orderId = UUID.randomUUID();
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        when(ticketOrderRepository.findByDomainId(orderId)).thenReturn(Optional.of(order));
        when(ticketOrderRepository.save(order)).thenReturn(order);

        TicketOrder updated = ticketOrderService.attachProviderCheckoutSession(orderId, "cs_test_123");

        assertThat(updated.getProviderCheckoutSessionId()).isEqualTo("cs_test_123");
    }

    @Test
    void completeOrder_notFound_throws() {
        UUID orderId = UUID.randomUUID();
        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketOrderService.completeOrder(orderId))
                .isInstanceOf(IllegalStateException.class);

        verify(ticketService, never()).issueTicket(any(), any(), any());
    }

    @Test
    void completeOrder_alreadySettled_isNoOp() {
        UUID orderId = UUID.randomUUID();
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setStatus(OrderStatusEnum.PAID);
        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.of(order));

        // Already settled by a concurrent webhook delivery or sweep pass -- a safe no-op.
        ticketOrderService.completeOrder(orderId);

        verify(ticketService, never()).issueTicket(any(), any(), any());
        verify(ticketOrderRepository, never()).save(any());
    }

    @Test
    void completeOrder_happyPath_issuesTicketsPerItemQuantityAndMarksPaid() {
        UUID orderId = UUID.randomUUID();
        User purchaser = new User();
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setStatus(OrderStatusEnum.PENDING);
        order.setPurchaser(purchaser);

        TicketType ticketTypeA = new TicketType();
        TicketOrderItem itemA = new TicketOrderItem();
        itemA.setTicketType(ticketTypeA);
        itemA.setQuantity(2);
        order.addItem(itemA);

        TicketType ticketTypeB = new TicketType();
        TicketOrderItem itemB = new TicketOrderItem();
        itemB.setTicketType(ticketTypeB);
        itemB.setQuantity(1);
        order.addItem(itemB);

        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.of(order));
        when(ticketOrderRepository.save(order)).thenReturn(order);

        ticketOrderService.completeOrder(orderId);

        verify(ticketService, times(2)).issueTicket(purchaser, ticketTypeA, itemA);
        verify(ticketService, times(1)).issueTicket(purchaser, ticketTypeB, itemB);
        assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PAID);
    }

    @Test
    void expireOrder_notFound_throws() {
        UUID orderId = UUID.randomUUID();
        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketOrderService.expireOrder(orderId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expireOrder_alreadySettled_isNoOp() {
        UUID orderId = UUID.randomUUID();
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setStatus(OrderStatusEnum.PAID);
        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.of(order));

        ticketOrderService.expireOrder(orderId);

        verify(ticketOrderRepository, never()).save(any());
        assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PAID);
    }

    @Test
    void expireOrder_happyPath_marksExpired() {
        UUID orderId = UUID.randomUUID();
        TicketOrder order = new TicketOrder();
        order.setDomainId(orderId);
        order.setStatus(OrderStatusEnum.PENDING);
        when(ticketOrderRepository.findByDomainIdWithLock(orderId)).thenReturn(Optional.of(order));
        when(ticketOrderRepository.save(order)).thenReturn(order);

        ticketOrderService.expireOrder(orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.EXPIRED);
    }

    private CreateCheckoutRequestDto requestFor(UUID ticketTypeId, int quantity) {
        return new CreateCheckoutRequestDto(List.of(new CheckoutLineItemRequestDto(ticketTypeId, quantity)));
    }

    private Event eventWithId(long id) {
        Event event = new Event();
        event.setId(id);
        event.setDomainId(UUID.randomUUID());
        return event;
    }

    private TicketType ticketTypeWithCapacity(Event event, Integer totalAvailable, long id) {
        TicketType ticketType = new TicketType();
        ticketType.setId(id);
        ticketType.setDomainId(UUID.randomUUID());
        ticketType.setEvent(event);
        ticketType.setTotalAvailable(totalAvailable);
        return ticketType;
    }
}
