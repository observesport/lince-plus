package com.lince.observer.desktop.spring.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.lince.observer.data.bean.transfer.ExportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * spring-test (MockMvc) is not on the lince-desktop test classpath, so the handler methods are called directly.
 */
class TransferExceptionHandlerTest {

    private final TransferExceptionHandler handler = new TransferExceptionHandler();

    @Test
    void appliesOnlyToTheImportAndExportControllers() {
        RestControllerAdvice advice = TransferExceptionHandler.class.getAnnotation(RestControllerAdvice.class);

        assertNotNull(advice);
        assertEquals(List.of(ImportControllerImpl.class, ExportControllerImpl.class), List.of(advice.assignableTypes()));
    }

    @Test
    void missingPartIs400InvalidRequest() {
        TransferError error = body(handler.missingPart(new MissingServletRequestPartException("file")));

        assertEquals("invalid_request", error.reason());
        assertTrue(error.message().contains("file"), error.message());
        assertNull(error.ids());
    }

    @Test
    void unreadableBodyIs400InvalidRequest() {
        TransferError error = body(handler.unreadableBody(
                new HttpMessageNotReadableException("JSON parse error", mock(HttpInputMessage.class))));

        assertEquals("invalid_request", error.reason());
        assertEquals("Malformed request body", error.message());
    }

    @Test
    void nonUuidResearchIdInTheBodyNamesTheField() {
        InvalidFormatException cause = assertThrows(InvalidFormatException.class, () -> new ObjectMapper()
                .readValue("{\"type\":\"csv\",\"researchId\":\"nope\"}", ExportRequest.class));

        TransferError error = body(handler.unreadableBody(
                new HttpMessageNotReadableException("JSON parse error", cause, mock(HttpInputMessage.class))));

        assertEquals("invalid_request", error.reason());
        assertEquals("Malformed request body: invalid value for researchId", error.message());
    }

    @Test
    void typeMismatchIs400NamingTheParameter() {
        TransferError error = body(handler.typeMismatch(new MethodArgumentTypeMismatchException(
                "nope", UUID.class, "researchId", null, new IllegalArgumentException("bad uuid"))));

        assertEquals("invalid_request", error.reason());
        assertEquals("Invalid value for researchId: nope", error.message());
    }

    private static TransferError body(ResponseEntity<TransferError> response) {
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        return response.getBody();
    }
}
