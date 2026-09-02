package com.etp.ticketservice.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListPublishedEventResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private VenueResponseDto venue;
    // The image at position 0, or null if the event has none -- a browse card shows one
    // image, not a whole gallery, so only the cover rides along here (see
    // GetPublishedEventDetailsResponseDto.images for the full ordered gallery).
    private UUID coverImageId;
}
