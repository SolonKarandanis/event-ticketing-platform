package com.etp.ticketservice.orders;

import com.etp.ticketservice.tickettypes.TicketType;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ticket_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketOrderItem {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketOrderItemGenerator"
    )
    @SequenceGenerator(
            name = "ticketOrderItemGenerator",
            sequenceName = "ticket_order_items_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    // Named ticketOrder, not order -- avoids ambiguity with the SQL/JPQL ORDER BY
    // keyword, same reasoning as Ticket.ticketType.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_order_id", nullable = false)
    private TicketOrder ticketOrder;

    // Read-only mirror of ticket_order_id -- lets callers read the FK without touching
    // the lazy association itself. insertable/updatable=false: ticketOrder above is the
    // only thing that ever writes this column.
    @Setter(AccessLevel.NONE)
    @Column(name = "ticket_order_id", insertable = false, updatable = false)
    private Long ticketOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @Setter(AccessLevel.NONE)
    @Column(name = "ticket_type_id", insertable = false, updatable = false)
    private Long ticketTypeId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Snapshot of TicketType.price at checkout time -- deliberately not a live
    // reference, so a later organizer price edit can't retroactively change what this
    // cart already locked in.
    @Column(name = "unit_price_at_checkout", nullable = false)
    private Double unitPriceAtCheckout;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TicketOrderItem ticketOrderItem = (TicketOrderItem) o;
        return Objects.equals(id, ticketOrderItem.id) &&
               Objects.equals(domainId, ticketOrderItem.domainId) &&
               Objects.equals(quantity, ticketOrderItem.quantity) &&
               Objects.equals(unitPriceAtCheckout, ticketOrderItem.unitPriceAtCheckout) &&
               Objects.equals(createdAt, ticketOrderItem.createdAt) &&
               Objects.equals(updatedAt, ticketOrderItem.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, quantity, unitPriceAtCheckout, createdAt, updatedAt);
    }
}
