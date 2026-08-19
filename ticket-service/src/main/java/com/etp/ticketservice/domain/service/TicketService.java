package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);

    Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId);

    ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType);

    ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket);

    GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket);
}
