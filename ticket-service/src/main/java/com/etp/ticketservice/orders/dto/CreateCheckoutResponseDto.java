package com.etp.ticketservice.orders.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCheckoutResponseDto {
    private UUID orderId;
    private String checkoutUrl;
    private LocalDateTime expiresAt;
}
