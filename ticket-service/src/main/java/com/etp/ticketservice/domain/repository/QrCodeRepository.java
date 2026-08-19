package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.QrCode;
import com.etp.ticketservice.domain.enums.QrCodeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {

    @Query("SELECT qc FROM QrCode qc WHERE qc.ticket.domainId = :ticketDomainId AND qc.ticket.purchaser.domainId = :ticketPurchaserDomainId")
    Optional<QrCode> findByTicketDomainIdAndTicketPurchaserDomainId(@Param("ticketDomainId") UUID ticketDomainId, @Param("ticketPurchaserDomainId") UUID ticketPurchaserDomainId);

    @Query("SELECT qc FROM QrCode qc WHERE qc.domainId = :domainId AND qc.status = :status")
    Optional<QrCode> findByDomainIdAndStatus(@Param("domainId") UUID domainId, @Param("status") QrCodeStatusEnum status);
}
