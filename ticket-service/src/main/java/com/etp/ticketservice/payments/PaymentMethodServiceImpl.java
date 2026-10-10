package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.payments.dto.PaymentMethodResponseDto;
import com.etp.ticketservice.payments.exception.PaymentMethodNotFoundException;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Deliberately NOT @Transactional -- createSetupIntent calls Stripe twice
// (createCustomer, then createSetupIntent) with the one DB write isolated on
// PaymentCustomerServiceImpl's own short transaction in between, so neither Stripe call
// ever runs with a DB transaction held open. Same reasoning as CheckoutServiceImpl.
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final UserRepository userRepository;
    private final PaymentCustomerRepository paymentCustomerRepository;
    private final PaymentCustomerService paymentCustomerService;
    private final PaymentGatewayService paymentGatewayService;

    @Override
    public String createSetupIntent(UUID userDomainId) {
        String providerCustomerId = paymentCustomerRepository
                .findByUserDomainIdAndProvider(userDomainId, PaymentProvider.STRIPE)
                .map(PaymentCustomer::getProviderCustomerId)
                .orElseGet(() -> createAndSaveCustomer(userDomainId));
        return paymentGatewayService.createSetupIntent(providerCustomerId);
    }

    private String createAndSaveCustomer(UUID userDomainId) {
        User user = userRepository.findByDomainId(userDomainId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND, userDomainId));
        String providerCustomerId = paymentGatewayService.createCustomer(
                CreateCustomerRequest.builder().email(user.getEmail()).name(user.getName()).build());
        return paymentCustomerService.saveIfAbsent(user, PaymentProvider.STRIPE, providerCustomerId)
                .getProviderCustomerId();
    }

    @Override
    public List<PaymentMethodResponseDto> listPaymentMethods(UUID userDomainId) {
        return paymentCustomerRepository.findByUserDomainIdAndProvider(userDomainId, PaymentProvider.STRIPE)
                .map(PaymentCustomer::getProviderCustomerId)
                .map(paymentGatewayService::listPaymentMethods)
                .orElseGet(List::of)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public void removePaymentMethod(UUID userDomainId, String providerPaymentMethodId) {
        paymentGatewayService.detachPaymentMethod(
                requireProviderCustomerId(userDomainId, providerPaymentMethodId), providerPaymentMethodId);
    }

    @Override
    public void setDefaultPaymentMethod(UUID userDomainId, String providerPaymentMethodId) {
        paymentGatewayService.setDefaultPaymentMethod(
                requireProviderCustomerId(userDomainId, providerPaymentMethodId), providerPaymentMethodId);
    }

    private String requireProviderCustomerId(UUID userDomainId, String providerPaymentMethodId) {
        return paymentCustomerRepository.findByUserDomainIdAndProvider(userDomainId, PaymentProvider.STRIPE)
                .map(PaymentCustomer::getProviderCustomerId)
                .orElseThrow(() -> new PaymentMethodNotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND, providerPaymentMethodId));
    }

    private PaymentMethodResponseDto toResponseDto(SavedPaymentMethodResult result) {
        return new PaymentMethodResponseDto(
                result.getProviderPaymentMethodId(),
                result.getBrand(),
                result.getLast4(),
                result.getExpMonth(),
                result.getExpYear(),
                result.isDefault());
    }
}
