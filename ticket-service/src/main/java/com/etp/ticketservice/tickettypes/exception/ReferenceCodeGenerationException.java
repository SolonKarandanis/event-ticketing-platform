package com.etp.ticketservice.tickettypes.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class ReferenceCodeGenerationException extends EventTicketException {
    public ReferenceCodeGenerationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ReferenceCodeGenerationException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public ReferenceCodeGenerationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
