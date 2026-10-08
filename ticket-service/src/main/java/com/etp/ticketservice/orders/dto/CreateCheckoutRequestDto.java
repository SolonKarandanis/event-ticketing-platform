package com.etp.ticketservice.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCheckoutRequestDto {

    @NotEmpty(message = "{validation.checkout.items.required}")
    @Valid
    private List<CheckoutLineItemRequestDto> items;
}
