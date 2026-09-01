package com.etp.ticketservice.domain.entity;

import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
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
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ticketGenerator"
    )
    @SequenceGenerator(
            name = "ticketGenerator",
            sequenceName = "tickets_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    // Short human-typeable code for the manual ticket-validation fallback -- domainId is a
    // full UUID, impractical for staff to type by hand at a door under time pressure.
    @Column(name = "reference_code", nullable = false, updatable = false, unique = true)
    private String referenceCode;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TicketStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaser_id")
    private User purchaser;

    // Bookkeeping for a cancelled ticket -- no real payment gateway exists (see issue #1),
    // so there's nothing to actually refund; this is the audit trail a future refund
    // process (or reporting) would read. All three are null until cancelTicket sets them
    // together, and never independently.
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason")
    @Enumerated(EnumType.STRING)
    private TicketCancelReasonEnum cancelReason;

    @Column(name = "cancel_note")
    private String cancelNote;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<TicketValidation> validations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<QrCode> qrCodes = new LinkedHashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void addValidation(TicketValidation validation) {
        this.validations.add(validation);
        validation.setTicket(this);
    }

    public void removeValidation(TicketValidation validation) {
        this.validations.remove(validation);
        validation.setTicket(null);
    }

    public void addQrCode(QrCode qrCode) {
        this.qrCodes.add(qrCode);
        qrCode.setTicket(this);
    }

    public void removeQrCode(QrCode qrCode) {
        this.qrCodes.remove(qrCode);
        qrCode.setTicket(null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id) &&
               Objects.equals(domainId, ticket.domainId) &&
               Objects.equals(referenceCode, ticket.referenceCode) &&
               status == ticket.status &&
               Objects.equals(cancelledAt, ticket.cancelledAt) &&
               cancelReason == ticket.cancelReason &&
               Objects.equals(cancelNote, ticket.cancelNote) &&
               Objects.equals(createdAt, ticket.createdAt) &&
               Objects.equals(updatedAt, ticket.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, referenceCode, status, cancelledAt, cancelReason, cancelNote, createdAt, updatedAt);
    }
}
