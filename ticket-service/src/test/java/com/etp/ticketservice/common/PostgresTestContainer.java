package com.etp.ticketservice.common;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

// The singleton Postgres/PostGIS container shared by every integration test that needs a
// real database -- @DataJpaTest repository slices (via AbstractPostgresContainerTest,
// which extends this) and full @SpringBootTest context tests (TicketServiceApplicationTests,
// OpenApiDocsIntegrationTest, which extend this directly) alike. Extracted out of
// AbstractPostgresContainerTest so a full-context test doesn't have to inherit that
// class's @DataJpaTest-specific TestEntityManager/fixture-builder baggage just to get a
// working datasource.
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
// @ServiceConnection on this static field overrides application.properties's own
// spring.datasource.* entirely (which hardcodes a developer-machine LAN address) for any
// test that extends this class, the same way it already does for every @DataJpaTest --
// full-context tests no longer depend on that address being reachable either.
//
// No @Container/@Testcontainers here -- deliberately. Debugging a hang showed the
// @Testcontainers JUnit5 extension re-running this same static field's start-up sequence
// (init script and all) a second time shortly after a perfectly healthy first container
// was already serving Liquibase, immediately followed by `docker kill` on that first
// container and a replacement being created -- something about a static field shared
// across multiple concrete subclasses isn't being recognized as "already started, skip"
// the way the singleton-container pattern is supposed to guarantee, at least in this
// Testcontainers 2.0.5 + Spring Boot 4.1.0 combination. Whichever test class's context had
// already captured the first container's port before the kill was left permanently wired
// to a dead connection. Managing the container's whole lifecycle ourselves (see the
// static block below) removes that extension entirely from the picture. @ServiceConnection
// alone is enough for Spring to still pick up this field's connection details -- it's
// Spring's own test-context customizer, independent of @Container/@Testcontainers, and
// only needs the field's current value at property-resolution time. Ryuk registration
// (JVM-exit cleanup) still happens automatically inside start() itself, regardless of
// which caller invokes it.
public abstract class PostgresTestContainer {

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
}
