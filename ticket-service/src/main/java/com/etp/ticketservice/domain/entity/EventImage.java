package com.etp.ticketservice.domain.entity;

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
@Table(name = "event_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "eventImageGenerator"
    )
    @SequenceGenerator(
            name = "eventImageGenerator",
            sequenceName = "event_images_seq",
            allocationSize = 1,
            initialValue = 1
    )
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    // Also the filename on disk -- every stored file is "{domainId}.jpg", since every
    // upload is re-encoded to JPEG on the way in (see EventImageServiceImpl). No separate
    // filename/content-type column needed as a result.
    @NaturalId
    @Column(name = "domain_id", nullable = false, updatable = false, unique = true)
    private UUID domainId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Gallery display order -- 0 is the cover image shown on the /browse card. Set from
    // the submitted images list's array order on every create/update, not from a
    // dedicated reorder endpoint (see EventServiceImpl#updateEventForOrganizer).
    @Column(name = "position", nullable = false)
    private Integer position;

    // Organizer-supplied, optional -- null/blank means the frontend falls back to a
    // computed alt (event name, or "event name -- photo N") rather than anything stored
    // here. Never auto-derived from the original filename (that's discarded entirely).
    @Column(name = "alt_text")
    private String altText;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        EventImage that = (EventImage) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(domainId, that.domainId) &&
               Objects.equals(position, that.position) &&
               Objects.equals(altText, that.altText) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, position, altText, createdAt, updatedAt);
    }
}
