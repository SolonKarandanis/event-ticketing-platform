package com.etp.ticketservice.ticketvalidation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketValidationRepository extends JpaRepository<TicketValidation, Long> {
}
