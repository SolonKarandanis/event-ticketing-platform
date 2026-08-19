package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Event;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.organizer.domainId = :organizerDomainId")
    Page<Event> findByOrganizerDomainId(@Param("organizerDomainId") UUID organizerDomainId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.domainId = :domainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Event> findByDomainIdAndOrganizerDomainId(@Param("domainId") UUID domainId, @Param("organizerDomainId") UUID organizerDomainId);

    @Query("SELECT e FROM Event e WHERE e.status = :status")
    Page<Event> findByStatus(@Param("status") EventStatusEnum status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.domainId = :domainId AND e.status = :status")
    Optional<Event> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") EventStatusEnum status);

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
