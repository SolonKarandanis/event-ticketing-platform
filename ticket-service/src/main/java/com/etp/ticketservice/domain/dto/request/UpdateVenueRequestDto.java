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
public class UpdateVenueRequestDto {
    @NotNull(message = "{validation.venue.id.required}")
    private UUID id;

    @NotBlank(message = "{validation.venue.name.required}")
    private String name;

    @NotBlank(message = "{validation.venue.address-line-1.required}")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "{validation.venue.city.required}")
    private String city;

    @NotBlank(message = "{validation.venue.postal-code.required}")
    private String postalCode;

    @NotBlank(message = "{validation.venue.country.required}")
    private String country;

    private Double latitude;
    private Double longitude;

    @PositiveOrZero(message = "{validation.venue.capacity.positive-or-zero}")
    private Integer capacity;

    private String accessibilityInfo;
}
