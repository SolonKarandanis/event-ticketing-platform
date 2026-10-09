package com.etp.ticketservice.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Separate bean, separate short transaction -- same "catch the exception across the
// proxy boundary" shape as TicketOrderServiceImpl.reserve/attachProviderCheckoutSession.
// Once the INSERT below fails on the unique constraint, Postgres marks this whole
// transaction aborted; scoping it to a single-statement transaction makes that abort
// exactly correct (there's nothing else in it to lose). Inlining this into a larger
// transaction would silently doom that whole transaction instead.
@Service
@RequiredArgsConstructor
public class ProcessedWebhookEventServiceImpl implements ProcessedWebhookEventService {

    private final ProcessedWebhookEventRepository processedWebhookEventRepository;

    @Override
    @Transactional
    public boolean recordIfNew(PaymentProvider provider, String providerEventId, String eventType) {
        ProcessedWebhookEvent event = new ProcessedWebhookEvent();
        event.setProvider(provider);
        event.setProviderEventId(providerEventId);
        event.setEventType(eventType);
        try {
            // saveAndFlush, not save -- forces the INSERT (and thus the unique-constraint
            // check) to run synchronously here, so the conflict is catchable in this
            // method rather than deferred to some later flush outside this try/catch.
            processedWebhookEventRepository.saveAndFlush(event);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }
}
