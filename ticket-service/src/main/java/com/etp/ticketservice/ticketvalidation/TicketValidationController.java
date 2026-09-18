package com.etp.ticketservice.ticketvalidation;

import com.etp.ticketservice.ticketvalidation.dto.TicketValidationRequestDto;
import com.etp.ticketservice.ticketvalidation.dto.TicketValidationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/ticket-validations")
@RequiredArgsConstructor
public class TicketValidationController {

    private static final Logger log = LoggerFactory.getLogger(TicketValidationController.class);

    private final TicketValidationService ticketValidationService;

    @PostMapping
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @Valid @RequestBody TicketValidationRequestDto ticketValidationRequestDto) {
        log.info("TicketValidationController --> validateTicket");
        TicketValidationMethod method = ticketValidationRequestDto.getMethod();
        TicketValidation ticketValidation;
        if (TicketValidationMethod.MANUAL.equals(method)) {
            ticketValidation = ticketValidationService.validateTicketByReferenceCode(
                    ticketValidationRequestDto.getId());
        } else {
            ticketValidation = ticketValidationService.validateTicketByQrCode(
                    ticketValidationRequestDto.getId()
            );
        }
        return ResponseEntity.ok(
                ticketValidationService.convertToTicketValidationResponseDto(ticketValidation)
        );
    }
}
