package com.etp.ticketservice.events.model;

import com.etp.ticketservice.events.images.model.EventImageRequest;
import com.etp.ticketservice.tickettypes.model.UpdateTicketTypeRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventRequest {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private UUID venueId;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private List<UpdateTicketTypeRequest> ticketTypes = new ArrayList<>();
    private List<EventImageRequest> images = new ArrayList<>();
}
