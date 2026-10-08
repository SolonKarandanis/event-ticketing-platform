package com.etp.ticketservice.tickettypes.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class TicketTypeNotInEventException extends EventTicketException {
    public TicketTypeNotInEventException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TicketTypeNotInEventException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TicketTypeNotInEventException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
