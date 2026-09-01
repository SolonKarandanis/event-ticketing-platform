package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Ticket;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Counts every ticket ever created against this ticket type, cancelled or not --
    // deliberately unfiltered. This is what guards the "can't remove a ticket type that
    // has sold tickets" check in EventServiceImpl#updateEventForOrganizer: a cancelled
    // ticket is still a real historical row (with its own cancellation audit trail --
    // see Ticket.cancelledAt/cancelReason/cancelNote) that orphan-deletion would destroy,
    // so it stays protected even after cancellation. For "does this still count against
    // availability" (the sold-out check, and the ticketsSold figure shown to
    // organizers), see countActiveByTicketTypeId below instead.
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
    int countByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

    // Excludes CANCELLED -- a cancelled ticket frees its slot back up, so this is the
    // count both purchaseTicket's sold-out check and the organizer-facing ticketsSold
    // figure should read, not the raw historical count above.
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId AND t.status <> :cancelledStatus")
    int countActiveByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId, @Param("cancelledStatus") TicketStatusEnum cancelledStatus);

    // ticketType is a @ManyToOne (to-one) fetch join -- safe to combine with Pageable.
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketType WHERE t.purchaser.domainId = :purchaserDomainId")
    Page<Ticket> findByPurchaserDomainId(@Param("purchaserDomainId") UUID purchaserDomainId, Pageable pageable);

    // GetTicketResponseDto walks ticket -> ticketType -> event -> venue; all @ManyToOne
    // (to-one) hops, so chaining the fetch joins never multiplies rows -- no DISTINCT needed.
    @Query("SELECT t FROM Ticket t " +
            "LEFT JOIN FETCH t.ticketType tt " +
            "LEFT JOIN FETCH tt.event e " +
            "LEFT JOIN FETCH e.venue " +
            "WHERE t.domainId = :domainId AND t.purchaser.domainId = :purchaserDomainId")
    Optional<Ticket> findByDomainIdAndPurchaserDomainId(@Param("domainId") UUID domainId, @Param("purchaserDomainId") UUID purchaserDomainId);

    @Query("SELECT t FROM Ticket t WHERE t.domainId = :domainId")
    Optional<Ticket> findByDomainId(@Param("domainId") UUID domainId);

    @Query("SELECT t FROM Ticket t WHERE t.referenceCode = :referenceCode")
    Optional<Ticket> findByReferenceCode(@Param("referenceCode") String referenceCode);

    // Organizer-cancel's lookup+authorization in one query: the ticket must both exist
    // and belong to an event owned by the calling organizer -- a mismatch on either
    // (wrong ticket, or someone else's event) is indistinguishable from "not found",
    // matching how getEventForOrganizer already treats an ID/ownership mismatch.
    // ticketType/event are @ManyToOne (to-one) fetch joins, needed to check the event's
    // status (can't cancel once COMPLETED) without a second query.
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketType tt LEFT JOIN FETCH tt.event e " +
            "WHERE t.domainId = :ticketDomainId AND e.domainId = :eventDomainId AND e.organizer.domainId = :organizerDomainId")
    Optional<Ticket> findByDomainIdAndEventDomainIdAndOrganizerDomainId(
            @Param("ticketDomainId") UUID ticketDomainId,
            @Param("eventDomainId") UUID eventDomainId,
            @Param("organizerDomainId") UUID organizerDomainId);

    // Per-event ticket-sales screen. ticketType/event/purchaser are all @ManyToOne --
    // safe to fetch join together with Pageable, none can multiply result rows. event is
    // fetch-joined even though the caller already knows which event this is (it's in the
    // URL) -- TicketSaleResponseDto is shared with the cross-event listing below and
    // always carries eventId/eventName, and that DTO conversion runs in the controller,
    // after this method's transaction (and Hibernate session) has already closed.
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketType tt LEFT JOIN FETCH tt.event LEFT JOIN FETCH t.purchaser " +
            "WHERE tt.event.domainId = :eventDomainId AND tt.event.organizer.domainId = :organizerDomainId")
    Page<Ticket> findByEventDomainIdAndOrganizerDomainId(
            @Param("eventDomainId") UUID eventDomainId,
            @Param("organizerDomainId") UUID organizerDomainId,
            Pageable pageable);

    // Cross-event ticket-sales screen -- same shape as above, minus the single-event
    // filter, plus event itself fetch-joined (still to-one throughout) since the
    // response needs to show which event each row belongs to.
    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.ticketType tt LEFT JOIN FETCH tt.event LEFT JOIN FETCH t.purchaser " +
            "WHERE tt.event.organizer.domainId = :organizerDomainId")
    Page<Ticket> findByOrganizerDomainId(@Param("organizerDomainId") UUID organizerDomainId, Pageable pageable);

    // Backs EventServiceImpl#cancelEvent's cascade: every non-cancelled ticket for the
    // event, with validations fetch-joined so the cascade can check "already validated"
    // for each one in memory rather than one query per ticket. validations is a
    // @OneToMany (to-many), hence DISTINCT -- this returns a plain List, not a Page, so
    // there's no pagination/collection-fetch-join conflict to design around here (see
    // EventServiceImpl#listEventsForOrganizer for where that conflict actually applies).
    @Query("SELECT DISTINCT t FROM Ticket t LEFT JOIN FETCH t.validations " +
            "WHERE t.ticketType.event.id = :eventId AND t.status <> :cancelledStatus")
    List<Ticket> findByEventIdAndStatusNotWithValidations(
            @Param("eventId") Long eventId,
            @Param("cancelledStatus") TicketStatusEnum cancelledStatus);
}
