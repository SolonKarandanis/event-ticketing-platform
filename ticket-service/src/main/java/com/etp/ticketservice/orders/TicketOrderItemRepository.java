package com.etp.ticketservice.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TicketOrderItemRepository extends JpaRepository<TicketOrderItem, Long> {

    // Sums quantity held by every still-live order in the given status against this
    // ticket type. "Non-expired" is enforced here by filtering expiresAt > :now, NOT by
    // status alone -- nothing (until #21's sweep exists) ever flips a stale PENDING row
    // to EXPIRED, so this WHERE clause is the entire hold-release mechanism for now: once
    // a row's expiresAt passes, it silently stops counting here even though its status
    // column still says PENDING forever.
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM TicketOrderItem oi " +
            "WHERE oi.ticketType.id = :ticketTypeId " +
            "AND oi.ticketOrder.status = :status " +
            "AND oi.ticketOrder.expiresAt > :now")
    int sumReservedQuantityByTicketTypeId(@Param("ticketTypeId") Long ticketTypeId,
            @Param("status") OrderStatusEnum status,
            @Param("now") LocalDateTime now);
}
