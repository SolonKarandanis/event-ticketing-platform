package com.etp.ticketservice.payments;

import com.etp.ticketservice.payments.dto.PaymentMethodResponseDto;

import java.util.List;
import java.util.UUID;

public interface PaymentMethodService {

    String createSetupIntent(UUID userDomainId);

    List<PaymentMethodResponseDto> listPaymentMethods(UUID userDomainId);

    void removePaymentMethod(UUID userDomainId, String providerPaymentMethodId);

    void setDefaultPaymentMethod(UUID userDomainId, String providerPaymentMethodId);
}
