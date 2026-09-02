package com.etp.ticketservice.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// Two shapes in one, the same "id present = keep, absent = create" pattern
// UpdateTicketTypeRequestDto already uses -- id present means keep this existing image
// (possibly at a new position/altText); id null means this is a new image, and
// newImageIndex points at its file among the request's separate "newImages" multipart
// parts (JSON can't carry the file bytes inline). There's no separate position field --
// this entry's index in the parent images list IS the gallery position, the same way
// there's no separate reorder endpoint.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventImageRequestDto {
    private UUID id;

    private Integer newImageIndex;

    @Size(max = 255, message = "{validation.event.image.alt-text.too-long}")
    private String altText;
}
