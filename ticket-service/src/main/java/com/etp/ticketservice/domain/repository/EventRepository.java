package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // venue is a @ManyToOne (to-one) fetch join -- safe to combine with Pageable,
    // it doesn't multiply result rows the way a @OneToMany fetch join would.
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.venue WHERE e.organizer.domainId = :organizerDomainId")
    Page<Event> findByOrganizerDomainId(@Param("organizerDomainId") UUID organizerDomainId, Pageable pageable);

    // Second step of the "paginate, then hydrate collections" pattern: run in the same
    // transaction as findByOrganizerDomainId so this fetch-joins ticketTypes onto the
    // already-managed Event instances from that page, without pagination+collection-fetch-join
    // ever occurring on the same query.
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.ticketTypes WHERE e.id IN :ids")
    List<Event> findByIdInWithTicketTypes(@Param("ids") Collection<Long> ids);

    // ticketTypes and images are both @OneToMany, but both Set-backed -- combining two
    // to-many fetch joins in one query is exactly what that Set-over-List choice (see
    // "Relationship Collections: Set, Not List") avoids MultipleBagFetchException for.
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes LEFT JOIN FETCH e.images WHERE e.domainId = :domainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Event> findByDomainIdAndOrganizerDomainId(@Param("domainId") UUID domainId, @Param("organizerDomainId") UUID organizerDomainId);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes LEFT JOIN FETCH e.images WHERE e.domainId = :domainId AND e.status = :status")
    Optional<Event> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") EventStatusEnum status);

    // Published-events browse/search, filtering, and sort all live here as native queries --
    // PostgreSQL full-text search (to_tsvector/plainto_tsquery) has no JPQL/Criteria
    // equivalent, so the whole thing stays native rather than splitting filtering into JPQL
    // and search into native SQL. Every filter uses the "(:param IS NULL OR ...)" pattern so
    // a null/absent param is a no-op -- searchTerm now covers both "browse all" (null) and
    // "search" (set), replacing the old separate findByStatus/searchEvents split.
    //
    // Spring Data does NOT inject an ORDER BY from Pageable.getSort() into native queries --
    // it silently no-ops rather than erroring, so there are three near-identical variants
    // below (one per sort option) instead of one dynamic query with a Pageable-driven sort.
    // JOIN FETCH isn't expressible in native SQL either -- see EventServiceImpl#findPublishedEvents
    // for how venue gets hydrated instead. minPrice/maxPrice match an event's cheapest ticket
    // type ("starting from" price), not "any ticket type in range".
    // Every :param below is wrapped in CAST(... AS <type>), including the "IS NULL" side --
    // same root cause and same fix as VenueRepository.search (see its comment): a
    // parameter used only in an "IS NULL" comparison gives Postgres zero type context to
    // infer from, and it either guesses wrong (silently, in the venue case) or -- as
    // happened here -- refuses outright with "could not determine data type of parameter".
    // Native query, so this is plain PostgreSQL CAST syntax, not Hibernate's JPQL one;
    // each :name is its own independent positional parameter per occurrence, so every
    // occurrence needs its own cast, not just the one that happened to fail first.
    // latitude/longitude/radiusMeters follow the same "all three null -> no-op" shape as
    // every other optional filter here, just spread across three co-dependent params
    // instead of one -- if any of the three is absent, the OR chain short-circuits true
    // and no venue gets excluded on distance grounds. No explicit "v.location IS NOT
    // NULL" guard is needed: ST_DWithin against a null geography evaluates to NULL, and
    // a NULL in a WHERE clause is already treated as non-matching, so a venue with no
    // coordinates is excluded by the same expression that includes nearby ones.
    String PUBLISHED_EVENTS_WHERE = "e.status = 'PUBLISHED' " +
            "AND (CAST(:searchTerm AS text) IS NULL OR to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) @@ plainto_tsquery('english', CAST(:searchTerm AS text))) " +
            "AND (CAST(:city AS text) IS NULL OR v.city = CAST(:city AS text)) " +
            "AND (CAST(:from AS timestamp) IS NULL OR e.event_start >= CAST(:from AS timestamp)) " +
            "AND (CAST(:to AS timestamp) IS NULL OR e.event_start <= CAST(:to AS timestamp)) " +
            "AND (CAST(:minPrice AS double precision) IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) >= CAST(:minPrice AS double precision)) " +
            "AND (CAST(:maxPrice AS double precision) IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) <= CAST(:maxPrice AS double precision)) " +
            "AND (CAST(:latitude AS double precision) IS NULL OR CAST(:longitude AS double precision) IS NULL OR CAST(:radiusMeters AS double precision) IS NULL " +
            "OR ST_DWithin(v.location, ST_SetSRID(ST_MakePoint(CAST(:longitude AS double precision), CAST(:latitude AS double precision)), 4326)::geography, CAST(:radiusMeters AS double precision)))";

    String PUBLISHED_EVENTS_COUNT_QUERY = "SELECT count(*) FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE;

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY e.event_start ASC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedBySoonest(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
            @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radiusMeters") Double radiusMeters,
            Pageable pageable);

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) ASC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedByPriceAsc(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
            @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radiusMeters") Double radiusMeters,
            Pageable pageable);

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) DESC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedByPriceDesc(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
            @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radiusMeters") Double radiusMeters,
            Pageable pageable);

    // Only ever invoked once EventServiceImpl#findPublishedEvents has confirmed
    // latitude/longitude/radiusMeters are all present -- unlike the WHERE clause's
    // deliberately lenient "any null means no-op" filter, sorting by distance from
    // nowhere isn't a meaningful fallback, so the guard lives at the call site instead
    // of here.
    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY ST_Distance(v.location, ST_SetSRID(ST_MakePoint(CAST(:longitude AS double precision), CAST(:latitude AS double precision)), 4326)::geography) ASC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedByDistance(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice,
            @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("radiusMeters") Double radiusMeters,
            Pageable pageable);

    // Backs the browse page's City filter -- plain JPQL, not native, since there's no
    // full-text search or nullable-parameter filtering here to force the native-query
    // path (see PUBLISHED_EVENTS_WHERE above). Only cities that actually have a
    // published event, not every venue ever created -- a venue with zero published
    // events would be a misleading filter option.
    @Query("SELECT DISTINCT e.venue.city FROM Event e WHERE e.status = 'PUBLISHED' ORDER BY e.venue.city")
    List<String> findDistinctPublishedEventCities();
}
