package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

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
}
