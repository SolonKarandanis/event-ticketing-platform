package com.etp.ticketservice.domain.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND("error.user.not-found", HttpStatus.BAD_REQUEST),
    VENUE_NOT_FOUND("error.venue.not-found", HttpStatus.BAD_REQUEST),
    VENUE_ID_REQUIRED("error.venue.id-required", HttpStatus.BAD_REQUEST),
    VENUE_ID_MISMATCH("error.venue.id-mismatch", HttpStatus.BAD_REQUEST),
    EVENT_NOT_FOUND("error.event.not-found", HttpStatus.BAD_REQUEST),
    EVENT_ID_REQUIRED("error.event.id-required", HttpStatus.BAD_REQUEST),
    EVENT_ID_MISMATCH("error.event.id-mismatch", HttpStatus.BAD_REQUEST),
    TICKET_TYPE_NOT_FOUND("error.ticket-type.not-found", HttpStatus.BAD_REQUEST),
    TICKET_NOT_FOUND("error.ticket.not-found", HttpStatus.BAD_REQUEST),
    TICKET_SOLD_OUT("error.ticket.sold-out", HttpStatus.CONFLICT),
    QR_CODE_GENERATION_FAILED("error.qr-code.generation-failed", HttpStatus.INTERNAL_SERVER_ERROR),
    QR_CODE_NOT_FOUND("error.qr-code.not-found", HttpStatus.BAD_REQUEST);

    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
