package com.etp.ticketservice.ticketvalidation.dto;

import com.etp.ticketservice.ticketvalidation.TicketValidationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationResponseDto {
    private UUID ticketId;
    private TicketValidationStatusEnum status;
}
