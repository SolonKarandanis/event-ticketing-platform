package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.CancelTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.GetTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketResponseDto;
import com.etp.ticketservice.domain.dto.response.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.domain.dto.response.TicketSaleResponseDto;
import com.etp.ticketservice.domain.dto.response.TicketSaleTicketTypeResponseDto;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable);

    Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId);

    Ticket cancelTicketForUser(UUID userId, UUID ticketId, String note);

    Page<Ticket> listTicketsForEvent(UUID organizerId, UUID eventId, Pageable pageable);

    Page<Ticket> listTicketsForOrganizer(UUID organizerId, Pageable pageable);

    Ticket cancelTicketForOrganizer(UUID organizerId, UUID eventId, UUID ticketId, String note);

    ListTicketTicketTypeResponseDto convertToListTicketTicketTypeResponseDto(TicketType ticketType);

    ListTicketResponseDto convertToListTicketResponseDto(Ticket ticket);

    GetTicketResponseDto convertToGetTicketResponseDto(Ticket ticket);

    CancelTicketResponseDto convertToCancelTicketResponseDto(Ticket ticket);

    TicketSaleTicketTypeResponseDto convertToTicketSaleTicketTypeResponseDto(TicketType ticketType);

    TicketSaleResponseDto convertToTicketSaleResponseDto(Ticket ticket);
}
