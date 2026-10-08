package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutSessionResult {
    private String providerSessionId;
    private String redirectUrl;
}
