package com.etp.ticketservice.payments;

public interface ProcessedWebhookEventService {

    // Returns true if this (provider, providerEventId) pair was just recorded for the
    // first time -- the caller should proceed. Returns false if it was already recorded
    // (a redelivered webhook, or a concurrent delivery racing this one) -- the caller
    // must treat the event as already handled and stop.
    boolean recordIfNew(PaymentProvider provider, String providerEventId, String eventType);
}
