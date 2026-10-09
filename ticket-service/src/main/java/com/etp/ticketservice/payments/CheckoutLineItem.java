package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutLineItem {
    private String name;
    // Minor units (e.g. cents) -- matches Stripe's own "unit_amount" convention
    // directly, so this needs no further conversion anywhere downstream (see #23).
    private Long unitAmount;
    private Integer quantity;
}
