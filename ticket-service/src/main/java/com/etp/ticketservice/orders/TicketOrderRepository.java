package com.etp.ticketservice.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {

    @Query("SELECT o FROM TicketOrder o WHERE o.domainId = :domainId")
    Optional<TicketOrder> findByDomainId(@Param("domainId") UUID domainId);
}
