package com.etp.ticketservice.orders;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {

    @Query("SELECT o FROM TicketOrder o WHERE o.domainId = :domainId")
    Optional<TicketOrder> findByDomainId(@Param("domainId") UUID domainId);

    // Pessimistic lock -- same reasoning as TicketTypeRepository.findByDomainIdWithLock:
    // prevents a webhook delivery and the scheduled sweep from both reading the same
    // order's pre-transition status and racing each other across its expiry boundary.
    @Query("SELECT o FROM TicketOrder o WHERE o.domainId = :domainId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketOrder> findByDomainIdWithLock(@Param("domainId") UUID domainId);

    // Unlocked, ids only -- the sweep never holds a lock across the whole candidate
    // batch, only per-order inside TicketOrderServiceImpl#expireOrder.
    @Query("SELECT o.domainId FROM TicketOrder o WHERE o.status = :status AND o.expiresAt < :now")
    List<UUID> findDomainIdsByStatusAndExpiresAtBefore(
            @Param("status") OrderStatusEnum status, @Param("now") LocalDateTime now);
}
