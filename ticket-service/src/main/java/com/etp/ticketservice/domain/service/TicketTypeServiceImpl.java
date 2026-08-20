package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.domain.exception.TicketsSoldOutException;
import com.etp.ticketservice.domain.exception.UserNotFoundException;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.TicketTypeRepository;
import com.etp.ticketservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;
    private final TicketEventPublisher ticketEventPublisher;

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
        // purely as an internal join key against tickets.ticket_type_id.
        int purchasedTickets = ticketRepository.countByTicketTypeId(ticketType.getId());
        Integer totalAvailable = ticketType.getTotalAvailable();

        if (purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException(ErrorCode.TICKET_SOLD_OUT, ticketTypeId);
        }

        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchaser(user);
        ticketType.addTicket(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);
        savedTicket = ticketRepository.save(savedTicket);

        ticketEventPublisher.publishTicketPurchased(savedTicket);

        return savedTicket;
    }
}
