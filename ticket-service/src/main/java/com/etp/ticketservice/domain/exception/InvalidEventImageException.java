package com.etp.ticketservice.domain.exception;

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
