package com.etp.ticketservice.venues.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
