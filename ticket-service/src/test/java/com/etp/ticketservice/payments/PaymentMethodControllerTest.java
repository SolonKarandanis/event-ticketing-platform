package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.UserProvisioningTestConfig;
import com.etp.ticketservice.payments.dto.PaymentMethodResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.etp.ticketservice.common.TestJwts.withSubject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as TicketControllerTest/CheckoutControllerTest -- every
// request here is authenticated as the resource owner (ATTENDEE_ID); the controller
// itself has no notion of roles, it just trusts parseUserId(jwt) and lets
// PaymentMethodServiceImpl resolve ownership against the stored PaymentCustomer row.
@WebMvcTest(PaymentMethodController.class)
@Import(UserProvisioningTestConfig.class)
class PaymentMethodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    private static final UUID ATTENDEE_ID = UUID.randomUUID();

    @Test
    void createSetupIntent_returns201WithClientSecret() throws Exception {
        when(paymentMethodService.createSetupIntent(ATTENDEE_ID)).thenReturn("seti_secret_123");

        mockMvc.perform(post("/api/v1/payment-methods/setup-intents").with(attendee()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientSecret").value("seti_secret_123"));
    }

    @Test
    void listPaymentMethods_returnsCards() throws Exception {
        when(paymentMethodService.listPaymentMethods(ATTENDEE_ID)).thenReturn(List.of(
                new PaymentMethodResponseDto("pm_123", "visa", "4242", 8L, 2028L, true)));

        mockMvc.perform(get("/api/v1/payment-methods").with(attendee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("pm_123"))
                .andExpect(jsonPath("$[0].brand").value("visa"))
                .andExpect(jsonPath("$[0].last4").value("4242"))
                .andExpect(jsonPath("$[0].isDefault").value(true));
    }

    @Test
    void removePaymentMethod_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/payment-methods/{paymentMethodId}", "pm_123").with(attendee()))
                .andExpect(status().isNoContent());

        verify(paymentMethodService).removePaymentMethod(ATTENDEE_ID, "pm_123");
    }

    @Test
    void setDefaultPaymentMethod_returns204() throws Exception {
        mockMvc.perform(put("/api/v1/payment-methods/{paymentMethodId}/default", "pm_123").with(attendee()))
                .andExpect(status().isNoContent());

        verify(paymentMethodService).setDefaultPaymentMethod(ATTENDEE_ID, "pm_123");
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor attendee() {
        return withSubject(ATTENDEE_ID);
    }
}
