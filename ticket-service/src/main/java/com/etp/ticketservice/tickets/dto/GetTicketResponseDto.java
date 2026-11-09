package com.etp.ticketservice.tickets.dto;

import com.etp.ticketservice.tickets.TicketStatusEnum;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTicketResponseDto {
    private UUID id;
    private String referenceCode;
    private TicketStatusEnum status;
    private Double price;
    private String description;
    private String eventName;
    private String eventVenueName;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
}
