package com.etp.ticketservice.payments;

import com.etp.ticketservice.payments.exception.InvalidWebhookSignatureException;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// verifyWebhookSignature is fully unit-testable without any network call or static-SDK
// mocking: sign a real payload with Webhook.Signature.generateSignatureHeader (the
// Stripe SDK's own helper, explicitly documented for exactly this -- signing payloads in
// unit tests), then assert on the extraction. createCheckoutSession, refund, and the
// saved-payment-methods methods (createCustomer/createSetupIntent/listPaymentMethods/
// detachPaymentMethod/setDefaultPaymentMethod) have no equivalent test -- all are
// unmockable static Stripe SDK calls (Session.create(...), Session.retrieve(...)/
// Refund.create(...), Customer.create(...)/.retrieve(...)/.update(...),
// SetupIntent.create(...), PaymentMethod.list(...)/.retrieve(...)/.detach(...)) and this
// project has no static-mocking dependency (no mockito-inline/PowerMock) to intercept
// them -- so this class fills a real gap rather than duplicating coverage.
class StripePaymentGatewayServiceTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    private final StripePaymentGatewayService service =
            new StripePaymentGatewayService("sk_test_fake", "usd", WEBHOOK_SECRET);

    @Test
    void verifyWebhookSignature_validSignature_extractsOrderDomainIdFromClientReferenceId() throws Exception {
        UUID orderDomainId = UUID.randomUUID();
        String payload = checkoutSessionEventPayload("evt_123", "checkout.session.completed", orderDomainId.toString());
        String signature = Webhook.Signature.generateSignatureHeader(payload, WEBHOOK_SECRET);

        WebhookEvent event = service.verifyWebhookSignature(payload, signature);

        assertThat(event.getProviderEventId()).isEqualTo("evt_123");
        assertThat(event.getEventType()).isEqualTo("checkout.session.completed");
        assertThat(event.getOrderDomainId()).isEqualTo(orderDomainId);
    }

    @Test
    void verifyWebhookSignature_missingClientReferenceId_orderDomainIdIsNull() throws Exception {
        String payload = checkoutSessionEventPayload("evt_456", "checkout.session.expired", null);
        String signature = Webhook.Signature.generateSignatureHeader(payload, WEBHOOK_SECRET);

        WebhookEvent event = service.verifyWebhookSignature(payload, signature);

        assertThat(event.getOrderDomainId()).isNull();
    }

    @Test
    void verifyWebhookSignature_nonUuidClientReferenceId_orderDomainIdIsNull() throws Exception {
        String payload = """
                {
                  "id": "evt_789",
                  "object": "event",
                  "type": "checkout.session.completed",
                  "data": { "object": { "id": "cs_test_123", "object": "checkout.session", "client_reference_id": "not-a-uuid" } }
                }
                """;
        String signature = Webhook.Signature.generateSignatureHeader(payload, WEBHOOK_SECRET);

        WebhookEvent event = service.verifyWebhookSignature(payload, signature);

        assertThat(event.getOrderDomainId()).isNull();
    }

    @Test
    void verifyWebhookSignature_badSignature_throwsInvalidWebhookSignature() {
        String payload = checkoutSessionEventPayload("evt_123", "checkout.session.completed", UUID.randomUUID().toString());

        assertThatThrownBy(() -> service.verifyWebhookSignature(payload, "t=123,v1=garbled"))
                .isInstanceOf(InvalidWebhookSignatureException.class);
    }

    private String checkoutSessionEventPayload(String eventId, String eventType, String clientReferenceId) {
        String clientReferenceIdJson = clientReferenceId == null ? "null" : "\"" + clientReferenceId + "\"";
        return """
                {
                  "id": "%s",
                  "object": "event",
                  "type": "%s",
                  "data": { "object": { "id": "cs_test_123", "object": "checkout.session", "client_reference_id": %s } }
                }
                """.formatted(eventId, eventType, clientReferenceIdJson);
    }
}
