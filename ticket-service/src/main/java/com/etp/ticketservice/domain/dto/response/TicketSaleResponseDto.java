package com.etp.ticketservice.domain.dto.response;

import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Shared by both the per-event and cross-event organizer ticket-sales endpoints --
// same shape either way (VenueResponseDto's reuse-across-endpoints precedent, not
// ListEventTicketTypeResponseDto/ListTicketTicketTypeResponseDto's "one DTO per
// endpoint" one): the per-event screen just doesn't render the event column.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketSaleResponseDto {
    private UUID id;
    private String referenceCode;
    private TicketStatusEnum status;
    private TicketSaleTicketTypeResponseDto ticketType;
    private String purchaserName;
    private String purchaserEmail;
    private UUID eventId;
    private String eventName;
    private LocalDateTime createdAt;
}
