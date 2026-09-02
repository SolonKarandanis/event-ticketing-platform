package com.etp.ticketservice.domain.exception;

public class TooManyEventImagesException extends EventTicketException {
    public TooManyEventImagesException(ErrorCode errorCode) {
        super(errorCode);
    }

    public TooManyEventImagesException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public TooManyEventImagesException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
