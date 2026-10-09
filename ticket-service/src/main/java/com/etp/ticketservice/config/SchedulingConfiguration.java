package com.etp.ticketservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// ticket-service's first use of Spring scheduling (TicketOrderExpirySweeper) -- a
// dedicated single-purpose @Configuration class, matching JpaConfiguration's own
// precedent, rather than annotating TicketServiceApplication directly.
@Configuration
@EnableScheduling
public class SchedulingConfiguration {
}
