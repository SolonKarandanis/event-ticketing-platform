package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.request.TicketValidationRequestDto;
import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.TicketValidation;
import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import com.etp.ticketservice.domain.service.TicketValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/ticket-validations")
@RequiredArgsConstructor
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    @PostMapping
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @Valid @RequestBody TicketValidationRequestDto ticketValidationRequestDto) {
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
