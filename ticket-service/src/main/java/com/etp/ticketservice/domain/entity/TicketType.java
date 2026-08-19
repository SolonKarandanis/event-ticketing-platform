package com.etp.ticketservice.domain.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ticket_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketTypeGenerator"
    )
    @SequenceGenerator(
            name = "ticketTypeGenerator",
            sequenceName = "ticket_types_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "description")
    private String description;

    @Column(name = "total_available")
    private Integer totalAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToMany(mappedBy = "ticketType", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Ticket> tickets = new LinkedHashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
        ticket.setTicketType(this);
    }

    public void removeTicket(Ticket ticket) {
        this.tickets.remove(ticket);
        ticket.setTicketType(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TicketType ticketType = (TicketType) o;
        return Objects.equals(id, ticketType.id) &&
               Objects.equals(domainId, ticketType.domainId) &&
               Objects.equals(name, ticketType.name) &&
               Objects.equals(price, ticketType.price) &&
               Objects.equals(description, ticketType.description) &&
               Objects.equals(totalAvailable, ticketType.totalAvailable) &&
               Objects.equals(createdAt, ticketType.createdAt) &&
               Objects.equals(updatedAt, ticketType.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, name, price, description, totalAvailable,
                createdAt, updatedAt);
    }
}
