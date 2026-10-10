package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.AbstractPostgresContainerTest;
import com.etp.ticketservice.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Real-Postgres slice test, same rationale as ProcessedWebhookEventRepositoryTest -- the
// one place proving the (user_id, provider) unique constraint really exists and that
// Spring's exception-translation machinery really turns a violation into a
// DataIntegrityViolationException, not just that the JPQL/entity mapping compiles.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentCustomerRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private PaymentCustomerRepository paymentCustomerRepository;

    @Test
    void saveAndFlush_duplicateUserAndProvider_throwsDataIntegrityViolation() {
        User user = persistUser("Jane Attendee");
        paymentCustomerRepository.saveAndFlush(newPaymentCustomer(user, "cus_test_123"));

        assertThatThrownBy(() -> paymentCustomerRepository.saveAndFlush(newPaymentCustomer(user, "cus_test_456")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByUserDomainIdAndProvider_findsThroughTheJoin() {
        User user = persistUser("Jane Attendee");
        paymentCustomerRepository.saveAndFlush(newPaymentCustomer(user, "cus_test_123"));

        Optional<PaymentCustomer> found = paymentCustomerRepository.findByUserDomainIdAndProvider(
                user.getDomainId(), PaymentProvider.STRIPE);

        assertThat(found).isPresent();
        assertThat(found.get().getProviderCustomerId()).isEqualTo("cus_test_123");
    }

    @Test
    void findByUserDomainIdAndProvider_returnsEmpty_whenNoneExists() {
        User user = persistUser("Jane Attendee");

        Optional<PaymentCustomer> found = paymentCustomerRepository.findByUserDomainIdAndProvider(
                user.getDomainId(), PaymentProvider.STRIPE);

        assertThat(found).isEmpty();
    }

    private PaymentCustomer newPaymentCustomer(User user, String providerCustomerId) {
        PaymentCustomer paymentCustomer = new PaymentCustomer();
        paymentCustomer.setUser(user);
        paymentCustomer.setProvider(PaymentProvider.STRIPE);
        paymentCustomer.setProviderCustomerId(providerCustomerId);
        return paymentCustomer;
    }
}
