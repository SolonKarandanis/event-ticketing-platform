package com.etp.ticketservice.payments;

import com.etp.ticketservice.payments.dto.PaymentMethodResponseDto;
import com.etp.ticketservice.payments.exception.PaymentMethodNotFoundException;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.user.UserNotFoundException;
import com.etp.ticketservice.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pure Mockito unit tests for the orchestration -- see PaymentMethodServiceImpl's own
// class comment for why createSetupIntent sequences two Stripe calls around one
// independent PaymentCustomerService transaction rather than one @Transactional method.
@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentCustomerRepository paymentCustomerRepository;
    @Mock
    private PaymentCustomerService paymentCustomerService;
    @Mock
    private PaymentGatewayService paymentGatewayService;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void createSetupIntent_existingCustomer_skipsCustomerCreation() {
        PaymentCustomer existing = new PaymentCustomer();
        existing.setProviderCustomerId("cus_existing_123");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(existing));
        when(paymentGatewayService.createSetupIntent("cus_existing_123")).thenReturn("seti_secret_123");

        String clientSecret = paymentMethodService.createSetupIntent(USER_ID);

        assertThat(clientSecret).isEqualTo("seti_secret_123");
        verify(paymentGatewayService, never()).createCustomer(any());
        verify(paymentCustomerService, never()).saveIfAbsent(any(), any(), any());
    }

    @Test
    void createSetupIntent_noExistingCustomer_createsAndSavesThenCreatesSetupIntent() {
        User user = new User();
        user.setDomainId(USER_ID);
        user.setEmail("jane@example.com");
        user.setName("Jane Attendee");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.of(user));
        when(paymentGatewayService.createCustomer(any(CreateCustomerRequest.class))).thenReturn("cus_new_456");
        PaymentCustomer saved = new PaymentCustomer();
        saved.setProviderCustomerId("cus_new_456");
        when(paymentCustomerService.saveIfAbsent(user, PaymentProvider.STRIPE, "cus_new_456")).thenReturn(saved);
        when(paymentGatewayService.createSetupIntent("cus_new_456")).thenReturn("seti_secret_456");

        String clientSecret = paymentMethodService.createSetupIntent(USER_ID);

        assertThat(clientSecret).isEqualTo("seti_secret_456");
        verify(paymentGatewayService).createCustomer(CreateCustomerRequest.builder()
                .email("jane@example.com").name("Jane Attendee").build());
    }

    @Test
    void createSetupIntent_userNotFound_throws() {
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());
        when(userRepository.findByDomainId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.createSetupIntent(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listPaymentMethods_noCustomer_returnsEmptyListWithoutGatewayCall() {
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        List<PaymentMethodResponseDto> result = paymentMethodService.listPaymentMethods(USER_ID);

        assertThat(result).isEmpty();
        verify(paymentGatewayService, never()).listPaymentMethods(any());
    }

    @Test
    void listPaymentMethods_existingCustomer_translatesGatewayResults() {
        PaymentCustomer customer = new PaymentCustomer();
        customer.setProviderCustomerId("cus_test_123");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(customer));
        when(paymentGatewayService.listPaymentMethods("cus_test_123")).thenReturn(List.of(
                SavedPaymentMethodResult.builder()
                        .providerPaymentMethodId("pm_123")
                        .brand("visa")
                        .last4("4242")
                        .expMonth(8L)
                        .expYear(2028L)
                        .isDefault(true)
                        .build()));

        List<PaymentMethodResponseDto> result = paymentMethodService.listPaymentMethods(USER_ID);

        assertThat(result).hasSize(1);
        PaymentMethodResponseDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo("pm_123");
        assertThat(dto.getBrand()).isEqualTo("visa");
        assertThat(dto.getLast4()).isEqualTo("4242");
        assertThat(dto.getExpMonth()).isEqualTo(8L);
        assertThat(dto.getExpYear()).isEqualTo(2028L);
        assertThat(dto.isDefault()).isTrue();
    }

    @Test
    void removePaymentMethod_noCustomer_throwsWithoutGatewayCall() {
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.removePaymentMethod(USER_ID, "pm_123"))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(paymentGatewayService, never()).detachPaymentMethod(any(), any());
    }

    @Test
    void removePaymentMethod_existingCustomer_delegatesWithProviderCustomerId() {
        PaymentCustomer customer = new PaymentCustomer();
        customer.setProviderCustomerId("cus_test_123");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(customer));

        paymentMethodService.removePaymentMethod(USER_ID, "pm_123");

        verify(paymentGatewayService).detachPaymentMethod("cus_test_123", "pm_123");
    }

    @Test
    void setDefaultPaymentMethod_noCustomer_throwsWithoutGatewayCall() {
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.setDefaultPaymentMethod(USER_ID, "pm_123"))
                .isInstanceOf(PaymentMethodNotFoundException.class);

        verify(paymentGatewayService, never()).setDefaultPaymentMethod(any(), any());
    }

    @Test
    void setDefaultPaymentMethod_existingCustomer_delegatesWithProviderCustomerId() {
        PaymentCustomer customer = new PaymentCustomer();
        customer.setProviderCustomerId("cus_test_123");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(customer));

        paymentMethodService.setDefaultPaymentMethod(USER_ID, "pm_123");

        verify(paymentGatewayService).setDefaultPaymentMethod("cus_test_123", "pm_123");
    }
}
