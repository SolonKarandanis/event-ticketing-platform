package com.etp.ticketservice.domain.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException() {
        super();
    }

    public ServiceUnavailableException(String msgKey) {
        super(msgKey);
    }

    public ServiceUnavailableException(String msgKey, Throwable t) {
        super(msgKey, t);
    }
}
