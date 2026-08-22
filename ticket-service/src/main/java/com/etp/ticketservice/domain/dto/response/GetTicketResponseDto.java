package com.etp.ticketservice.domain.dto.response;

import com.etp.ticketservice.domain.enums.TicketStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
