package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.TicketType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @Query("SELECT tt FROM TicketType tt WHERE tt.domainId = :domainId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketType> findByDomainIdWithLock(@Param("domainId") UUID domainId);
}
