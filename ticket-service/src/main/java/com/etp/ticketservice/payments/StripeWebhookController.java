package com.etp.ticketservice.payments;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Reachable with NO JWT at all (see SecurityConfig) -- trusted only by Stripe's
// cryptographic signature, verified inside stripeWebhookService.handle. @RequestBody
// String, not a parsed DTO: Spring's Jackson converter explicitly declines String-typed
// targets, deferring to StringHttpMessageConverter, which decodes bytes -> String with
// no reparse/reserialize step -- the exact bytes Stripe signed must reach
// Webhook.constructEvent unchanged, or signature verification fails.
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payments/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeWebhookService stripeWebhookService;

    @PostMapping
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        log.info("StripeWebhookController --> handleStripeWebhook");
        stripeWebhookService.handle(payload, signature);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
