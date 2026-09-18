package com.etp.ticketservice.events.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class EventNotFoundException extends EventTicketException {
    public EventNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
