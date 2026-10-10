package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.payments.exception.InvalidWebhookSignatureException;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class StripePaymentGatewayService implements PaymentGatewayService {

    // One minute of slack over Stripe's own 30-minute expires_at floor -- computed
    // independently at call time (see createCheckoutSession below), not copied from our
    // DB hold's expiresAt, since that value was computed earlier (at TX1-commit time) and
    // any latency between then and this call would otherwise risk landing under Stripe's
    // minimum and getting the request rejected outright.
    private static final long SESSION_EXPIRY_MINUTES = 31;

    private final String currency;
    private final String webhookSecret;

    public StripePaymentGatewayService(
            @Value("${stripe.api-key}") String apiKey,
            @Value("${app.checkout.currency}") String currency,
            @Value("${stripe.webhook-secret}") String webhookSecret) {
        Stripe.apiKey = apiKey;
        this.currency = currency;
        this.webhookSecret = webhookSecret;
    }

    @Override
    public CheckoutSessionResult createCheckoutSession(CheckoutSessionRequest request) {
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.getSuccessUrl())
                .setCancelUrl(request.getCancelUrl())
                .setClientReferenceId(request.getOrderDomainId().toString())
                .putMetadata("ticketOrderDomainId", request.getOrderDomainId().toString())
                .setExpiresAt(Instant.now().plus(SESSION_EXPIRY_MINUTES, ChronoUnit.MINUTES).getEpochSecond());

        // No pre-created Stripe Products/Prices -- ticket type prices are dynamic and
        // instance-specific, so each line item carries its own PriceData instead.
        for (CheckoutLineItem item : request.getLineItems()) {
            builder.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(item.getQuantity().longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            // item.getUnitAmount() is already minor units (see #23) --
                            // no conversion here. Multiplying by 100 again would
                            // silently overcharge every checkout 100x.
                            .setUnitAmount(item.getUnitAmount())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getName())
                                    .build())
                            .build())
                    .build());
        }

        try {
            Session session = Session.create(builder.build());
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    @Override
    public WebhookEvent verifyWebhookSignature(String payload, String signatureHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // A bad/forged/misconfigured inbound request -- the opposite direction from
            // createCheckoutSession's PAYMENT_GATEWAY_ERROR above (an outbound call to
            // Stripe that failed), so it gets its own error code rather than reusing that one.
            throw new InvalidWebhookSignatureException(ErrorCode.INVALID_WEBHOOK_SIGNATURE, e);
        }

        return WebhookEvent.builder()
                .providerEventId(event.getId())
                .eventType(event.getType())
                .orderDomainId(extractOrderDomainId(event))
                .build();
    }

    // Deliberately reads client_reference_id off the event's raw JSON rather than via
    // event.getDataObjectDeserializer().getObject() (typed deserialization into a
    // Session) -- that typed path silently returns Optional.empty() if the webhook
    // endpoint's configured Stripe API version (a dashboard setting, not source
    // controlled) ever drifts from this SDK's own pinned version, which would make a
    // legitimate checkout.session.completed event silently never complete its order.
    // client_reference_id is a stable, integrator-supplied field that doesn't drift with
    // Stripe's own schema evolution, so reading it straight off the raw JSON sidesteps
    // that whole failure mode. getRawJson() returns the data.object JSON directly (the
    // Session object itself, not wrapped in an outer "object"/"data" key).
    private UUID extractOrderDomainId(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        JsonObject sessionJson = JsonParser.parseString(rawJson).getAsJsonObject();
        JsonElement clientReferenceId = sessionJson.get("client_reference_id");
        if (null == clientReferenceId || clientReferenceId.isJsonNull()) {
            return null;
        }
        try {
            return UUID.fromString(clientReferenceId.getAsString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public RefundResult refund(RefundRequest request) {
        try {
            // Stripe's Refund API takes a PaymentIntent reference, not the Checkout
            // Session id directly -- resolved via one extra retrieve call (see #22).
            Session session = Session.retrieve(request.getProviderCheckoutSessionId());
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .setAmount(request.getAmountMinorUnits())
                    .build();
            Refund refund = Refund.create(params);
            return RefundResult.builder()
                    .providerRefundId(refund.getId())
                    .status(refund.getStatus())
                    .build();
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }
}
