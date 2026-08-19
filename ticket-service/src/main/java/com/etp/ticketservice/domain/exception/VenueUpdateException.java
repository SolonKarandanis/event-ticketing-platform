package com.etp.ticketservice.domain.exception;

public class VenueUpdateException extends EventTicketException {
    public VenueUpdateException() {
    }

    public VenueUpdateException(String message) {
        super(message);
    }

    public VenueUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public VenueUpdateException(Throwable cause) {
        super(cause);
    }

    public VenueUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
