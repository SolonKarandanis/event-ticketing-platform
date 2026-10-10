package com.etp.ticketservice.payments;

public interface PaymentGatewayService {

    CheckoutSessionResult createCheckoutSession(CheckoutSessionRequest request);

    WebhookEvent verifyWebhookSignature(String payload, String signatureHeader);

    RefundResult refund(RefundRequest request);
}
