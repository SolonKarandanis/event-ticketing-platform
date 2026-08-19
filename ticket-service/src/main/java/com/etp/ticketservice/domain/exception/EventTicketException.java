package com.etp.ticketservice.domain.exception;

import lombok.Getter;

@Getter
public class EventTicketException extends RuntimeException {
    private final ErrorCode errorCode;

    public EventTicketException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public EventTicketException(ErrorCode errorCode, Object detail) {
        super(errorCode.name() + ": " + detail);
        this.errorCode = errorCode;
    }

    public EventTicketException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }
}
