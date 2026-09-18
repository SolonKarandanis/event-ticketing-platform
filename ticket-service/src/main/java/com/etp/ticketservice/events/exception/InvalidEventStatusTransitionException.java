package com.etp.ticketservice.events.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class InvalidEventStatusTransitionException extends EventTicketException {
    public InvalidEventStatusTransitionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidEventStatusTransitionException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public InvalidEventStatusTransitionException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
