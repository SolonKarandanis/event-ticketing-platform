package com.etp.ticketservice.payments;

import com.etp.ticketservice.user.User;

public interface PaymentCustomerService {

    PaymentCustomer saveIfAbsent(User user, PaymentProvider provider, String providerCustomerId);
}
