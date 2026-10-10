package com.etp.ticketservice.payments;

import com.etp.ticketservice.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCustomerServiceImplTest {

    @Mock
    private PaymentCustomerRepository paymentCustomerRepository;

    @InjectMocks
    private PaymentCustomerServiceImpl paymentCustomerService;

    private static final UUID USER_DOMAIN_ID = UUID.randomUUID();

    @Test
    void saveIfAbsent_cleanSave_returnsSavedRow() {
        User user = new User();
        user.setDomainId(USER_DOMAIN_ID);
        when(paymentCustomerRepository.saveAndFlush(any(PaymentCustomer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCustomer result = paymentCustomerService.saveIfAbsent(user, PaymentProvider.STRIPE, "cus_test_123");

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getProvider()).isEqualTo(PaymentProvider.STRIPE);
        assertThat(result.getProviderCustomerId()).isEqualTo("cus_test_123");
    }

    @Test
    void saveIfAbsent_uniqueConstraintViolation_returnsConcurrentWinner() {
        User user = new User();
        user.setDomainId(USER_DOMAIN_ID);
        when(paymentCustomerRepository.saveAndFlush(any(PaymentCustomer.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        PaymentCustomer winner = new PaymentCustomer();
        winner.setProviderCustomerId("cus_winner_456");
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_DOMAIN_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.of(winner));

        PaymentCustomer result = paymentCustomerService.saveIfAbsent(user, PaymentProvider.STRIPE, "cus_test_123");

        assertThat(result.getProviderCustomerId()).isEqualTo("cus_winner_456");
    }

    @Test
    void saveIfAbsent_conflictThenVanished_throwsIllegalState() {
        User user = new User();
        user.setDomainId(USER_DOMAIN_ID);
        when(paymentCustomerRepository.saveAndFlush(any(PaymentCustomer.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));
        when(paymentCustomerRepository.findByUserDomainIdAndProvider(USER_DOMAIN_ID, PaymentProvider.STRIPE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentCustomerService.saveIfAbsent(user, PaymentProvider.STRIPE, "cus_test_123"))
                .isInstanceOf(IllegalStateException.class);
    }
}
