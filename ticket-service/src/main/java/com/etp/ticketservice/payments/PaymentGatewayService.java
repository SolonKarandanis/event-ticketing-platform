package com.etp.ticketservice.payments;

public interface PaymentGatewayService {

    // refund is deliberately not declared yet -- it belongs to #22, added to this
    // interface when actually implemented rather than stubbed out ahead of time.
    CheckoutSessionResult createCheckoutSession(CheckoutSessionRequest request);

    WebhookEvent verifyWebhookSignature(String payload, String signatureHeader);
}
