package com.etp.ticketservice.payments;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Objects;

// Deliberately NOT shaped like every other entity in this codebase -- no domainId/
// @NaturalId, since this is purely internal bookkeeping, never exposed via any API/DTO.
// No updatedAt either: this row is written exactly once and never touched again, so a
// pair-with-updatedAt would be dead weight here, unlike every other entity.
@Entity
@Table(name = "processed_webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedWebhookEvent {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "processedWebhookEventGenerator"
    )
    @SequenceGenerator(
            name = "processedWebhookEventGenerator",
            sequenceName = "processed_webhook_events_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "provider", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(name = "provider_event_id", nullable = false, updatable = false)
    private String providerEventId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @CreatedDate
    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProcessedWebhookEvent that = (ProcessedWebhookEvent) o;
        return Objects.equals(id, that.id) &&
               provider == that.provider &&
               Objects.equals(providerEventId, that.providerEventId) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(processedAt, that.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, providerEventId, eventType, processedAt);
    }
}
