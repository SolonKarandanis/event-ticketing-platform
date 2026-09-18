package com.etp.ticketservice.ticketvalidation;

import com.etp.ticketservice.ticketvalidation.dto.TicketValidationResponseDto;

public interface TicketValidationService {
    TicketValidation validateTicketByQrCode(String qrCodeId);

    TicketValidation validateTicketByReferenceCode(String referenceCode);

    TicketValidationResponseDto convertToTicketValidationResponseDto(TicketValidation ticketValidation);
}
