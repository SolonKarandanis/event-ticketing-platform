package com.etp.ticketservice.domain.dto.request;

import com.etp.ticketservice.domain.enums.TicketValidationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationRequestDto {
    // A QR code's domainId (UUID, as a string) for QR_SCAN, or a ticket's referenceCode for
    // MANUAL -- which one depends on `method`.
    @NotBlank(message = "{validation.ticket-validation.id.required}")
    private String id;

    @NotNull(message = "{validation.ticket-validation.method.required}")
    private TicketValidationMethod method;
}
