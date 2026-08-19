package com.etp.ticketservice.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEventTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
    private String description;
    private Integer totalAvailable;
}
