package com.etp.ticketservice.domain.service;

import com.etp.ticketservice.domain.dto.response.TicketValidationResponseDto;
import com.etp.ticketservice.domain.entity.TicketValidation;

public interface TicketValidationService {
    TicketValidation validateTicketByQrCode(String qrCodeId);

    TicketValidation validateTicketByReferenceCode(String referenceCode);

    TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation);
}
