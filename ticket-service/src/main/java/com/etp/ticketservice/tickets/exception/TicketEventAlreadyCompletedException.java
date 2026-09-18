package com.etp.ticketservice.tickets.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
