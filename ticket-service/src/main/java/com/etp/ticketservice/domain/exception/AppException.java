package com.etp.ticketservice.domain.exception;

public class AppException extends RuntimeException{
    public AppException(String msgKey) {
        super(msgKey);
    }
}
