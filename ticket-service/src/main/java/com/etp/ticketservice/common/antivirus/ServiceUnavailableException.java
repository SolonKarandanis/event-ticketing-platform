package com.etp.ticketservice.common.antivirus;

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
