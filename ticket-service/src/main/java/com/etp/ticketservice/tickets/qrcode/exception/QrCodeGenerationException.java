package com.etp.ticketservice.tickets.qrcode.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

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
