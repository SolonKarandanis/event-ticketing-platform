package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.repository.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// A real-Postgres slice test, not a mocked one -- unlike the @WebMvcTest controller
// slices, there's no meaningful way to fake what's under test here: EventRepository's
// riskiest methods are native SQL (PostgreSQL full-text search, MIN-price subquery
// ordering, PostGIS ST_DWithin/ST_Distance), none of which a mock or an in-memory H2
// database would actually exercise. @AutoConfigureTestDatabase(replace = NONE) stops
// @DataJpaTest from swapping in its default embedded database; @ServiceConnection (on
// AbstractPostgresContainerTest's POSTGRES field) points it at the real Testcontainers
// instance instead. Liquibase still runs its normal migrations against that container --
// nothing here hand-rolls the schema.
//
// Each test method runs in its own transaction, rolled back afterward (@DataJpaTest's
// default) -- fixtures created in one test are never visible to another, so no shared
// setup/teardown is needed.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void findByOrganizerDomainId_fetchJoinsVenueEagerly() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        persistEvent("Test Event", venue, organizer, EventStatusEnum.DRAFT, LocalDateTime.now().plusDays(1), 10.0);

        // Clearing the persistence context before the query under test forces a fresh
        // load from the DB -- without LEFT JOIN FETCH e.venue, reading event.getVenue()
        // afterward would throw LazyInitializationException, since there'd be no open
        // session left for it to lazily resolve against once this test method returns
        // the result.
        entityManager.clear();

        Page<Event> page = eventRepository.findByOrganizerDomainId(organizer.getDomainId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getVenue().getName()).isEqualTo("Main Hall");
    }

    @Test
    void findByDomainIdAndOrganizerDomainId_returnsEmpty_whenOwnedByDifferentOrganizer() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User owner = persistUser("Jane Organizer");
        User someoneElse = persistUser("Someone Else");
        Event event = persistEvent("Test Event", venue, owner, EventStatusEnum.DRAFT, LocalDateTime.now().plusDays(1), 10.0);

        Optional<Event> asOwner = eventRepository.findByDomainIdAndOrganizerDomainId(
                event.getDomainId(), owner.getDomainId());
        Optional<Event> asSomeoneElse = eventRepository.findByDomainIdAndOrganizerDomainId(
                event.getDomainId(), someoneElse.getDomainId());

        assertThat(asOwner).isPresent();
        assertThat(asSomeoneElse).isEmpty();
    }

    @Test
    void findPublishedEventsSortedBySoonest_filtersByFullTextSearchAndOrdersSoonestFirst() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        LocalDateTime now = LocalDateTime.now();

        persistEvent("Music in the Park", venue, organizer, EventStatusEnum.PUBLISHED, now.plusDays(5), 10.0);
        persistEvent("Summer Music Festival", venue, organizer, EventStatusEnum.PUBLISHED, now.plusDays(20), 10.0);
        // Neither a status mismatch nor a name mismatch alone would prove the WHERE
        // clause combines both correctly -- these two catch the query matching on the
        // wrong condition and still passing.
        persistEvent("Winter Gala", venue, organizer, EventStatusEnum.PUBLISHED, now.plusDays(1), 10.0);
        persistEvent("Music Rehearsal", venue, organizer, EventStatusEnum.DRAFT, now.plusDays(1), 10.0);

        Page<Event> page = eventRepository.findPublishedEventsSortedBySoonest(
                "Music", null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Event::getName)
                .containsExactly("Music in the Park", "Summer Music Festival");
    }

    @Test
    void findPublishedEventsSortedByPriceAsc_ordersByCheapestTicketType() {
        Venue venue = persistVenue("Main Hall", "Athens", null, null);
        User organizer = persistUser("Jane Organizer");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        persistEvent("Pricey Gala", venue, organizer, EventStatusEnum.PUBLISHED, start, 50.0);
        persistEvent("Budget Meetup", venue, organizer, EventStatusEnum.PUBLISHED, start, 20.0);
        persistEvent("Mid-Range Show", venue, organizer, EventStatusEnum.PUBLISHED, start, 35.0);

        Page<Event> page = eventRepository.findPublishedEventsSortedByPriceAsc(
                null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Event::getName)
                .containsExactly("Budget Meetup", "Mid-Range Show", "Pricey Gala");
    }

    // The one test in this class that only a real PostGIS-enabled Postgres, not H2 or a
    // mock, could ever catch a regression in: ST_DWithin/ST_Distance against a
    // geography(Point,4326) column. Three venues at increasing distance from a
    // reference point -- one inside the search radius and nearer, one inside and
    // farther, one outside entirely -- prove both the distance ordering and the radius
    // cutoff in one pass.
    @Test
    void findPublishedEventsSortedByDistance_ordersNearestFirstAndExcludesBeyondRadius() {
        double refLatitude = 37.9838;
        double refLongitude = 23.7275;

        Venue nearVenue = persistVenue("Near Venue", "Athens", metersNorth(refLatitude, 500), refLongitude);
        Venue farVenue = persistVenue("Far Venue", "Athens", metersNorth(refLatitude, 3_000), refLongitude);
        Venue outsideVenue = persistVenue("Outside Venue", "Athens", metersNorth(refLatitude, 50_000), refLongitude);
        User organizer = persistUser("Jane Organizer");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        persistEvent("Near Event", nearVenue, organizer, EventStatusEnum.PUBLISHED, start, 10.0);
        persistEvent("Far Event", farVenue, organizer, EventStatusEnum.PUBLISHED, start, 10.0);
        persistEvent("Outside Event", outsideVenue, organizer, EventStatusEnum.PUBLISHED, start, 10.0);

        Page<Event> page = eventRepository.findPublishedEventsSortedByDistance(
                null, null, null, null, null, null, refLatitude, refLongitude, 10_000.0, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Event::getName)
                .containsExactly("Near Event", "Far Event");
    }

    @Test
    void findDistinctPublishedEventCities_excludesUnpublishedAndDeduplicatesCities() {
        Venue athensVenueOne = persistVenue("Hall A", "Athens", null, null);
        Venue athensVenueTwo = persistVenue("Hall B", "Athens", null, null);
        Venue thessalonikiVenue = persistVenue("Hall C", "Thessaloniki", null, null);
        User organizer = persistUser("Jane Organizer");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        persistEvent("Event A", athensVenueOne, organizer, EventStatusEnum.PUBLISHED, start, 10.0);
        persistEvent("Event B", athensVenueTwo, organizer, EventStatusEnum.PUBLISHED, start, 10.0);
        // A published event in Thessaloniki is deliberately absent -- only a DRAFT one --
        // so the city must not appear in the result.
        persistEvent("Event C", thessalonikiVenue, organizer, EventStatusEnum.DRAFT, start, 10.0);

        List<String> cities = eventRepository.findDistinctPublishedEventCities();

        assertThat(cities).containsExactly("Athens");
    }

    private static double metersNorth(double baseLatitude, double meters) {
        return baseLatitude + (meters / 111_320.0);
    }
}
