package com.etp.ticketservice.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCheckoutResponseDto {
    private UUID orderId;
    private String checkoutUrl;
    private LocalDateTime expiresAt;
}
