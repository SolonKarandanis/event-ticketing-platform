package com.etp.ticketservice.domain.event;

import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;

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
