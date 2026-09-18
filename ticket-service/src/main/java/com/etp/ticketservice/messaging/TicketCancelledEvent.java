package com.etp.ticketservice.messaging;

import com.etp.ticketservice.tickets.TicketCancelReasonEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketCancelledEvent(
        UUID ticketId,
        UUID ticketTypeId,
        UUID eventId,
        UUID organizerId,
        UUID purchaserId,
        LocalDateTime cancelledAt,
        TicketCancelReasonEnum cancelReason
) {
}
