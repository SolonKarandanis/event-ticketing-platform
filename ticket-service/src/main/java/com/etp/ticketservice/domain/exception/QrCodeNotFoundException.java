package com.etp.ticketservice.domain.exception;

public class QrCodeNotFoundException extends EventTicketException {
    public QrCodeNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public QrCodeNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public QrCodeNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
