package com.etp.ticketservice.domain.exception;

public class EventTicketException extends RuntimeException {
    public EventTicketException() {
    }

    public EventTicketException(String message) {
        super(message);
    }

    public EventTicketException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventTicketException(Throwable cause) {
        super(cause);
    }

    public EventTicketException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
