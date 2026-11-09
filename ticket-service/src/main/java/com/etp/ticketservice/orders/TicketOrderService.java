package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.tickets.RefundStatusEnum;
import java.util.UUID;

public interface TicketOrderService {

    TicketOrder reserve(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request);

    TicketOrder attachProviderCheckoutSession(UUID orderDomainId, String providerSessionId);

    // Called once Stripe has taken payment. Issues every held ticket and marks the order
    // PAID, or marks it REFUND_PENDING if it can no longer be fulfilled: its event is no
    // longer on sale, or it had already expired and the capacity is gone. A safe no-op for
    // an order that's already settled, so webhook redelivery can call this repeatedly.
    // providerSessionId is nullable and only fills in a session id that was never attached.
    void completeOrder(UUID orderDomainId, String providerSessionId);

    // Releases the hold by marking the order EXPIRED -- a safe no-op if the order isn't
    // still PENDING, same reasoning as completeOrder.
    void expireOrder(UUID orderDomainId);

    void recordRefundOutcome(
            UUID orderDomainId, RefundStatusEnum refundStatus, String providerRefundId);

    void recordRefundAttemptFailed(UUID orderDomainId);
}
