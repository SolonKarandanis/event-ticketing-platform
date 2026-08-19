package com.etp.ticketservice.domain.exception;

public class VenueNotFoundException extends EventTicketException {
    public VenueNotFoundException() {
    }

    public VenueNotFoundException(String message) {
        super(message);
    }

    public VenueNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VenueNotFoundException(Throwable cause) {
        super(cause);
    }

    public VenueNotFoundException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
