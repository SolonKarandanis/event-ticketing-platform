package com.etp.ticketservice.payments;

import java.util.List;

public interface PaymentGatewayService {

    CheckoutSessionResult createCheckoutSession(CheckoutSessionRequest request);

    WebhookEvent verifyWebhookSignature(String payload, String signatureHeader);

    RefundResult refund(RefundRequest request);

    String createCustomer(CreateCustomerRequest request);

    String createSetupIntent(String providerCustomerId);

    List<SavedPaymentMethodResult> listPaymentMethods(String providerCustomerId);

    void detachPaymentMethod(String expectedProviderCustomerId, String providerPaymentMethodId);

    void setDefaultPaymentMethod(String expectedProviderCustomerId, String providerPaymentMethodId);
}
