package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;

import java.util.UUID;

public interface TicketOrderService {

    TicketOrder reserve(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request);

    TicketOrder attachProviderCheckoutSession(UUID orderDomainId, String providerSessionId);

    // Issues every held ticket across the order's items and marks it PAID -- a safe
    // no-op if the order isn't still PENDING (already settled by a race between a
    // redelivered webhook and the scheduled sweep).
    void completeOrder(UUID orderDomainId);

    // Releases the hold by marking the order EXPIRED -- a safe no-op if the order isn't
    // still PENDING, same reasoning as completeOrder.
    void expireOrder(UUID orderDomainId);
}
