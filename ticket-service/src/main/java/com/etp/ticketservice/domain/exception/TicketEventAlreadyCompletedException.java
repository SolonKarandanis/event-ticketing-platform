package com.etp.ticketservice.domain.exception;

public class TicketEventAlreadyCompletedException extends EventTicketException {
    public TicketEventAlreadyCompletedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketEventAlreadyCompletedException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketEventAlreadyCompletedException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
