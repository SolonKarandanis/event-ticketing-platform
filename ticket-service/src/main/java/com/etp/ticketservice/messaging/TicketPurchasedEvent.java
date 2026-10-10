package com.etp.ticketservice.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketPurchasedEvent(
        UUID ticketId,
        UUID ticketTypeId,
        UUID eventId,
        UUID organizerId,
        UUID purchaserId,
        // null <=> this ticket wasn't issued from a TicketOrderItem (the old direct
        // ticket-type-purchase path, see TicketTypeServiceImpl) -- same null-ability as
        // Ticket.orderItem itself.
        UUID orderId,
        Long price,
        String currency,
        LocalDateTime purchasedAt
) {
}
