package com.etp.ticketservice.venues.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
