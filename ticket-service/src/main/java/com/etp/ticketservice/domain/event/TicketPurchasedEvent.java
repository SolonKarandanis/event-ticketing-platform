package com.etp.ticketservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPurchasedEvent(
        UUID ticketId,
        UUID ticketTypeId,
        UUID eventId,
        UUID purchaserId,
        Double price,
        LocalDateTime purchasedAt
) {
}
