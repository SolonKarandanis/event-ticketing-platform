package com.etp.ticketservice.payments;

import com.etp.ticketservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Separate bean, separate short transaction -- same "catch the exception across the
// proxy boundary" shape as ProcessedWebhookEventServiceImpl.recordIfNew. Once the INSERT
// below fails on the (user_id, provider) unique constraint, Postgres marks this whole
// transaction aborted; scoping it to a single-statement transaction makes that abort
// exactly correct (there's nothing else in it to lose).
@Service
@RequiredArgsConstructor
public class PaymentCustomerServiceImpl implements PaymentCustomerService {

    private final PaymentCustomerRepository paymentCustomerRepository;

    @Override
    @Transactional
    public PaymentCustomer saveIfAbsent(User user, PaymentProvider provider, String providerCustomerId) {
        PaymentCustomer paymentCustomer = new PaymentCustomer();
        paymentCustomer.setUser(user);
        paymentCustomer.setProvider(provider);
        paymentCustomer.setProviderCustomerId(providerCustomerId);
        try {
            // saveAndFlush, not save -- forces the INSERT (and thus the unique-constraint
            // check) to run synchronously here, so the conflict is catchable in this
            // method rather than deferred to some later flush outside this try/catch.
            return paymentCustomerRepository.saveAndFlush(paymentCustomer);
        } catch (DataIntegrityViolationException ex) {
            // A concurrent request already created one for this user+provider -- the
            // Stripe customer the caller just created above becomes a harmless orphan on
            // Stripe's side; the existing row wins, and its providerCustomerId is just as
            // usable as the one we tried to save.
            return paymentCustomerRepository.findByUserDomainIdAndProvider(user.getDomainId(), provider)
                    .orElseThrow(() -> new IllegalStateException(
                            "PaymentCustomer for " + user.getDomainId() + "/" + provider +
                            " vanished immediately after a unique-constraint conflict"));
        }
    }
}
