package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.exception.ReferenceCodeGenerationException;
import com.etp.ticketservice.domain.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.domain.exception.TicketsSoldOutException;
import com.etp.ticketservice.domain.exception.UserNotFoundException;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.TicketTypeRepository;
import com.etp.ticketservice.domain.repository.UserRepository;
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
// is worth testing directly. The two things worth pinning down here that a controller-
// or repository-level test structurally can't reach: the "active count, not raw count"
// sold-out arithmetic (a cancelled ticket must not count against a new purchase), and
// the null-totalAvailable-means-unlimited branch.
@ExtendWith(MockitoExtension.class)
class TicketTypeServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private TicketEventPublisher ticketEventPublisher;

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

        assertThatThrownBy(() -> ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID))
                .isInstanceOf(TicketsSoldOutException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void purchaseTicket_cancelledTicketsDoNotCountAgainstCapacity() {
        TicketType ticketType = ticketTypeWithCapacity(1, 100L);
        User user = new User();
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(user));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        // Zero ACTIVE sales (a prior ticket was cancelled and freed its slot) -- this
        // purchase should be allowed even though the raw historical count is higher.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(0);
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket purchased = ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID);

        assertThat(purchased.getStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
        assertThat(purchased.getPurchaser()).isEqualTo(user);
        verify(qrCodeService).generateQrCode(purchased);
        verify(ticketEventPublisher).publishTicketPurchased(purchased);
    }

    @Test
    void purchaseTicket_withNoTotalAvailable_isUnlimited() {
        TicketType ticketType = ticketTypeWithCapacity(null, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        // Lenient: countActiveByTicketTypeId is never even consulted for the sold-out
        // check when totalAvailable is null, but stub it anyway in case that changes.
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(1_000_000);
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.empty());
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket purchased = ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID);

        assertThat(purchased.getStatus()).isEqualTo(TicketStatusEnum.PURCHASED);
    }

    @Test
    void purchaseTicket_referenceCodeCollisions_retriesThenGivesUp() {
        TicketType ticketType = ticketTypeWithCapacity(null, 100L);
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(new User()));
        when(ticketTypeRepository.findByDomainIdWithLock(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countActiveByTicketTypeId(100L, TicketStatusEnum.CANCELLED)).thenReturn(0);
        // Every candidate "collides" -- forces every one of the 5 generation attempts to
        // be exhausted rather than succeeding on the first try.
        when(ticketRepository.findByReferenceCode(any())).thenReturn(Optional.of(new Ticket()));

        assertThatThrownBy(() -> ticketTypeService.purchaseTicket(USER_ID, TICKET_TYPE_ID))
                .isInstanceOf(ReferenceCodeGenerationException.class);

        verify(ticketRepository, never()).save(any());
    }

    private TicketType ticketTypeWithCapacity(Integer totalAvailable, long id) {
        TicketType ticketType = new TicketType();
        ticketType.setId(id);
        ticketType.setDomainId(TICKET_TYPE_ID);
        ticketType.setTotalAvailable(totalAvailable);
        return ticketType;
    }
}
