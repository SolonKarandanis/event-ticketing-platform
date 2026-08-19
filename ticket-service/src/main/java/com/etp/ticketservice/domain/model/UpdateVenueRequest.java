package com.etp.ticketservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVenueRequest {
    private UUID id;
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer capacity;
    private String accessibilityInfo;
}
