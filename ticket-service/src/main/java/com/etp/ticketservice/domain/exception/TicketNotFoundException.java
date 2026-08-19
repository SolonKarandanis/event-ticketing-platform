package com.etp.ticketservice.domain.exception;

public class TicketNotFoundException extends EventTicketException {
    public TicketNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
