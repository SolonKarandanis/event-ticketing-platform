package com.etp.ticketservice.payments.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class PaymentGatewayException extends EventTicketException {
    public PaymentGatewayException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentGatewayException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public PaymentGatewayException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
