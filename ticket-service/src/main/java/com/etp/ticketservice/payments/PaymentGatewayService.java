package com.etp.ticketservice.payments;

public interface PaymentGatewayService {

    // verifyWebhookSignature/refund are deliberately not declared yet -- they belong to
    // #21 (webhook handling) and #22 (refund flow), added to this interface when each is
    // actually implemented rather than stubbed out ahead of time.
    CheckoutSessionResult createCheckoutSession(CheckoutSessionRequest request);
}
