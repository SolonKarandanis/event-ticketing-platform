package com.etp.ticketservice.events.dto;

import com.etp.ticketservice.events.images.dto.EventImageResponseDto;

import com.etp.ticketservice.venues.dto.VenueResponseDto;

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
public class GetPublishedEventDetailsResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    private List<GetPublishedEventDetailsTicketTypesResponseDto> ticketTypes = new ArrayList<>();
    private List<EventImageResponseDto> images = new ArrayList<>();
}
