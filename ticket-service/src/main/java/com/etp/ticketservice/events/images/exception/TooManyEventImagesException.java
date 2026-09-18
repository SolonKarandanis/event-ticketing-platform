package com.etp.ticketservice.events.images.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
