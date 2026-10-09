package com.etp.ticketservice.payments;

import com.etp.ticketservice.common.UserProvisioningTestConfig;
import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.config.InternationalizationConfig;
import com.etp.ticketservice.payments.exception.InvalidWebhookSignatureException;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.etp.ticketservice.common.TestJwts.withSubject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same slice-test approach as every other *ControllerTest -- note this slice does NOT
// import the real SecurityConfig, so it's still secured by Spring Security's own
// @WebMvcTest default; requests here authenticate with withSubject purely to get past
// that default, same as every other controller slice test in this codebase. This does
// NOT prove the production endpoint is reachable anonymously -- that's
// SecurityAuthorizationTest's job exclusively (see its stripeWebhook_isPermitAll_* test).
//
// The primary case below is the empirical proof of the raw-body claim documented on
// StripeWebhookController: payload is signed with the Stripe SDK's own
// Webhook.Signature.generateSignatureHeader test helper, then the exact same string is
// asserted to reach the (mocked) service -- any reparse/reserialize anywhere in the
// request pipeline would make this comparison fail.
@WebMvcTest(StripeWebhookController.class)
// InternationalizationConfig is required here specifically because this is the first
// controller slice to actually exercise GlobalExceptionHandler's message resolution:
// every other *ControllerTest's "bad request" case only goes through Bean Validation's
// own interpolator, which silently falls back to the raw "{code}" placeholder text when
// a key can't resolve -- but GlobalExceptionHandler.resolve(...) calls
// messageSource.getMessage(code, null, locale) directly, which throws
// NoSuchMessageException against the plain default MessageSource @WebMvcTest
// auto-configures when this isn't imported (the real bean, with this app's actual
// message keys, lives on this separate @Configuration class that @WebMvcTest doesn't
// scan by default).
@Import({UserProvisioningTestConfig.class, InternationalizationConfig.class})
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StripeWebhookService stripeWebhookService;

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void handleStripeWebhook_returns200_andPassesRawBodyAndSignatureThroughUnchanged() throws Exception {
        String payload = "{\"id\": \"evt_123\", \"type\": \"checkout.session.completed\"}";
        String signature = Webhook.Signature.generateSignatureHeader(payload, "whsec_test_secret");

        mockMvc.perform(post("/api/v1/payments/webhooks/stripe")
                        .with(withSubject(USER_ID))
                        .header("Stripe-Signature", signature)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk());

        verify(stripeWebhookService).handle(payload, signature);
    }

    @Test
    void handleStripeWebhook_serviceRejectsSignature_returns400() throws Exception {
        doThrow(new InvalidWebhookSignatureException(ErrorCode.INVALID_WEBHOOK_SIGNATURE))
                .when(stripeWebhookService).handle(any(), any());

        mockMvc.perform(post("/api/v1/payments/webhooks/stripe")
                        .with(withSubject(USER_ID))
                        .header("Stripe-Signature", "t=123,v1=garbled")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
