package com.etp.ticketservice.tickettypes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTicketTypeRequest {
    private String name;
    private Long priceMinorUnits;
    private String description;
    private Integer totalAvailable;
}
