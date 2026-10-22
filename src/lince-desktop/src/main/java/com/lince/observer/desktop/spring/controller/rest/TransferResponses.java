package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.transfer.TransferException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Maps {@link TransferException} reasons to HTTP answers for the import and export controllers (LO-349).
 * <p>
 * {@code TYPE_MISMATCH} → 415; {@code IO} → 500 with an empty body; every other reason → 400. 4xx answers carry a
 * {@link TransferError} JSON body. {@code UNKNOWN_TYPE} on {@code GET /types/{id}} is a 404 handled by the
 * controllers themselves.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
final class TransferResponses {

    private TransferResponses() {
    }

    static HttpStatus statusOf(TransferException.Reason reason) {
        return switch (reason) {
            case TYPE_MISMATCH -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case IO -> HttpStatus.INTERNAL_SERVER_ERROR;
            case UNKNOWN_TYPE, UNREADABLE, INSTRUMENT_REQUIRED, EMPTY_SELECTION, UNKNOWN_CRITERIA, UNKNOWN_RESEARCH,
                 INVALID_OPTION ->
                    HttpStatus.BAD_REQUEST;
        };
    }

    /**
     * The body type differs from the endpoint's success type; generics are erased and Jackson serialises the
     * actual body, so the cast is safe.
     */
    static <T> ResponseEntity<T> failure(TransferException e, Logger log, String path) {
        HttpStatus status = statusOf(e.getReason());
        if (status.is5xxServerError()) {
            log.error(path, e);
            return new ResponseEntity<>(status);
        }
        log.warn("{} {}: {}", path, TransferError.code(e.getReason()), e.getMessage());
        return error(status, TransferError.of(e));
    }

    static <T> ResponseEntity<T> badRequest(String reason, String message) {
        return error(HttpStatus.BAD_REQUEST, new TransferError(reason, message, null));
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> error(HttpStatus status, TransferError body) {
        return (ResponseEntity<T>) ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
