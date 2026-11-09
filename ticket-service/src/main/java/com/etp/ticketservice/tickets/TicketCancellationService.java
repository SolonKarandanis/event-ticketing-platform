package com.etp.ticketservice.tickets;

import java.util.UUID;

public interface TicketCancellationService {

    // Sets the ticket CANCELLED, persists, publishes ticket.cancelled, and resolves
    // (without attempting) whatever refund context exists -- null amount/session id if
    // the ticket was never paid via Stripe (orderItem == null). A paid ticket is left
    // refundStatus=PENDING in the same commit, so the owed refund is durable before any
    // Stripe call. Throws if already cancelled, except for the EVENT_CANCELLED cascade,
    // which treats that as nothing to do.
    TicketCancellationOutcome cancelAndPersist(
            UUID ticketDomainId, TicketCancelReasonEnum reason, String note);

    // The refund context of an already-cancelled ticket, for a retry.
    TicketCancellationOutcome loadRefundContext(UUID ticketDomainId);

    // Records the outcome of a refund attempt made after cancelAndPersist already
    // committed -- a separate, independent transaction, so a refund failure can never
    // roll back the cancellation itself.
    void recordRefundOutcome(
            UUID ticketDomainId, RefundStatusEnum refundStatus, String providerRefundId);

    // Counts a failed gateway call and returns the resulting status: still PENDING while
    // retries remain, FAILED once they run out.
    RefundStatusEnum recordRefundAttemptFailed(UUID ticketDomainId);

    // Applies a provider's later status change. Ignores a refund id with no matching
    // ticket, such as a whole-order refund or one made in the Stripe dashboard.
    void applyProviderRefundUpdate(String providerRefundId, RefundStatusEnum refundStatus);
}
