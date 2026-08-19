package com.etp.ticketservice.domain.exception;

public class QrCodeGenerationException extends EventTicketException {
    public QrCodeGenerationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public QrCodeGenerationException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public QrCodeGenerationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
