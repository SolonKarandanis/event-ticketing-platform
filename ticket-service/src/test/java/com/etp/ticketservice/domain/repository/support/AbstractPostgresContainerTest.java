package com.etp.ticketservice.domain.repository.support;

import com.etp.ticketservice.config.JpaConfiguration;
import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.entity.EventImage;
import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.entity.TicketType;
import com.etp.ticketservice.domain.entity.User;
import com.etp.ticketservice.domain.entity.Venue;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

// Shared by every @DataJpaTest repository slice -- extend this instead of declaring your
// own container. POSTGRES is a static field on THIS class, so every subclass shares the
// exact same field (not a fresh one each) -- Testcontainers' documented "singleton
// container" pattern: it starts once for the whole JVM and is left running for Ryuk /
// JVM shutdown to reap.
//
// Image pinned to postgis/postgis:14-3.5 to match the real dev database exactly
// (confirmed via `SELECT version()` / `SELECT extversion FROM pg_extension WHERE
// extname = 'postgis'` against it: PostgreSQL 14.18, PostGIS 3.5.3) -- plain postgres
// images don't have PostGIS at all, and EventRepository/VenueRepository's native
// queries (ST_DWithin, ST_MakePoint, ST_Distance, the venues.location geography column)
// would just fail against one. asCompatibleSubstituteFor("postgres") is required
// because PostgreSQLContainer normally refuses any image not literally named
// "postgres" -- it tells the container the postgis image is still wire-compatible.
//
// The init script runs CREATE EXTENSION postgis before Spring/Liquibase ever connects --
// see its own comment for why that has to happen this early.
//
// No @Container/@Testcontainers here -- deliberately. Debugging a hang (see the static
// block's comment) showed the @Testcontainers JUnit5 extension re-running this SAME
// static field's start-up sequence (init script and all) a second time shortly after a
// perfectly healthy first container was already serving Liquibase, immediately followed
// by `docker kill` on that first container and a replacement being created -- something
// about a static field shared across multiple concrete subclasses isn't being recognized
// as "already started, skip" the way the singleton-container pattern is supposed to
// guarantee, at least in this Testcontainers 2.0.5 + Spring Boot 4.1.0 combination.
// Whichever @DataJpaTest class's context had already captured the first container's
// port before the kill was left permanently wired to a dead connection. Managing the
// container's whole lifecycle ourselves (see the static block below) removes that
// extension entirely from the picture. @ServiceConnection alone is enough for Spring to
// still pick up this field's connection details -- it's Spring's own test-context
// customizer, independent of @Container/@Testcontainers, and only needs the field's
// current value at property-resolution time. Ryuk registration (JVM-exit cleanup) still
// happens automatically inside start() itself, regardless of which caller invokes it.
//
// @Import(JpaConfiguration.class): @DataJpaTest excludes regular @Configuration beans
// from its slice by default, same as @WebMvcTest does for @Component/@Service beans --
// without this, the real app's @EnableJpaAuditing config never loads, and every
// @CreatedDate/@LastModifiedDate column (all NOT NULL) comes back null, failing the
// insert outright. Importing the app's own config class here instead of redeclaring
// @EnableJpaAuditing locally keeps this test suite honest about what the real app
// actually wires up.
//
// Also carries protected fixture-builder methods below -- every repository test needs
// some slice of the same entity graph (Venue -> Event -> TicketType -> Ticket -> QrCode),
// so those live here once rather than being redeclared per test class.
@Import(JpaConfiguration.class)
public abstract class AbstractPostgresContainerTest {

    // withStartupTimeout is generous: the postgis image restarts itself once during
    // first-run init (a temporary server to run initdb + our init script, then the real
    // one bound to the TCP port), and CREATE EXTENSION postgis adds to that window.
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:14-3.5").asCompatibleSubstituteFor("postgres"))
            .withInitScript("testcontainers/init-postgis.sql")
            .withStartupTimeout(Duration.ofMinutes(3));

    // Starts POSTGRES exactly once, deterministically, here -- JLS class-initialization
    // semantics guarantee this block fully completes before any other code, including
    // Spring's @ServiceConnection property resolution, can observe this field at all
    // (reading a static field first forces its declaring class to finish initializing).
    // No JUnit5 extension is involved in starting it any more (see the class comment).
    static {
        POSTGRES.start();
    }

    @Autowired
    protected TestEntityManager entityManager;

    protected Venue persistVenue(String name, String city, Double latitude, Double longitude) {
        Venue venue = Venue.builder()
                .domainId(UUID.randomUUID())
                .name(name)
                .addressLine1("1 Main St")
                .city(city)
                .postalCode("12345")
                .country("Testland")
                .build();
        venue.setCoordinates(latitude, longitude);
        return entityManager.persistAndFlush(venue);
    }

    protected User persistUser(String name) {
        return entityManager.persistAndFlush(User.builder()
                .domainId(UUID.randomUUID())
                .name(name)
                .email(UUID.randomUUID() + "@example.com")
                .build());
    }

    protected Event persistEvent(String name, Venue venue, User organizer, EventStatusEnum status, LocalDateTime start) {
        return entityManager.persistAndFlush(Event.builder()
                .domainId(UUID.randomUUID())
                .name(name)
                .venue(venue)
                .organizer(organizer)
                .status(status)
                .start(start)
                .build());
    }

    // Convenience overload for tests that don't care about the ticket type itself, just
    // that the event has one -- EventRepository's published-events price sort needs
    // every event priced, for instance.
    protected Event persistEvent(String name, Venue venue, User organizer, EventStatusEnum status,
            LocalDateTime start, double ticketPrice) {
        Event event = persistEvent(name, venue, organizer, status, start);
        persistTicketType(event, "General", ticketPrice);
        return event;
    }

    protected TicketType persistTicketType(Event event, String name, double price) {
        return entityManager.persistAndFlush(TicketType.builder()
                .domainId(UUID.randomUUID())
                .name(name)
                .price(price)
                .event(event)
                .build());
    }

    protected Ticket persistTicket(TicketType ticketType, User purchaser, TicketStatusEnum status) {
        return entityManager.persistAndFlush(Ticket.builder()
                .domainId(UUID.randomUUID())
                .referenceCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(status)
                .ticketType(ticketType)
                .purchaser(purchaser)
                .build());
    }

    protected QrCode persistQrCode(Ticket ticket, QrCodeStatusEnum status) {
        return entityManager.persistAndFlush(QrCode.builder()
                .domainId(UUID.randomUUID())
                .status(status)
                .value("qr-value-" + UUID.randomUUID())
                .ticket(ticket)
                .build());
    }

    protected EventImage persistEventImage(Event event, int position, String altText) {
        return entityManager.persistAndFlush(EventImage.builder()
                .domainId(UUID.randomUUID())
                .event(event)
                .position(position)
                .altText(altText)
                .build());
    }
}
