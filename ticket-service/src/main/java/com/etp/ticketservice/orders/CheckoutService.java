package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutResponseDto;

import java.util.UUID;

public interface CheckoutService {

    CreateCheckoutResponseDto createCheckout(UUID userId, UUID eventDomainId, CreateCheckoutRequestDto request);
}
