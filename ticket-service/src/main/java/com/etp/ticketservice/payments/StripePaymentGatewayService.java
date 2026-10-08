package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.payments.exception.PaymentGatewayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class StripePaymentGatewayService implements PaymentGatewayService {

    // One minute of slack over Stripe's own 30-minute expires_at floor -- computed
    // independently at call time (see createCheckoutSession below), not copied from our
    // DB hold's expiresAt, since that value was computed earlier (at TX1-commit time) and
    // any latency between then and this call would otherwise risk landing under Stripe's
    // minimum and getting the request rejected outright.
    private static final long SESSION_EXPIRY_MINUTES = 31;

    private final String currency;

    public StripePaymentGatewayService(
            @Value("${stripe.api-key}") String apiKey,
            @Value("${app.checkout.currency}") String currency) {
        Stripe.apiKey = apiKey;
        this.currency = currency;
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
                            .setUnitAmount(Math.round(item.getUnitAmount() * 100))
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
}
