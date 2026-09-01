package com.etp.ticketservice.domain.exception;

public class TicketAlreadyCancelledException extends EventTicketException {
    public TicketAlreadyCancelledException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketAlreadyCancelledException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketAlreadyCancelledException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
