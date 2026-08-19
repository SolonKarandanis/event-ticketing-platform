package com.etp.ticketservice.domain.repository;

import com.etp.ticketservice.domain.entity.TicketValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketValidationRepository extends JpaRepository<TicketValidation, Long> {
}
