package com.etp.ticketservice.domain.exception;

public class EventNotPublishableException extends EventTicketException {
    public EventNotPublishableException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EventNotPublishableException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public EventNotPublishableException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
