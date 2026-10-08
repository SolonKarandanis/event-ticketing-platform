package com.etp.ticketservice.orders;

import com.etp.ticketservice.common.UserProvisioningTestConfig;
import com.etp.ticketservice.orders.dto.CheckoutLineItemRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.etp.ticketservice.common.TestJwts.withSubject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as TicketTypeControllerTest. One endpoint, two things worth
// asserting: the path variable and deserialized body reach CheckoutService unmodified,
// and the response DTO actually serializes back out.
@WebMvcTest(CheckoutController.class)
@Import(UserProvisioningTestConfig.class)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CheckoutService checkoutService;

    private static final UUID ATTENDEE_ID = UUID.randomUUID();

    @Test
    void createCheckout_returns201WithCheckoutUrl() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID ticketTypeId = UUID.randomUUID();
        CreateCheckoutRequestDto request = new CreateCheckoutRequestDto(
                List.of(new CheckoutLineItemRequestDto(ticketTypeId, 2)));
        UUID orderId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        when(checkoutService.createCheckout(ATTENDEE_ID, eventId, request))
                .thenReturn(new CreateCheckoutResponseDto(orderId, "https://checkout.stripe.com/test", expiresAt));

        mockMvc.perform(post("/api/v1/events/{eventId}/checkout", eventId)
                        .with(withSubject(ATTENDEE_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.com/test"));

        verify(checkoutService).createCheckout(ATTENDEE_ID, eventId, request);
    }

    @Test
    void createCheckout_emptyCart_returns400() throws Exception {
        UUID eventId = UUID.randomUUID();
        CreateCheckoutRequestDto request = new CreateCheckoutRequestDto(List.of());

        mockMvc.perform(post("/api/v1/events/{eventId}/checkout", eventId)
                        .with(withSubject(ATTENDEE_ID))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest());
    }
}
