package com.etp.ticketservice.payments;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
