package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.venue WHERE e.status = :status")
    Page<Event> findByStatus(@Param("status") EventStatusEnum status, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.venue LEFT JOIN FETCH e.ticketTypes WHERE e.domainId = :domainId AND e.status = :status")
    Optional<Event> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") EventStatusEnum status);

    // Native query for PostgreSQL full-text search -- JOIN FETCH isn't expressible here,
    // see EventServiceImpl#searchPublishedEvents for how venue gets hydrated instead.
    @Query(value = "SELECT e.* FROM events e " +
            "JOIN venues v ON v.id = e.venue_id WHERE " +
            "e.status = 'PUBLISHED' AND " +
            "to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) " +
            "@@ plainto_tsquery('english', :searchTerm)",
            countQuery = "SELECT count(*) FROM events e " +
                    "JOIN venues v ON v.id = e.venue_id WHERE " +
                    "e.status = 'PUBLISHED' AND " +
                    "to_tsvector('english', COALESCE(e.name, '') || ' ' || COALESCE(v.name, '') || ' ' || COALESCE(v.city, '')) " +
                    "@@ plainto_tsquery('english', :searchTerm)",
            nativeQuery = true)
    Page<Event> searchEvents(@Param("searchTerm") String searchTerm, Pageable pageable);
}
