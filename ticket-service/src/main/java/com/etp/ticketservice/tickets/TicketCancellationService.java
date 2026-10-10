package com.etp.ticketservice.tickets;

import java.util.UUID;

public interface TicketCancellationService {

    // Sets the ticket CANCELLED, persists, publishes ticket.cancelled, and resolves
    // (without attempting) whatever refund context exists -- null amount/session id if
    // the ticket was never paid via Stripe (orderItem == null).
    TicketCancellationOutcome cancelAndPersist(UUID ticketDomainId, TicketCancelReasonEnum reason, String note);

    // Records the outcome of a refund attempt made after cancelAndPersist already
    // committed -- a separate, independent transaction, so a refund failure can never
    // roll back the cancellation itself.
    void recordRefundOutcome(UUID ticketDomainId, RefundStatusEnum refundStatus, String providerRefundId);
}
