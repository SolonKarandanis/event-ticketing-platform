package com.etp.ticketservice.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketSaleTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
}
