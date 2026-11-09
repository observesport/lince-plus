package com.lince.observer.desktop.spring.controller.rest;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

/**
 * Turns request binding failures on {@code /api/import} and {@code /api/export} into a 400 with a
 * {@link TransferError} body ({@code reason: "invalid_request"}) instead of Spring's default error page (LO-349):
 * a missing multipart {@code file}, an unreadable JSON body (for example a non-UUID {@code researchId}), or a
 * request parameter of the wrong type.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@RestControllerAdvice(assignableTypes = {ImportControllerImpl.class, ExportControllerImpl.class})
public class TransferExceptionHandler {

    static final String INVALID_REQUEST = "invalid_request";

    private static final Logger log = LoggerFactory.getLogger(TransferExceptionHandler.class);

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<TransferError> missingPart(MissingServletRequestPartException e) {
        return invalid("Missing request part: " + e.getRequestPartName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<TransferError> unreadableBody(HttpMessageNotReadableException e) {
        String message = "Malformed request body";
        if (e.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            message += ": invalid value for " + mismatch.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
                    .collect(Collectors.joining("."));
        }
        return invalid(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<TransferError> typeMismatch(MethodArgumentTypeMismatchException e) {
        return invalid("Invalid value for " + e.getName() + ": " + e.getValue());
    }

    private static ResponseEntity<TransferError> invalid(String message) {
        log.warn("{}: {}", INVALID_REQUEST, message);
        return TransferResponses.badRequest(INVALID_REQUEST, message);
    }
}
