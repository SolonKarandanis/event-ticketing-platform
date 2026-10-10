package com.etp.ticketservice.payments;

import com.etp.ticketservice.user.User;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Objects;

// Deliberately NOT shaped like every other entity in this codebase -- no domainId/
// @NaturalId, since this is purely internal bookkeeping (the provider-customer mapping),
// never exposed via any API/DTO. No updatedAt either: this row is written once (by
// PaymentCustomerServiceImpl.saveIfAbsent) and never touched again.
@Entity
@Table(name = "payment_customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCustomer {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "paymentCustomerGenerator"
    )
    @SequenceGenerator(
            name = "paymentCustomerGenerator",
            sequenceName = "payment_customers_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    // Read-only mirror of user_id -- lets callers read the FK without touching the lazy
    // association itself. insertable/updatable=false: user above is the only thing that
    // ever writes this column.
    @Setter(AccessLevel.NONE)
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "provider", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(name = "provider_customer_id", nullable = false, updatable = false)
    private String providerCustomerId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaymentCustomer that = (PaymentCustomer) o;
        return Objects.equals(id, that.id) &&
               provider == that.provider &&
               Objects.equals(providerCustomerId, that.providerCustomerId) &&
               Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, providerCustomerId, createdAt);
    }
}
