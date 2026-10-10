package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.payments.exception.InvalidWebhookSignatureException;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stripe.Stripe;
import com.etp.ticketservice.payments.exception.PaymentMethodNotFoundException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.Refund;
import com.stripe.model.SetupIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    @Override
    public String createCustomer(CreateCustomerRequest request) {
        try {
            Customer customer = Customer.create(CustomerCreateParams.builder()
                    .setEmail(request.getEmail())
                    .setName(request.getName())
                    .build());
            return customer.getId();
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    @Override
    public String createSetupIntent(String providerCustomerId) {
        try {
            SetupIntent setupIntent = SetupIntent.create(SetupIntentCreateParams.builder()
                    .setCustomer(providerCustomerId)
                    // OFF_SESSION -- this card is being saved for a future checkout, not
                    // confirming a payment right now.
                    .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
                    .build());
            return setupIntent.getClientSecret();
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    @Override
    public List<SavedPaymentMethodResult> listPaymentMethods(String providerCustomerId) {
        try {
            PaymentMethodCollection collection = PaymentMethod.list(PaymentMethodListParams.builder()
                    .setCustomer(providerCustomerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build());
            String defaultPaymentMethodId = Customer.retrieve(providerCustomerId)
                    .getInvoiceSettings()
                    .getDefaultPaymentMethod();
            return collection.getData().stream()
                    .map(paymentMethod -> SavedPaymentMethodResult.builder()
                            .providerPaymentMethodId(paymentMethod.getId())
                            .brand(paymentMethod.getCard().getBrand())
                            .last4(paymentMethod.getCard().getLast4())
                            .expMonth(paymentMethod.getCard().getExpMonth())
                            .expYear(paymentMethod.getCard().getExpYear())
                            .isDefault(paymentMethod.getId().equals(defaultPaymentMethodId))
                            .build())
                    .toList();
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    @Override
    public void detachPaymentMethod(String expectedProviderCustomerId, String providerPaymentMethodId) {
        PaymentMethod paymentMethod = retrieveOwnedPaymentMethod(expectedProviderCustomerId, providerPaymentMethodId);
        try {
            paymentMethod.detach();
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    @Override
    public void setDefaultPaymentMethod(String expectedProviderCustomerId, String providerPaymentMethodId) {
        retrieveOwnedPaymentMethod(expectedProviderCustomerId, providerPaymentMethodId);
        try {
            Customer customer = Customer.retrieve(expectedProviderCustomerId);
            customer.update(CustomerUpdateParams.builder()
                    .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                            .setDefaultPaymentMethod(providerPaymentMethodId)
                            .build())
                    .build());
        } catch (StripeException e) {
            throw new PaymentGatewayException(ErrorCode.PAYMENT_GATEWAY_ERROR, e);
        }
    }

    // Security-critical: without this check, any authenticated user could pass an
    // arbitrary Stripe payment-method id belonging to a DIFFERENT customer and
    // detach/default someone else's card. Lives here rather than one layer up in
    // PaymentMethodServiceImpl because this package already owns all raw-Stripe-shape
    // knowledge -- nothing above it ever sees a com.stripe.model.* type, and this makes
    // the gateway method self-defending regardless of caller. A genuinely nonexistent id
    // and a wrong-owner id deliberately collapse into the same exception: returning a
    // different error for "exists but isn't yours" vs "doesn't exist" would leak
    // information about other customers' payment methods.
    private PaymentMethod retrieveOwnedPaymentMethod(String expectedProviderCustomerId, String providerPaymentMethodId) {
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.retrieve(providerPaymentMethodId);
        } catch (StripeException e) {
            throw new PaymentMethodNotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND, providerPaymentMethodId);
        }
        if (!expectedProviderCustomerId.equals(paymentMethod.getCustomer())) {
            throw new PaymentMethodNotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND, providerPaymentMethodId);
        }
        return paymentMethod;
    }
}
