package com.etp.ticketservice.domain.exception;

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
