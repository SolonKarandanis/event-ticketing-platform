package com.etp.ticketservice.tickettypes.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
