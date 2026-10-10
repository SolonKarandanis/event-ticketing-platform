package com.etp.ticketservice.payments;

import com.etp.ticketservice.payments.dto.PaymentMethodResponseDto;
import com.etp.ticketservice.payments.dto.SetupIntentResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Top-level resource scoped to the current user (like TicketController), not nested
// under /api/v1/events/** -- no SecurityConfig rule needed, falls through to the
// existing anyRequest().authenticated() default, which is correct: any authenticated
// user manages their own saved cards.
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payment-methods")
public class PaymentMethodController {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodController.class);

    private final PaymentMethodService paymentMethodService;

    @PostMapping(path = "/setup-intents")
    public ResponseEntity<SetupIntentResponseDto> createSetupIntent(@AuthenticationPrincipal Jwt jwt) {
        log.info("PaymentMethodController --> createSetupIntent");
        String clientSecret = paymentMethodService.createSetupIntent(parseUserId(jwt));
        return new ResponseEntity<>(new SetupIntentResponseDto(clientSecret), HttpStatus.CREATED);
    }

    @GetMapping
    public List<PaymentMethodResponseDto> listPaymentMethods(@AuthenticationPrincipal Jwt jwt) {
        log.info("PaymentMethodController --> listPaymentMethods");
        return paymentMethodService.listPaymentMethods(parseUserId(jwt));
    }

    @DeleteMapping(path = "/{paymentMethodId}")
    public ResponseEntity<Void> removePaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentMethodId) {
        log.info("PaymentMethodController --> removePaymentMethod");
        paymentMethodService.removePaymentMethod(parseUserId(jwt), paymentMethodId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/{paymentMethodId}/default")
    public ResponseEntity<Void> setDefaultPaymentMethod(@AuthenticationPrincipal Jwt jwt, @PathVariable String paymentMethodId) {
        log.info("PaymentMethodController --> setDefaultPaymentMethod");
        paymentMethodService.setDefaultPaymentMethod(parseUserId(jwt), paymentMethodId);
        return ResponseEntity.noContent().build();
    }

    private UUID parseUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
