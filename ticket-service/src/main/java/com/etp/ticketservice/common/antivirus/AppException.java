package com.etp.ticketservice.common.antivirus;

public class AppException extends RuntimeException{
    public AppException(String msgKey) {
        super(msgKey);
    }
}
