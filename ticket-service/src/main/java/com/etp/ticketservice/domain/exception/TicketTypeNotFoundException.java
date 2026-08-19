package com.etp.ticketservice.domain.exception;

public class TicketTypeNotFoundException extends EventTicketException {
    public TicketTypeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketTypeNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketTypeNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
