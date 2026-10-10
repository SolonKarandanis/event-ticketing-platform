package com.etp.ticketservice.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCustomerRepository extends JpaRepository<PaymentCustomer, Long> {

    @Query("SELECT pc FROM PaymentCustomer pc WHERE pc.user.domainId = :userDomainId AND pc.provider = :provider")
    Optional<PaymentCustomer> findByUserDomainIdAndProvider(
            @Param("userDomainId") UUID userDomainId,
            @Param("provider") PaymentProvider provider);
}
