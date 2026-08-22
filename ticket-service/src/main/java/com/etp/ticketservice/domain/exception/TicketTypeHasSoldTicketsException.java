package com.etp.ticketservice.domain.exception;

public class TicketTypeHasSoldTicketsException extends EventTicketException {
    public TicketTypeHasSoldTicketsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketTypeHasSoldTicketsException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketTypeHasSoldTicketsException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
