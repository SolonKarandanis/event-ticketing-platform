package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.EventImage;
import com.etp.ticketservice.domain.enums.EventStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    // Backs the organizer-facing raw-bytes GET -- lookup + ownership check in one query,
    // the same "wrong image, or someone else's event, is indistinguishable from not
    // found" shape TicketRepository#findByDomainIdAndEventDomainIdAndOrganizerDomainId
    // already uses. Works for a still-DRAFT event, unlike the public lookup below.
    @Query("SELECT ei FROM EventImage ei WHERE ei.domainId = :imageId " +
            "AND ei.event.domainId = :eventId AND ei.event.organizer.domainId = :organizerId")
    Optional<EventImage> findByDomainIdAndEventDomainIdAndEventOrganizerDomainId(
            @Param("imageId") UUID imageId, @Param("eventId") UUID eventId, @Param("organizerId") UUID organizerId);

    // Backs the public raw-bytes GET -- only ever resolves an image belonging to a
    // PUBLISHED event, mirroring EventRepository#findByDomainIdAndStatus's own
    // public/organizer split for event data itself.
    @Query("SELECT ei FROM EventImage ei WHERE ei.domainId = :imageId " +
            "AND ei.event.domainId = :eventId AND ei.event.status = :status")
    Optional<EventImage> findByDomainIdAndEventDomainIdAndEventStatus(
            @Param("imageId") UUID imageId, @Param("eventId") UUID eventId, @Param("status") EventStatusEnum status);
}
