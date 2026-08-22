package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketType.id = :ticketTypeId")
    int countByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId);

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
}
