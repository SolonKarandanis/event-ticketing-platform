package com.etp.ticketservice.orders;

import com.etp.ticketservice.common.exception.ErrorCode;
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
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.tickettypes.TicketTypeRepository;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotInEventException;
import com.etp.ticketservice.tickets.exception.TicketsSoldOutException;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Separate Spring bean from CheckoutServiceImpl, deliberately -- calling a
// @Transactional method via `this.` from another method on the SAME bean bypasses the
// AOP proxy and silently never starts a transaction. reserve() and
// attachProviderCheckoutSession() are two short, independent transactions with the
// Stripe call happening in between them, outside both -- see CheckoutServiceImpl for why.
@Service
@RequiredArgsConstructor
public class TicketOrderServiceImpl implements TicketOrderService {

    // Stripe's own minimum too, so our DB hold and Stripe's session expiry can align.
    private static final int HOLD_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final TicketOrderRepository ticketOrderRepository;
    private final TicketOrderItemRepository ticketOrderItemRepository;
    private final TicketService ticketService;

    @Override
    @Transactional
    public TicketOrder reserve(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request) {
        User purchaser = userRepository.findByDomainId(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // Reused as-is -- same blended "doesn't exist or isn't PUBLISHED" semantics
        // EventServiceImpl#getPublishedEvent already relies on.
        Event event = eventRepository.findByDomainIdAndStatus(eventDomainId, EventStatusEnum.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException(ErrorCode.EVENT_NOT_FOUND, eventDomainId));

        Map<UUID, Integer> quantitiesByTicketTypeId = mergeDuplicateLines(request.getItems());

        // Ascending UUID order -- avoids deadlock between two concurrent checkouts whose
        // carts lock the same ticket types in different orders. purchaseTicket never
        // locks more than one TicketType, so it never participates in this ordering
        // question.
        List<UUID> orderedTicketTypeIds = quantitiesByTicketTypeId.keySet().stream().sorted().toList();

        TicketOrder order = new TicketOrder();
        order.setDomainId(UUID.randomUUID());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setProvider(PaymentProvider.STRIPE);
        order.setPurchaser(purchaser);
        order.setEvent(event);
        order.setExpiresAt(LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES));

        for (UUID ticketTypeId : orderedTicketTypeIds) {
            // Pessimistic lock -- same reasoning as TicketTypeServiceImpl#purchaseTicket:
            // prevents two concurrent reservations from both reading the same
            // pre-reservation availability count and overselling this ticket type.
            TicketType ticketType = ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)
                    .orElseThrow(() -> new TicketTypeNotFoundException(ErrorCode.TICKET_TYPE_NOT_FOUND, ticketTypeId));

            if (!ticketType.getEvent().getId().equals(event.getId())) {
                throw new TicketTypeNotInEventException(ErrorCode.TICKET_TYPE_NOT_IN_EVENT, ticketTypeId);
            }

            int quantity = quantitiesByTicketTypeId.get(ticketTypeId);
            int activeTickets = ticketRepository.countActiveByTicketTypeId(ticketType.getId(), TicketStatusEnum.CANCELLED);
            int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                    ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());
            Integer totalAvailable = ticketType.getTotalAvailable();

            // A null totalAvailable means this ticket type is unlimited -- only enforce
            // the cap when one is actually set.
            if (null != totalAvailable && activeTickets + reserved + quantity > totalAvailable) {
                throw new TicketsSoldOutException(ErrorCode.TICKET_SOLD_OUT, ticketTypeId);
            }

            TicketOrderItem item = new TicketOrderItem();
            item.setDomainId(UUID.randomUUID());
            item.setTicketType(ticketType);
            item.setQuantity(quantity);
            item.setUnitPriceAtCheckout(ticketType.getPrice());
            order.addItem(item);
        }

        return ticketOrderRepository.save(order);
    }

    @Override
    @Transactional
    public TicketOrder attachProviderCheckoutSession(UUID orderDomainId, String providerSessionId) {
        TicketOrder order = ticketOrderRepository.findByDomainId(orderDomainId)
                .orElseThrow(() -> new IllegalStateException("TicketOrder " + orderDomainId + " vanished between reserve() and attachProviderCheckoutSession()"));
        order.setProviderCheckoutSessionId(providerSessionId);
        return ticketOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void completeOrder(UUID orderDomainId) {
        TicketOrder order = ticketOrderRepository.findByDomainIdWithLock(orderDomainId)
                .orElseThrow(() -> new IllegalStateException("TicketOrder " + orderDomainId + " not found while completing order"));

        if (OrderStatusEnum.PENDING != order.getStatus()) {
            // Already settled by a concurrent webhook delivery or sweep pass -- the lock
            // + re-check above is exactly what makes this a safe no-op, not an error.
            return;
        }

        for (TicketOrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                ticketService.issueTicket(order.getPurchaser(), item.getTicketType(), item);
            }
        }

        order.setStatus(OrderStatusEnum.PAID);
        ticketOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void expireOrder(UUID orderDomainId) {
        TicketOrder order = ticketOrderRepository.findByDomainIdWithLock(orderDomainId)
                .orElseThrow(() -> new IllegalStateException("TicketOrder " + orderDomainId + " not found while expiring order"));

        if (OrderStatusEnum.PENDING != order.getStatus()) {
            return;
        }

        order.setStatus(OrderStatusEnum.EXPIRED);
        ticketOrderRepository.save(order);
    }

    // Duplicate ticketTypeId lines in one request are summed rather than rejected --
    // friendlier, and no new error code needed for what's really the same cart line
    // submitted twice. LinkedHashMap preserves the request's original line order.
    private Map<UUID, Integer> mergeDuplicateLines(List<CheckoutLineItemRequestDto> items) {
        Map<UUID, Integer> quantitiesByTicketTypeId = new LinkedHashMap<>();
        for (CheckoutLineItemRequestDto item : items) {
            quantitiesByTicketTypeId.merge(item.getTicketTypeId(), item.getQuantity(), Integer::sum);
        }
        return quantitiesByTicketTypeId;
    }
}
