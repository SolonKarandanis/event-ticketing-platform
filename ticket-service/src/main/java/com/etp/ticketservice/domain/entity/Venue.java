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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    // SRID 4326 (WGS 84) matches the location column's geography(Point,4326) type and
    // the 003-add-venue-geography migration's backfill (ST_SetSRID(..., 4326)).
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

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

    // Derived from latitude/longitude, not set directly -- use setCoordinates() so this
    // never drifts out of sync with them. Existing rows were backfilled once by the
    // 003-add-venue-geography Liquibase migration; every write since goes through
    // setCoordinates() instead.
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "accessibility_info")
    private String accessibilityInfo;

    @OneToMany(mappedBy = "venue")
    @Builder.Default
    private Set<Event> events = new LinkedHashSet<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // The one way latitude/longitude/location should be set, in create and update
    // alike -- Point's coordinate order is (x, y), i.e. (longitude, latitude), matching
    // the migration's own ST_MakePoint(longitude, latitude). Either coordinate missing
    // (both are optional, per the venue form's design) means no location at all, not a
    // half-built point.
    public void setCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = (null != latitude && null != longitude)
                ? GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude))
                : null;
    }

    public void addEvent(Event event) {
        this.events.add(event);
        event.setVenue(this);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
        event.setVenue(null);
    }

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
               Objects.equals(location, venue.location) &&
               Objects.equals(capacity, venue.capacity) &&
               Objects.equals(accessibilityInfo, venue.accessibilityInfo) &&
               Objects.equals(createdAt, venue.createdAt) &&
               Objects.equals(updatedAt, venue.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, name, addressLine1, addressLine2, city,
                postalCode, country, latitude, longitude, location, capacity, accessibilityInfo,
                createdAt, updatedAt);
    }
}
