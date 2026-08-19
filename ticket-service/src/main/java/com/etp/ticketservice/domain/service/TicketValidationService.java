package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.TicketValidation;

import java.util.UUID;

public interface TicketValidationService {
    TicketValidation validateTicketByQrCode(UUID qrCodeId);

    TicketValidation validateTicketManually(UUID ticketId);

    TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation);
}
