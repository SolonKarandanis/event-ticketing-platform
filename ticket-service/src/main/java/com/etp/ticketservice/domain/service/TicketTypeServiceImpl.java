package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.ReferenceCodeGenerationException;
import com.etp.ticketservice.domain.exception.TicketTypeNotFoundException;
import com.etp.ticketservice.domain.exception.TicketsSoldOutException;
import com.etp.ticketservice.domain.exception.UserNotFoundException;
import com.etp.ticketservice.domain.repository.TicketRepository;
import com.etp.ticketservice.domain.repository.TicketTypeRepository;
import com.etp.ticketservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    // Excludes visually ambiguous characters (0/O, 1/I/L) -- this code is meant to be read
    // off a phone screen and typed by hand at a door under time pressure.
    private static final String REFERENCE_CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int REFERENCE_CODE_LENGTH = 8;
    private static final int REFERENCE_CODE_MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;
    private final TicketEventPublisher ticketEventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

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
        Integer totalAvailable = ticketType.getTotalAvailable();

        // A null totalAvailable means this ticket type is unlimited -- only enforce the cap
        // when one is actually set.
        if (null != totalAvailable && purchasedTickets + 1 > totalAvailable) {
            throw new TicketsSoldOutException(ErrorCode.TICKET_SOLD_OUT, ticketTypeId);
        }

        Ticket ticket = new Ticket();
        ticket.setDomainId(UUID.randomUUID());
        ticket.setReferenceCode(generateReferenceCode());
        ticket.setStatus(TicketStatusEnum.PURCHASED);
        ticket.setPurchaser(user);
        ticketType.addTicket(ticket);

        Ticket savedTicket = ticketRepository.save(ticket);
        qrCodeService.generateQrCode(savedTicket);
        savedTicket = ticketRepository.save(savedTicket);

        ticketEventPublisher.publishTicketPurchased(savedTicket);

        return savedTicket;
    }

    // Unlike domainId (a UUID, collision-proof enough to generate-and-save with the DB's
    // unique constraint as the only backstop), this code is short enough that a collision,
    // while astronomically unlikely, is worth actually checking for.
    private String generateReferenceCode() {
        for (int attempt = 0; attempt < REFERENCE_CODE_MAX_ATTEMPTS; attempt++) {
            StringBuilder code = new StringBuilder(REFERENCE_CODE_LENGTH);
            for (int i = 0; i < REFERENCE_CODE_LENGTH; i++) {
                code.append(REFERENCE_CODE_ALPHABET.charAt(secureRandom.nextInt(REFERENCE_CODE_ALPHABET.length())));
            }
            String candidate = code.toString();
            if (ticketRepository.findByReferenceCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new ReferenceCodeGenerationException(ErrorCode.REFERENCE_CODE_GENERATION_FAILED);
    }
}
