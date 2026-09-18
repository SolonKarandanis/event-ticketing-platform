package com.etp.ticketservice.common;

import com.etp.ticketservice.config.JpaConfiguration;
import com.etp.ticketservice.events.Event;
import com.etp.ticketservice.events.images.EventImage;
import com.etp.ticketservice.tickets.qrcode.QrCode;
import com.etp.ticketservice.tickets.Ticket;
import com.etp.ticketservice.tickettypes.TicketType;
import com.etp.ticketservice.user.User;
import com.etp.ticketservice.venues.Venue;
import com.etp.ticketservice.events.EventStatusEnum;
import com.etp.ticketservice.tickets.qrcode.QrCodeStatusEnum;
import com.etp.ticketservice.tickets.TicketStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.UUID;

// Shared by every @DataJpaTest repository slice -- extend this instead of declaring your
// own container. The Postgres/PostGIS container itself, and its singleton start-up
// handling, live on PostgresTestContainer (see its own comment) -- this class adds only
// what's specific to @DataJpaTest slices on top of that.
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
public abstract class AbstractPostgresContainerTest extends PostgresTestContainer {

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
