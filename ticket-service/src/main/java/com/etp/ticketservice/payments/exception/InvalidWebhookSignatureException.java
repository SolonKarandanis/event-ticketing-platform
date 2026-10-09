package com.etp.ticketservice.payments.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class InvalidWebhookSignatureException extends EventTicketException {
    public InvalidWebhookSignatureException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidWebhookSignatureException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public InvalidWebhookSignatureException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
