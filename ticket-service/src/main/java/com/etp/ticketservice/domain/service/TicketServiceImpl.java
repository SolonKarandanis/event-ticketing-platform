package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.CancelTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.TicketSaleResponseDto;
import com.etp.ticketservice.domain.dto.response.TicketSaleTicketTypeResponseDto;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import com.etp.ticketservice.domain.enums.TicketValidationStatusEnum;
import com.etp.ticketservice.domain.exception.ErrorCode;
import com.etp.ticketservice.domain.exception.TicketAlreadyCancelledException;
import com.etp.ticketservice.domain.exception.TicketAlreadyValidatedException;
import com.etp.ticketservice.domain.exception.TicketEventAlreadyCompletedException;
import com.etp.ticketservice.domain.exception.TicketNotFoundException;
import com.etp.ticketservice.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
        return ticketRepository.findByPurchaserDomainId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
        return ticketRepository.findByDomainIdAndPurchaserDomainId(ticketId, userId);
    }

    @Override
    @Transactional
    public Ticket cancelTicketForUser(UUID userId, UUID ticketId, String note) {
        Ticket ticket = ticketRepository.findByDomainIdAndPurchaserDomainId(ticketId, userId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, ticketId));

        guardCancellable(ticket);

        return cancelTicket(ticket, TicketCancelReasonEnum.ATTENDEE_REQUEST, note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForEvent(UUID organizerId, UUID eventId, Pageable pageable) {
        return ticketRepository.findByEventDomainIdAndOrganizerDomainId(eventId, organizerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> listTicketsForOrganizer(UUID organizerId, Pageable pageable) {
        return ticketRepository.findByOrganizerDomainId(organizerId, pageable);
    }

    @Override
    @Transactional
    public Ticket cancelTicketForOrganizer(UUID organizerId, UUID eventId, UUID ticketId, String note) {
        Ticket ticket = ticketRepository.findByDomainIdAndEventDomainIdAndOrganizerDomainId(ticketId, eventId, organizerId)
                .orElseThrow(() -> new TicketNotFoundException(ErrorCode.TICKET_NOT_FOUND, ticketId));

        guardCancellable(ticket);

        return cancelTicket(ticket, TicketCancelReasonEnum.ORGANIZER_ACTION, note);
    }

    // Shared by both cancel paths above. Not shared with EventServiceImpl#cancelEvent's
    // bulk cascade -- that path deliberately skips an already-validated ticket rather
    // than erroring the whole cascade over one attendee who already got in.
    private void guardCancellable(Ticket ticket) {
        if (TicketStatusEnum.CANCELLED.equals(ticket.getStatus())) {
            throw new TicketAlreadyCancelledException(ErrorCode.TICKET_ALREADY_CANCELLED, ticket.getDomainId());
        }

        if (EventStatusEnum.COMPLETED.equals(ticket.getTicketType().getEvent().getStatus())) {
            throw new TicketEventAlreadyCompletedException(ErrorCode.TICKET_EVENT_ALREADY_COMPLETED, ticket.getDomainId());
        }

        boolean alreadyValidated = ticket.getValidations().stream()
                .anyMatch(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()));
        if (alreadyValidated) {
            throw new TicketAlreadyValidatedException(ErrorCode.TICKET_ALREADY_VALIDATED, ticket.getDomainId());
        }
    }

    private Ticket cancelTicket(Ticket ticket, TicketCancelReasonEnum reason, String note) {
        ticket.setStatus(TicketStatusEnum.CANCELLED);
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setCancelReason(reason);
        ticket.setCancelNote(note);

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketEventPublisher.publishTicketCancelled(savedTicket);

        return savedTicket;
    }

    @Override
    public ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType) {
        ListTicketTicketTypeResponseDto dto = new ListTicketTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        return dto;
    }

    @Override
    public ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket) {
        ListTicketResponseDto dto = new ListTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setStatus(ticket.getStatus());
        dto.setTicketType(convertToListTicketTicketTypeResponseDto(ticket.getTicketType()));
        return dto;
    }

    @Override
    public GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket) {
        GetTicketResponseDto dto = new GetTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setReferenceCode(ticket.getReferenceCode());
        dto.setStatus(ticket.getStatus());
        dto.setPrice(ticket.getTicketType().getPrice());
        dto.setDescription(ticket.getTicketType().getDescription());
        dto.setEventName(ticket.getTicketType().getEvent().getName());
        dto.setEventVenueName(ticket.getTicketType().getEvent().getVenue().getName());
        dto.setEventStart(ticket.getTicketType().getEvent().getStart());
        dto.setEventEnd(ticket.getTicketType().getEvent().getEnd());
        return dto;
    }

    @Override
    public CancelTicketResponseDto convertToCancelTicketResponseDto(Ticket ticket) {
        CancelTicketResponseDto dto = new CancelTicketResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setStatus(ticket.getStatus());
        dto.setCancelledAt(ticket.getCancelledAt());
        dto.setCancelReason(ticket.getCancelReason());
        dto.setCancelNote(ticket.getCancelNote());
        return dto;
    }

    @Override
    public TicketSaleTicketTypeResponseDto convertToTicketSaleTicketTypeResponseDto(TicketType ticketType) {
        TicketSaleTicketTypeResponseDto dto = new TicketSaleTicketTypeResponseDto();
        dto.setId(ticketType.getDomainId());
        dto.setName(ticketType.getName());
        dto.setPrice(ticketType.getPrice());
        return dto;
    }

    @Override
    public TicketSaleResponseDto convertToTicketSaleResponseDto(Ticket ticket) {
        TicketSaleResponseDto dto = new TicketSaleResponseDto();
        dto.setId(ticket.getDomainId());
        dto.setReferenceCode(ticket.getReferenceCode());
        dto.setStatus(ticket.getStatus());
        dto.setTicketType(convertToTicketSaleTicketTypeResponseDto(ticket.getTicketType()));
        dto.setPurchaserName(ticket.getPurchaser().getName());
        dto.setPurchaserEmail(ticket.getPurchaser().getEmail());
        dto.setEventId(ticket.getTicketType().getEvent().getDomainId());
        dto.setEventName(ticket.getTicketType().getEvent().getName());
        dto.setCreatedAt(ticket.getCreatedAt());
        return dto;
    }
}
