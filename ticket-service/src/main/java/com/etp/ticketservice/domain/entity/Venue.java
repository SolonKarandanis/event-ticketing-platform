package com.etp.ticketservice.domain.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "venueGenerator"
    )
    @SequenceGenerator(
            name = "venueGenerator",
            sequenceName = "venues_seq",
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

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "accessibility_info")
    private String accessibilityInfo;

    @OneToMany(mappedBy = "venue")
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Venue venue = (Venue) o;
        return Objects.equals(id, venue.id) &&
               Objects.equals(domainId, venue.domainId) &&
               Objects.equals(name, venue.name) &&
               Objects.equals(addressLine1, venue.addressLine1) &&
               Objects.equals(addressLine2, venue.addressLine2) &&
               Objects.equals(city, venue.city) &&
               Objects.equals(postalCode, venue.postalCode) &&
               Objects.equals(country, venue.country) &&
               Objects.equals(latitude, venue.latitude) &&
               Objects.equals(longitude, venue.longitude) &&
               Objects.equals(capacity, venue.capacity) &&
               Objects.equals(accessibilityInfo, venue.accessibilityInfo) &&
               Objects.equals(createdAt, venue.createdAt) &&
               Objects.equals(updatedAt, venue.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, name, addressLine1, addressLine2, city,
                postalCode, country, latitude, longitude, capacity, accessibilityInfo,
                createdAt, updatedAt);
    }
}
