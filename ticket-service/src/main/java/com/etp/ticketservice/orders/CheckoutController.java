package com.etp.ticketservice.orders;

import com.etp.ticketservice.orders.dto.CreateCheckoutRequestDto;
import com.etp.ticketservice.orders.dto.CreateCheckoutResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/events/{eventId}/checkout")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CreateCheckoutResponseDto> createCheckout(@AuthenticationPrincipal Jwt jwt,
                                                                      @PathVariable UUID eventId,
                                                                      @Valid @RequestBody CreateCheckoutRequestDto request) {
        log.info("CheckoutController --> createCheckout");
        CreateCheckoutResponseDto response = checkoutService.createCheckout(parseUserId(jwt), eventId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    private UUID parseUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
