package com.etp.ticketservice.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponseDto {
    private String id;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;

    // Lombok's isDefault() getter, combined with default Jackson bean introspection,
    // would otherwise serialize this field as "default" rather than "isDefault" -- same
    // gotcha already solved once in this codebase, see AbstractPaging's @JsonProperty
    // annotations.
    @JsonProperty("isDefault")
    private boolean isDefault;
}
