package com.etp.ticketservice.domain.exception;

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
