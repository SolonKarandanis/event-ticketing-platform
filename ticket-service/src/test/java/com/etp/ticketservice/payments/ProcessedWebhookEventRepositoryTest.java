package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Real-Postgres slice test -- the one place proving ProcessedWebhookEventServiceImpl's
// idempotency check actually works end to end: that a duplicate (provider,
// provider_event_id) really does violate a unique constraint, and that Spring's
// exception-translation machinery really does turn that into a
// DataIntegrityViolationException, not just that the JPQL/entity mapping compiles.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProcessedWebhookEventRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private ProcessedWebhookEventRepository processedWebhookEventRepository;

    @Test
    void saveAndFlush_duplicateProviderAndProviderEventId_throwsDataIntegrityViolation() {
        processedWebhookEventRepository.saveAndFlush(newEvent("evt_123"));

        assertThatThrownBy(() -> processedWebhookEventRepository.saveAndFlush(newEvent("evt_123")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private ProcessedWebhookEvent newEvent(String providerEventId) {
        ProcessedWebhookEvent event = new ProcessedWebhookEvent();
        event.setProvider(PaymentProvider.STRIPE);
        event.setProviderEventId(providerEventId);
        event.setEventType("checkout.session.completed");
        return event;
    }
}
