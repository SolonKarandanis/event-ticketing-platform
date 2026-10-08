package com.etp.ticketservice.orders;

import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.payments.PaymentProvider;
import com.etp.ticketservice.user.User;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ticket_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketOrder {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketOrderGenerator"
    )
    @SequenceGenerator(
            name = "ticketOrderGenerator",
            sequenceName = "ticket_orders_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    // Null until the Stripe call (made outside this entity's own transaction -- see
    // TicketOrderServiceImpl) succeeds; stays null forever if that call fails.
    @Column(name = "provider_checkout_session_id", unique = true)
    private String providerCheckoutSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id", nullable = false)
    private User purchaser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "ticketOrder", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TicketOrderItem> items = new LinkedHashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addItem(TicketOrderItem item) {
        this.items.add(item);
        item.setTicketOrder(this);
    }

    public void removeItem(TicketOrderItem item) {
        this.items.remove(item);
        item.setTicketOrder(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TicketOrder ticketOrder = (TicketOrder) o;
        return Objects.equals(id, ticketOrder.id) &&
               Objects.equals(domainId, ticketOrder.domainId) &&
               status == ticketOrder.status &&
               provider == ticketOrder.provider &&
               Objects.equals(providerCheckoutSessionId, ticketOrder.providerCheckoutSessionId) &&
               Objects.equals(expiresAt, ticketOrder.expiresAt) &&
               Objects.equals(createdAt, ticketOrder.createdAt) &&
               Objects.equals(updatedAt, ticketOrder.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, status, provider, providerCheckoutSessionId,
                expiresAt, createdAt, updatedAt);
    }
}
