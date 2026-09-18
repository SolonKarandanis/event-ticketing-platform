package com.etp.ticketservice.events.images.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class InvalidEventImageException extends EventTicketException {
    public InvalidEventImageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidEventImageException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public InvalidEventImageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
