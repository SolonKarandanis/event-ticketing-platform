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
        // Same nullability as TicketPurchasedEvent.orderId -- see that field's comment.
        // analytics-service's recordCancellation doesn't use this today (it only ever
        // writes cancelledAt against an already-recorded sale), but it's cheap to carry
        // alongside the purchased event's own orderId widening, same "capture now" #23
        // precedent already used for the currency column.
        UUID orderId,
        LocalDateTime cancelledAt,
        TicketCancelReasonEnum cancelReason
) {
}
