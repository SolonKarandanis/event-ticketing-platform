package com.etp.ticketservice.domain.dto.response;

import com.etp.ticketservice.domain.enums.TicketCancelReasonEnum;
import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Shared by both the attendee-cancel and organizer-cancel endpoints -- same underlying
// Ticket, same shape either way.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelTicketResponseDto {
    private UUID id;
    private TicketStatusEnum status;
    private LocalDateTime cancelledAt;
    private TicketCancelReasonEnum cancelReason;
    private String cancelNote;
}
