package com.etp.ticketservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTicketTypeRequestDto {

    private UUID id;

    @NotBlank(message = "{validation.ticket-type.name.required}")
    private String name;

    @NotNull(message = "{validation.ticket-type.price.required}")
    @PositiveOrZero(message = "{validation.ticket-type.price.positive-or-zero}")
    private Double price;

    private String description;

    private Integer totalAvailable;
}
