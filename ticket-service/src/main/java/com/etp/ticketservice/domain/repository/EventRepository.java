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

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes WHERE e.domainId = :domainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Event> findByDomainIdAndOrganizerDomainId(@Param("domainId") UUID domainId, @Param("organizerDomainId") UUID organizerDomainId);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes WHERE e.domainId = :domainId AND e.status = :status")
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
    String PUBLISHED_EVENTS_WHERE = "e.status = 'PUBLISHED' " +
            "AND (:searchTerm IS NULL OR to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) @@ plainto_tsquery('english', :searchTerm)) " +
            "AND (:city IS NULL OR v.city = :city) " +
            "AND (:from IS NULL OR e.event_start >= :from) " +
            "AND (:to IS NULL OR e.event_start <= :to) " +
            "AND (:minPrice IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) >= :minPrice) " +
            "AND (:maxPrice IS NULL OR (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) <= :maxPrice)";

    String PUBLISHED_EVENTS_COUNT_QUERY = "SELECT count(*) FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE;

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY e.event_start ASC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedBySoonest(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) ASC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedByPriceAsc(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);

    @Query(value = "SELECT e.* FROM events e JOIN venues v ON v.id = e.venue_id WHERE " + PUBLISHED_EVENTS_WHERE +
            " ORDER BY (SELECT MIN(tt.price) FROM ticket_types tt WHERE tt.event_id = e.id) DESC",
            countQuery = PUBLISHED_EVENTS_COUNT_QUERY,
            nativeQuery = true)
    Page<Event> findPublishedEventsSortedByPriceDesc(@Param("searchTerm") String searchTerm, @Param("city") String city,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
            @Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice, Pageable pageable);
}
