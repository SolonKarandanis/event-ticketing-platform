package com.etp.ticketservice.events.images.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class EventImageNotFoundException extends EventTicketException {
    public EventImageNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventImageNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventImageNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
