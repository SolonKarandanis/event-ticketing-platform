package com.etp.ticketservice.tickets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancellationOutcome {
    private Ticket ticket;
    // null <=> ticket.getOrderItem() == null -- nothing was ever paid via Stripe for it,
    // so there's nothing to refund.
    private Long refundAmountMinorUnits;
    // May be null even when refundAmountMinorUnits isn't -- see
    // TicketServiceImpl#cancelAndAttemptRefund for how that's handled.
    private String providerCheckoutSessionId;
}
