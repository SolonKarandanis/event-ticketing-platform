package com.etp.ticketservice.tickets;

import com.etp.ticketservice.tickets.dto.CancelTicketResponseDto;
import com.etp.ticketservice.tickets.dto.GetTicketResponseDto;
import com.etp.ticketservice.tickets.dto.ListTicketResponseDto;
import com.etp.ticketservice.tickets.dto.ListTicketTicketTypeResponseDto;
import com.etp.ticketservice.tickets.dto.TicketSaleResponseDto;
import com.etp.ticketservice.tickets.dto.TicketSaleTicketTypeResponseDto;
import com.etp.ticketservice.tickettypes.TicketType;
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
