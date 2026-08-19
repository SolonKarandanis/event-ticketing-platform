package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.entity.Ticket;

import java.util.UUID;

public interface TicketTypeService {
    Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}
