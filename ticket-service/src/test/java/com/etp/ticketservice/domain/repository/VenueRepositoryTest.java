package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.repository.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Real-Postgres slice test, same rationale as EventRepositoryTest -- search()'s
// CAST(:searchTerm AS string) IS NULL trick and findWithinRadius()'s PostGIS functions
// are exactly the kind of native-SQL behavior a mock or H2 wouldn't actually exercise.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VenueRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private VenueRepository venueRepository;

    @Test
    void findByDomainId_returnsVenue_whenFound() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);

        Optional<Venue> found = venueRepository.findByDomainId(venue.getDomainId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Main Hall");
    }

    @Test
    void findByDomainId_returnsEmpty_whenNotFound() {
        Optional<Venue> found = venueRepository.findByDomainId(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    // A null searchTerm is "browse all" -- this is the case that actually requires the
    // CAST(:searchTerm AS string) fix (see the repository method's own comment): without
    // it, Postgres can't infer a type for the bind parameter and the query fails
    // outright with "function lower(bytea) does not exist", not just returns wrong
    // results, so this is a real regression test, not a redundant one.
    @Test
    void search_withNullSearchTerm_returnsEveryVenue() {
        persistVenue("Main Hall", "Athens", null, null);
        persistVenue("Side Room", "Thessaloniki", null, null);

        Page<Venue> page = venueRepository.search(null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Venue::getName)
                .containsExactlyInAnyOrder("Main Hall", "Side Room");
    }

    @Test
    void search_withSearchTerm_filtersCaseInsensitivelyByName() {
        persistVenue("Main Hall", "Athens", null, null);
        persistVenue("Side Room", "Thessaloniki", null, null);

        Page<Venue> page = venueRepository.search("main", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Venue::getName).containsExactly("Main Hall");
    }

    // Same PostGIS-specific rationale as EventRepositoryTest's distance test: only a
    // real geography-enabled Postgres can catch a regression in ST_DWithin/ST_Distance.
    @Test
    void findWithinRadius_ordersNearestFirstAndExcludesBeyondRadius() {
        double refLatitude = 37.9838;
        double refLongitude = 23.7275;

        persistVenue("Near Venue", "Athens", metersNorth(refLatitude, 500), refLongitude);
        persistVenue("Far Venue", "Athens", metersNorth(refLatitude, 3_000), refLongitude);
        persistVenue("Outside Venue", "Athens", metersNorth(refLatitude, 50_000), refLongitude);
        persistVenue("No Coordinates Venue", "Athens", null, null);

        Page<Venue> page = venueRepository.findWithinRadius(refLatitude, refLongitude, 10_000.0, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Venue::getName)
                .containsExactly("Near Venue", "Far Venue");
    }

    private static double metersNorth(double baseLatitude, double meters) {
        return baseLatitude + (meters / 111_320.0);
    }
}
