package com.etp.ticketservice.domain.exception;

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
