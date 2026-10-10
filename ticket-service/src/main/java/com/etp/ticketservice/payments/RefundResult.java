package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResult {
    private String providerRefundId;
    // Stripe's raw refund status string (e.g. "succeeded"/"pending"/"failed"),
    // untranslated -- same precedent as WebhookEvent.eventType. Translation to this
    // app's own RefundStatusEnum happens one layer up, in TicketServiceImpl, so this
    // package stays ticket-domain-agnostic.
    private String status;
}
