package com.etp.ticketservice.tickettypes;

import com.etp.ticketservice.common.UserProvisioningTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.etp.ticketservice.common.TestJwts.withSubject;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as EventControllerTest. One endpoint, one behavior worth
// asserting: the path variables actually reach TicketTypeService unmodified.
@WebMvcTest(TicketTypeController.class)
@Import(UserProvisioningTestConfig.class)
class TicketTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketTypeService ticketTypeService;

    private static final UUID ATTENDEE_ID = UUID.randomUUID();

    @Test
    void purchaseTicket_returns204() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketTypeId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/events/{eventId}/ticket-types/{ticketTypeId}/tickets", eventId, ticketTypeId)
                        .with(withSubject(ATTENDEE_ID)))
                .andExpect(status().isNoContent());

        verify(ticketTypeService).purchaseTicket(ATTENDEE_ID, ticketTypeId);
    }
}
