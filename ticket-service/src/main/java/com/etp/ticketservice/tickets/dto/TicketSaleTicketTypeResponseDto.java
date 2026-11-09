package com.etp.ticketservice.tickets.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketSaleTicketTypeResponseDto {
    private UUID id;
    private String name;
    private Double price;
}
