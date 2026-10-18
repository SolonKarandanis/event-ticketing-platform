package com.etp.ticketservice.orders;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

// Quartz instantiates Job classes itself via reflection on every fire (needs a public
// no-arg constructor, so no Lombok constructor injection here -- see
// AutowiringSpringBeanJobFactory for how the field below still gets populated), so this
// adapter is deliberately thin: the real sweep logic plus its existing constructor-injected
// unit tests stay on TicketOrderExpirySweeper untouched.
// @DisallowConcurrentExecution preserves the old @Scheduled(fixedDelay=...) guarantee that
// a sweep never overlaps a still-running previous one.
@DisallowConcurrentExecution
public class TicketOrderExpiryJob implements Job {

    @Autowired
    private TicketOrderExpirySweeper sweeper;

    @Override
    public void execute(JobExecutionContext context) {
        sweeper.sweep();
    }
}
