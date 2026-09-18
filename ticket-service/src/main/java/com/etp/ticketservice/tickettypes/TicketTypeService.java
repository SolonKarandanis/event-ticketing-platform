package com.etp.ticketservice.tickettypes;

import com.etp.ticketservice.tickets.Ticket;

import java.util.UUID;

public interface TicketTypeService {
    Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}
