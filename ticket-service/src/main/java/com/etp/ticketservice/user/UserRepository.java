package com.etp.ticketservice.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.domainId = :domainId")
    boolean existsByDomainId(@Param("domainId") UUID domainId);

    @Query("SELECT u FROM User u WHERE u.domainId = :domainId")
    Optional<User> findByDomainId(@Param("domainId") UUID domainId);
}
