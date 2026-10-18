package com.etp.ticketservice.orders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketOrderExpiryJobTest {

    @Mock
    private TicketOrderExpirySweeper sweeper;

    @InjectMocks
    private TicketOrderExpiryJob job;

    @Test
    void execute_delegatesToSweeper() {
        job.execute(null);

        verify(sweeper).sweep();
    }
}
