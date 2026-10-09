package com.etp.ticketservice.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {
    private String providerEventId;
    private String eventType;
    // Nullable -- absent for event types we don't act on, or an unparseable/missing
    // client_reference_id.
    private UUID orderDomainId;
}
