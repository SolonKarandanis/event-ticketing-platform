package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;

import java.util.UUID;

public interface TicketOrderService {

    TicketOrder reserve(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request);

    TicketOrder attachProviderCheckoutSession(UUID orderDomainId, String providerSessionId);
}
