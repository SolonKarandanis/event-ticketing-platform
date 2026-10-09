package com.etp.ticketservice.tickettypes;

import com.etp.ticketservice.orders.OrderStatusEnum;
import com.etp.ticketservice.orders.TicketOrderItemRepository;
import com.etp.ticketservice.tickets.Ticket;
import com.etp.ticketservice.tickets.TicketService;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.tickets.TicketStatusEnum;
import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.tickets.exception.TicketsSoldOutException;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.tickets.TicketRepository;
import com.etp.ticketservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final TicketOrderItemRepository ticketOrderItemRepository;
    private final TicketService ticketService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        User user = userRepository.findByDomainId(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // Pessimistic lock -- prevents two concurrent purchases from both reading the same
        // pre-purchase availability count and overselling this ticket type.
        TicketType ticketType = ticketTypeRepository.findByDomainIdWithLock(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException(ErrorCode.TICKET_TYPE_NOT_FOUND, ticketTypeId));

        // ticketType.getId() here is the resolved entity's internal sequential id, used
        // purely as an internal join key against tickets.ticket_type_id. Active count,
        // not the raw historical one -- a cancelled ticket freed its slot back up, so
        // it shouldn't count against a new purchase the same way a live one does.
        int purchasedTickets = ticketRepository.countActiveByTicketTypeId(ticketType.getId(), TicketStatusEnum.CANCELLED);
        // Issue #20's checkout flow can hold a PENDING reservation against this same
        // ticket type before any Ticket row exists -- without subtracting it here, this
        // single-ticket path could still oversell straight through an active cart hold.
        int reserved = ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(
                ticketType.getId(), OrderStatusEnum.PENDING, LocalDateTime.now());
        Integer totalAvailable = ticketType.getTotalAvailable();

        // A null totalAvailable means this ticket type is unlimited -- only enforce the cap
        // when one is actually set.
        if (null != totalAvailable && purchasedTickets + reserved + 1 > totalAvailable) {
            throw new TicketsSoldOutException(ErrorCode.TICKET_SOLD_OUT, ticketTypeId);
        }

        // Not an order-item-backed purchase -- this is the legacy direct-purchase path,
        // predating carts (issue #20).
        return ticketService.issueTicket(user, ticketType, null);
    }
}
