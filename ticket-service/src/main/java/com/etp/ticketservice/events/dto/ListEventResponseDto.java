package com.etp.ticketservice.events.dto;

import com.etp.ticketservice.venues.dto.VenueResponseDto;

import com.etp.ticketservice.events.EventStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<ListEventTicketTypeResponseDto> ticketTypes = new ArrayList<>();
}
