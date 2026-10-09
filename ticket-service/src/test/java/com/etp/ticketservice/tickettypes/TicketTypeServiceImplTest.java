package com.etp.ticketservice.tickettypes;

import com.etp.ticketservice.orders.OrderStatusEnum;
import com.etp.ticketservice.orders.TicketOrderItemRepository;
import com.etp.ticketservice.tickets.Ticket;
import com.etp.ticketservice.tickets.TicketService;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.tickets.TicketStatusEnum;
import com.etp.ticketservice.tickettypes.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.tickets.exception.TicketsSoldOutException;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.tickets.TicketRepository;
import com.etp.ticketservice.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests -- see EventServiceImplTest's class comment for why this layer
// is worth testing directly. The two things worth pinning down here that a controller-
// or repository-level test structurally can't reach: the "active count, not raw count"
// sold-out arithmetic (a cancelled ticket must not count against a new purchase), and
// the null-totalAvailable-means-unlimited branch. Actual ticket creation (reference-code
// generation, QR, event publishing) is TicketService's responsibility now -- see
// TicketServiceImplTest for that; this class only asserts the delegation happens with a
// null orderItem (the legacy direct-purchase path, not order-driven issuance).
@ExtendWith(MockitoExtension.class)
class TicketTypeServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketOrderItemRepository ticketOrderItemRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID TICKET_TYPE_ID = UUID.randomUUID();

    @Test
    void purchaseTicket_userNotFound_throws() {
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID))
                .isInstanceOf(UserNotFoundException.class);

        verify(ticketTypeRepository, never()).findByDomainIdWithLock(any());
    }

    @Test
    void purchaseTicket_ticketTypeNotFound_throws() {
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }

    @Test
    void purchaseTicket_atCapacity_throwsSoldOut() {
        TicketType ticketType = ticketTypeWithCapacity(1, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        // One active sale already, +1 for this purchase would exceed the cap of 1.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(1);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(0);

        assertThatThrownBy(() -> ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID))
                .isInstanceOf(TicketsSoldOutException.class);

        verify(ticketService, never()).issueTicket(any(), any(), any());
    }

    @Test
    void purchaseTicket_cancelledTicketsDoNotCountAgainstCapacity() {
        TicketType ticketType = ticketTypeWithCapacity(1, 100L);
        User user = new User();
        Ticket issued = new Ticket();
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        // Zero ACTIVE sales (a prior ticket was cancelled and freed its slot) -- this
        // purchase should be allowed even though the raw historical count is higher.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(0);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(0);
        when(ticketService.issueTicket(eq(user), eq(ticketType), isNull())).thenReturn(issued);

        Ticket purchased = ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID);

        assertThat(purchased).isSameAs(issued);
    }

    @Test
    void purchaseTicket_withNoTotalAvailable_isUnlimited() {
        TicketType ticketType = ticketTypeWithCapacity(null, 100L);
        User user = new User();
        Ticket issued = new Ticket();
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        // Lenient: countActiveByTicketTypeId/sumReservedQuantityByTicketTypeId are never
        // even consulted for the sold-out check when totalAvailable is null, but stubbed
        // anyway in case that changes.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(1_000_000);
        when(ticketOrderItemRepository.sumReservedQuantityByTicketTypeId(eq(100L), eq(OrderStatusEnum.PENDING), any(LocalDateTime.class))).thenReturn(0);
        when(ticketService.issueTicket(eq(user), eq(ticketType), isNull())).thenReturn(issued);

        Ticket purchased = ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID);

        assertThat(purchased).isSameAs(issued);
    }

    private TicketType ticketTypeWithCapacity(Integer totalAvailable, long id) {
        TicketType ticketType = new TicketType();
        ticketType.setId(id);
        ticketType.setDomainId(TICKET_TYPE_ID);
        ticketType.setTotalAvailable(totalAvailable);
        return ticketType;
    }
}
