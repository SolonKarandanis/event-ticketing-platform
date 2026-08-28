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
    //
    // CAST(:searchTerm AS string) is required, not decorative: with a null searchTerm,
    // Postgres has to infer a type for the bind parameter purely from context, since
    // ":searchTerm IS NULL" alone gives it none. Left uncast, its || (CONCAT) operator
    // resolution picks a bytea overload for the unknown-typed parameter instead of text,
    // and LOWER(bytea) then fails outright with "function lower(bytea) does not exist".
    // The explicit cast pins the parameter to text everywhere it's used.
    @Query("SELECT v FROM Venue v WHERE CAST(:searchTerm AS string) IS NULL OR LOWER(v.name) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))")
    Page<Venue> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Backs a future "near me" browse filter -- native, not JPQL: passing raw
    // latitude/longitude and letting Postgres build the point via ST_MakePoint matches
    // how every other geo-adjacent value in this codebase (VenueForm's lat/lng inputs)
    // is already handled, as plain doubles, rather than constructing a JTS Point on the
    // Java side just to hand it to the query as a parameter. Ordered nearest-first --
    // Spring Data does not inject an ORDER BY from Pageable into native queries (see
    // EventRepository's published-events queries for the same gotcha), so this is
    // written directly into the query rather than left to the caller's Pageable.sort.
    //
    // v.location IS NOT NULL excludes venues with no coordinates at all -- location is
    // only populated where latitude/longitude both already were (see the
    // 003-add-venue-geography migration's backfill), so a venue missing both would
    // otherwise never match ST_DWithin and doesn't need to be explicitly filtered out,
    // but the guard documents that exclusion instead of leaving it implicit.
    @Query(value = "SELECT v.* FROM venues v WHERE v.location IS NOT NULL " +
            "AND ST_DWithin(v.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters) " +
            "ORDER BY ST_Distance(v.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) ASC",
            countQuery = "SELECT count(*) FROM venues v WHERE v.location IS NOT NULL " +
            "AND ST_DWithin(v.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, :radiusMeters)",
            nativeQuery = true)
    Page<Venue> findWithinRadius(@Param("latitude") double latitude, @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters, Pageable pageable);
}
