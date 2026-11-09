package com.etp.ticketservice.orders.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutLineItemRequestDto {

    @NotNull(message = "{validation.checkout.item.ticket-type-id.required}")
    private UUID ticketTypeId;

    @NotNull(message = "{validation.checkout.item.quantity.required}")
    @Positive(message = "{validation.checkout.item.quantity.positive}")
    private Integer quantity;
}
