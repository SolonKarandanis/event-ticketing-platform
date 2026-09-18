package com.etp.ticketservice.tickettypes.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
