package com.etp.ticketservice.domain.exception;

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
