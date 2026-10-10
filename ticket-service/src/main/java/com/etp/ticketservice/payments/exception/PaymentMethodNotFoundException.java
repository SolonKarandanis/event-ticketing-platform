package com.etp.ticketservice.payments.exception;

import com.etp.ticketservice.common.exception.ErrorCode;
import com.etp.ticketservice.common.exception.EventTicketException;

public class PaymentMethodNotFoundException extends EventTicketException {
    public PaymentMethodNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentMethodNotFoundException(ErrorCode errorCode, Object detail) {
        super(errorCode, detail);
    }

    public PaymentMethodNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
