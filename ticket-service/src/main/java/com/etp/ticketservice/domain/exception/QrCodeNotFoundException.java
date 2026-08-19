package com.etp.ticketservice.domain.exception;

public class QrCodeNotFoundException extends EventTicketException {
    public QrCodeNotFoundException() {
    }

    public QrCodeNotFoundException(String message) {
        super(message);
    }

    public QrCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QrCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public QrCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
