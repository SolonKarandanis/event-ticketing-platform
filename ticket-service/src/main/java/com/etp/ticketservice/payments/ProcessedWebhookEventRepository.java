package com.etp.ticketservice.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// No custom query -- this is insert-only, and the uniqueness check is the DB's own
// unique constraint on (provider, provider_event_id), not a query here.
@Repository
public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, Long> {
}
