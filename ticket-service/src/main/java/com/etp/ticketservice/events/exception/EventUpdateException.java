package com.etp.ticketservice.events.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class EventUpdateException extends EventTicketException {
    public EventUpdateException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventUpdateException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventUpdateException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
