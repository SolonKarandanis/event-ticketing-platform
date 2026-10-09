package com.etp.ticketservice.payments;

public interface StripeWebhookService {
    void handle(String payload, String signatureHeader);
}
