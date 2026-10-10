package com.etp.ticketservice.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetupIntentResponseDto {
    private String clientSecret;
}
