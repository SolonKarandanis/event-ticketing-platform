package com.etp.ticketservice.domain.exception;

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
