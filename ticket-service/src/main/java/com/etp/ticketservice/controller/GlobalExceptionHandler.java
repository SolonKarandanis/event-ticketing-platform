package com.etp.ticketservice.controller;

import com.etp.ticketservice.domain.dto.response.ErrorDto;
import com.etp.ticketservice.domain.exception.EventTicketException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        log.error("Caught exception", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.unknown"));
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("Caught ConstraintViolationException", ex);

        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse(resolve("error.constraint-violation"));

        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("Caught MethodArgumentNotValidException", ex);
        ErrorDto errorDto = new ErrorDto();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse(resolve("error.validation-failed"));

        errorDto.setError(errorMessage);
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Spring throws this itself once a multipart request exceeds
    // spring.servlet.multipart.max-file-size/max-request-size -- without this handler it
    // falls through to the generic Exception handler above and comes back as a 500
    // "unknown error", which is wrong for something the client actually caused. The only
    // multipart endpoints in this app are create/update event's image uploads, so this
    // message is safe to be specific rather than generic.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorDto> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("Caught MaxUploadSizeExceededException", ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve("error.event.image-invalid-file"));
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // Every domain exception carries an ErrorCode -- its HTTP status and client-facing
    // message both come from that code, so one handler covers all of them.
    @ExceptionHandler(EventTicketException.class)
    public ResponseEntity<ErrorDto> handleEventTicketException(EventTicketException ex) {
        log.error("Caught {}", ex.getClass().getSimpleName(), ex);
        ErrorDto errorDto = new ErrorDto();
        errorDto.setError(resolve(ex.getErrorCode().getMessageKey()));
        return new ResponseEntity<>(errorDto, ex.getErrorCode().getHttpStatus());
    }

    private String resolve(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
