package com.etp.ticketservice.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
}
