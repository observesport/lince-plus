package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.service.ImportService;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.TransferException.Reason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImportControllerImplTest {

    private ImportService importService;
    private ImportControllerImpl controller;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        importService = mock(ImportService.class);
        controller = new ImportControllerImpl(importService);
        file = multipart("project.xml", "<xml/>".getBytes());
    }

    @Test
    void getTypesReturnsServiceValue() {
        List<ImportTypeDescriptor> types = List.of(new ImportTypeDescriptor("lince-plus", "Lince PLUS", List.of("xml"), null));
        when(importService.types()).thenReturn(types);

        ResponseEntity<List<ImportTypeDescriptor>> response = controller.getTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(types, response.getBody());
    }

    @Test
    void getTypesReturns500WhenServiceFails() {
        when(importService.types()).thenThrow(new IllegalStateException("boom"));

        ResponseEntity<List<ImportTypeDescriptor>> response = controller.getTypes();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getTypeReturnsTheDescriptorOr404() {
        ImportTypeDescriptor hoisan = new ImportTypeDescriptor("hoisan", "HOISAN", List.of("mdb"), null);
        when(importService.type("hoisan")).thenReturn(Optional.of(hoisan));
        when(importService.type("nope")).thenReturn(Optional.empty());

        assertEquals(hoisan, controller.getType("hoisan").getBody());
        assertEquals(HttpStatus.NOT_FOUND, controller.getType("nope").getStatusCode());
    }

    @Test
    void importFilePassesNameStreamTypeScopeAndOptions() {
        ImportResult result = new ImportResult("lince-plus", ImportScope.OBSERVATIONS, 3, 1, 10, 1, List.of());
        when(importService.importFile(any(), any(), any(), any(), any())).thenReturn(result);
        Map<String, String> params = Map.of("type", "lince-plus", "scope", "observations", "options.k", "v");

        ResponseEntity<ImportResult> response = controller.importFile(file, "lince-plus", "observations", params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());
        verify(importService).importFile(eq("project.xml"), any(InputStream.class), eq("lince-plus"),
                eq(ImportScope.OBSERVATIONS), eq(Map.of("k", "v")));
    }

    @Test
    void blankScopeMeansFull() {
        when(importService.importFile(any(), any(), any(), any(), any()))
                .thenReturn(new ImportResult("lince-plus", ImportScope.FULL, 0, 0, 0, 0, null));

        controller.importFile(file, null, "", Map.of());

        verify(importService).importFile(eq("project.xml"), any(InputStream.class), isNull(), eq(ImportScope.FULL), eq(Map.of()));
    }

    @Test
    void invalidScopeIs400() {
        ResponseEntity<ImportResult> response = controller.importFile(file, null, "everything", Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("invalid_scope", error(response).reason());
        verifyNoInteractions(importService);
    }

    @Test
    void emptyFileIs400Unreadable() {
        MultipartFile empty = multipart("project.xml", new byte[0]);

        ResponseEntity<ImportResult> response = controller.importFile(empty, null, null, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("unreadable", error(response).reason());
    }

    @Test
    void unknownTypeIs400WithReasonAndMessage() {
        failWith(new ImportException(Reason.UNKNOWN_TYPE, "Cannot detect the type of data.dat; send 'type'"));

        ResponseEntity<ImportResult> response = controller.importFile(file, null, null, Map.of());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        TransferError error = error(response);
        assertEquals("unknown_type", error.reason());
        assertEquals("Cannot detect the type of data.dat; send 'type'", error.message());
        assertNull(error.ids());
    }

    @Test
    void typeMismatchIs415() {
        failWith(new ImportException(Reason.TYPE_MISMATCH, "hoisan does not read .xml"));

        ResponseEntity<ImportResult> response = controller.importFile(file, "hoisan", null, Map.of());

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("type_mismatch", error(response).reason());
    }

    @Test
    void unreadableAndInstrumentRequiredAre400() {
        failWith(new ImportException(Reason.UNREADABLE, "bad"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.importFile(file, null, null, Map.of()).getStatusCode());

        failWith(new ImportException(Reason.INSTRUMENT_REQUIRED, "no instrument"));
        ResponseEntity<ImportResult> response = controller.importFile(file, null, null, Map.of());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("instrument_required", error(response).reason());
    }

    @Test
    void ioReasonIs500WithEmptyBody() {
        failWith(new ImportException(Reason.IO, "disk", new IOException("disk")));

        ResponseEntity<ImportResult> response = controller.importFile(file, null, null, Map.of());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void unexpectedFailureIs500WithEmptyBody() {
        failWith(new IllegalStateException("boom"));

        ResponseEntity<ImportResult> response = controller.importFile(file, null, null, Map.of());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void unreadableUploadStreamIs500() throws IOException {
        MultipartFile broken = mock(MultipartFile.class);
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getOriginalFilename()).thenReturn("project.xml");
        when(broken.getInputStream()).thenThrow(new IOException("gone"));

        ResponseEntity<ImportResult> response = controller.importFile(broken, null, null, Map.of());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    private static MultipartFile multipart(String name, byte[] content) {
        MultipartFile multipart = mock(MultipartFile.class);
        when(multipart.getOriginalFilename()).thenReturn(name);
        when(multipart.isEmpty()).thenReturn(content.length == 0);
        when(multipart.getSize()).thenReturn((long) content.length);
        try {
            when(multipart.getInputStream()).thenAnswer(invocation -> new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return multipart;
    }

    private void failWith(RuntimeException e) {
        doThrow(e).when(importService).importFile(any(), any(), any(), any(), any());
    }

    private static TransferError error(ResponseEntity<?> response) {
        Object body = response.getBody();
        assertInstanceOf(TransferError.class, body);
        return (TransferError) body;
    }
}
