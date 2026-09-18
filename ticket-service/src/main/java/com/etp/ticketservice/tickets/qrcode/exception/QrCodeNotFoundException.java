package com.etp.ticketservice.tickets.qrcode.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
