package com.etp.ticketservice.domain.enums;

public enum TicketValidationStatusEnum {
    VALID,
    INVALID,
    EXPIRED,
    // A validation attempt against a cancelled ticket -- distinct from INVALID (which
    // means "already used") so staff at the door see specifically why entry is refused.
    CANCELLED
}
