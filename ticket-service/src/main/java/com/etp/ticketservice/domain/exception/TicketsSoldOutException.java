package com.etp.ticketservice.domain.exception;

public class TicketsSoldOutException extends EventTicketException {
    public TicketsSoldOutException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketsSoldOutException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketsSoldOutException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
