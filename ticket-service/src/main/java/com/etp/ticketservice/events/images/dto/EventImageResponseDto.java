package com.etp.ticketservice.events.images.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// No url field -- same precedent QR codes already set (GetTicketResponseDto doesn't
// carry one either): the frontend already knows how to build a byte-serving fetch URL
// from an id it has (an eventId already in scope, plus this image's own id), the same
// way it already does for a ticket's QR code image.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventImageResponseDto {
    private UUID id;
    private String altText;
}
