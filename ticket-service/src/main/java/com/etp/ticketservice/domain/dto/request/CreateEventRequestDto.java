package com.etp.ticketservice.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateEventRequestDto {
    @NotBlank(message = "{validation.event.name.required}")
    private String name;

    private LocalDateTime start;
    private LocalDateTime end;

    @NotNull(message = "{validation.event.venue.required}")
    private UUID venueId;

    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;

    @NotEmpty(message = "{validation.event.ticket-types.required}")
    @Valid
    private List<CreateTicketTypeRequestDto> ticketTypes;

    // Optional -- 0 images is fine. Every entry here is a "new" one (nothing exists yet
    // to keep), so id is always null and newImageIndex always points into the request's
    // "newImages" multipart parts. The 8-image cap is enforced in EventServiceImpl, not
    // here -- that's a 409 Conflict (a state/quota conflict), not a 400 validation error.
    @Valid
    private List<EventImageRequestDto> images = new ArrayList<>();
}
