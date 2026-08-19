package com.etp.ticketservice.domain.exception;

public class VenueUpdateException extends EventTicketException {
    public VenueUpdateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VenueUpdateException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public VenueUpdateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
