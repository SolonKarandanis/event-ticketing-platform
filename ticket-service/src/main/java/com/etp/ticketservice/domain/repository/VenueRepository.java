package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // A null/absent searchTerm is "browse all" (same convention as published events'
    // search) -- one method covers both the plain list and the search-as-you-type
    // venue picker, rather than splitting into findAll/search.
    @Query("SELECT v FROM Venue v WHERE :searchTerm IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Venue> search(@Param("searchTerm") String searchTerm, Pageable pageable);
}
