package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    // Failed gateway calls allowed before a refund is marked failed for manual follow-up.
    // At PendingRefundSweeper's 5-minute interval, roughly an hour of retries.
    public static final int MAX_ATTEMPTS = 12;

    private String providerCheckoutSessionId;
    // Null refunds whatever remains of the whole payment.
    private Long amountMinorUnits;
    // Stable per refund target, so a retry can never pay out twice.
    private String idempotencyKey;
}
