package com.etp.ticketservice.domain.exception;

public class TicketAlreadyValidatedException extends EventTicketException {
    public TicketAlreadyValidatedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketAlreadyValidatedException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketAlreadyValidatedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
