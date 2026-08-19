package com.etp.ticketservice.domain.exception;

public class VenueNotFoundException extends EventTicketException {
    public VenueNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VenueNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public VenueNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
