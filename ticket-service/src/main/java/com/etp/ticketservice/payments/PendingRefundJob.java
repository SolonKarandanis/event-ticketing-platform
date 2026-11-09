package com.etp.ticketservice.payments;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

// Thin Quartz adapter, see TicketOrderExpiryJob for why. @DisallowConcurrentExecution keeps
// two passes from retrying the same refund at once.
@DisallowConcurrentExecution
public class PendingRefundJob implements Job {

    @Autowired private PendingRefundSweeper sweeper;

    @Override
    public void execute(JobExecutionContext context) {
        sweeper.sweep();
    }
}
