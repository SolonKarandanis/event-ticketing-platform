package com.etp.ticketservice.events.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class InvalidEventDatesException extends EventTicketException {
    public InvalidEventDatesException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidEventDatesException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public InvalidEventDatesException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
