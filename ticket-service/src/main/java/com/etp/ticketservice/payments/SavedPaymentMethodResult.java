package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedPaymentMethodResult {
    private String providerPaymentMethodId;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
    private boolean isDefault;
}
