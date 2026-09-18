package com.etp.ticketservice.user;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class UserNotFoundException extends EventTicketException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public UserNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
