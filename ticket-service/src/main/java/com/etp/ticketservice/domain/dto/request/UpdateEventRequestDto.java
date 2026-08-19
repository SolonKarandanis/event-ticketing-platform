package com.etp.ticketservice.domain.dto.request;

import com.etp.ticketservice.domain.enums.EventStatusEnum;
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
public class UpdateEventRequestDto {

    @NotNull(message = "{validation.event.id.required}")
    private UUID id;

    @NotBlank(message = "{validation.event.name.required}")
    private String name;

    private LocalDateTime start;

    private LocalDateTime end;

    @NotNull(message = "{validation.event.venue.required}")
    private UUID venueId;

    private LocalDateTime salesStart;

    private LocalDateTime salesEnd;

    @NotNull(message = "{validation.event.status.required}")
    private EventStatusEnum status;

    @NotEmpty(message = "{validation.event.ticket-types.required}")
    @Valid
    private List<UpdateTicketTypeRequestDto> ticketTypes;
}
