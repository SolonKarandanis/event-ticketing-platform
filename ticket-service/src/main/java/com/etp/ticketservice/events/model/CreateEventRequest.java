package com.etp.ticketservice.events.model;

import com.etp.ticketservice.tickettypes.model.CreateTicketTypeRequest;
import com.etp.ticketservice.events.images.model.EventImageRequest;

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
public class CreateEventRequest {
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private UUID venueId;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private List<CreateTicketTypeRequest> ticketTypes = new ArrayList<>();
    private List<EventImageRequest> images = new ArrayList<>();
}
