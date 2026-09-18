package com.etp.ticketservice.tickets.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
