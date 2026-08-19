package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query("SELECT v FROM Venue v WHERE v.domainId = :domainId")
    Optional<Venue> findByDomainId(@Param("domainId") UUID domainId);
}
