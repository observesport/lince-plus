package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.controller.ExportController;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException.Reason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportControllerImplTest {

    private ExportService exportService;
    private ExportControllerImpl controller;

    @BeforeEach
    void setUp() {
        exportService = mock(ExportService.class);
        controller = new ExportControllerImpl(exportService);
    }

    @Test
    void getTypesReturnsServiceValue() {
        List<ExportTypeDescriptor> types = List.of(descriptor("csv"));
        when(exportService.types()).thenReturn(types);

        ResponseEntity<List<ExportTypeDescriptor>> response = controller.getTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(types, response.getBody());
    }

    @Test
    void getTypesReturns500WhenServiceFails() {
        when(exportService.types()).thenThrow(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getTypes().getStatusCode());
    }

    @Test
    void getTypeReturnsTheDescriptorOr404() {
        when(exportService.type("csv")).thenReturn(Optional.of(descriptor("csv")));
        when(exportService.type("nope")).thenReturn(Optional.empty());

        assertEquals("csv", controller.getType("csv").getBody().id());
        ResponseEntity<ExportTypeDescriptor> missing = controller.getType("nope");
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertNull(missing.getBody());
    }

    @Test
    void singleFileIsReturnedWithItsMimeTypeAndAttachmentName() {
        byte[] content = "a,b\n1,2\n".getBytes(StandardCharsets.UTF_8);
        ExportRequest request = new ExportRequest("csv", null, List.of(), Map.of());
        when(exportService.export(request)).thenReturn(List.of(new ExportedFile("project.csv", "text/csv", content)));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(content, response.getBody());
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"project.csv\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(content.length, response.getHeaders().getContentLength());
    }

    @Test
    void singleFileWithoutMimeTypeIsOctetStream() {
        ExportRequest request = new ExportRequest("hoisan", null, null, null);
        when(exportService.export(request)).thenReturn(List.of(new ExportedFile("db.mdb", null, new byte[]{1})));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    void severalFilesAreZipped() throws IOException {
        ExportRequest request = new ExportRequest("theme6", null, null, null);
        when(exportService.export(request)).thenReturn(List.of(
                new ExportedFile("project.txt", "text/plain", "rows".getBytes(StandardCharsets.UTF_8)),
                new ExportedFile("vvt.vvt", "text/plain", "vvt".getBytes(StandardCharsets.UTF_8))));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ExportController.ZIP_MEDIA_TYPE, response.getHeaders().getContentType());
        assertEquals("attachment; filename=\"theme6-export.zip\"",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        Map<String, String> entries = unzip(response.getBody());
        assertEquals(List.of("project.txt", "vvt.vvt"), List.copyOf(entries.keySet()));
        assertEquals("rows", entries.get("project.txt"));
        assertEquals("vvt", entries.get("vvt.vvt"));
    }

    @Test
    void unknownTypeIs400OnPost() {
        ExportRequest request = new ExportRequest("excel", null, null, null);
        when(exportService.export(request)).thenThrow(new ExportException(Reason.UNKNOWN_TYPE, "Unknown export type: excel"));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        TransferError error = error(response);
        assertEquals("unknown_type", error.reason());
        assertEquals("Unknown export type: excel", error.message());
    }

    @Test
    void unknownCriteriaIs400ListingTheIds() {
        ExportRequest request = new ExportRequest("csv", null, List.of("99", "x"), null);
        when(exportService.export(request)).thenThrow(
                new ExportException(Reason.UNKNOWN_CRITERIA, "Unknown criteria ids: 99, x", List.of("99", "x")));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("unknown_criteria", error(response).reason());
        assertEquals(List.of("99", "x"), error(response).ids());
    }

    @Test
    void unknownResearchAndEmptySelectionAre400() {
        ExportRequest request = new ExportRequest("csv", null, null, null);
        when(exportService.export(request)).thenThrow(new ExportException(Reason.UNKNOWN_RESEARCH, "r", List.of("id")));
        assertEquals("unknown_research", error(controller.export(request)).reason());

        reset(exportService);
        when(exportService.export(request)).thenThrow(new ExportException(Reason.EMPTY_SELECTION, "Select at least one criteria"));
        ResponseEntity<byte[]> response = controller.export(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("empty_selection", error(response).reason());
    }

    @Test
    void invalidOptionIs400NamingTheOption() {
        ExportRequest request = new ExportRequest("csv", null, null, Map.of("separator", "tab"));
        when(exportService.export(request)).thenThrow(new ExportException(Reason.INVALID_OPTION,
                "Unknown CSV separator: tab (expected comma or semicolon)", List.of("separator")));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertEquals("invalid_option", error(response).reason());
        assertEquals(List.of("separator"), error(response).ids());
    }

    @Test
    void nullRequestIs400() {
        ResponseEntity<byte[]> response = controller.export(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("unknown_type", error(response).reason());
        verifyNoInteractions(exportService);
    }

    @Test
    void ioReasonIs500WithEmptyBody() {
        ExportRequest request = new ExportRequest("hoisan", null, null, null);
        when(exportService.export(request)).thenThrow(new ExportException(Reason.IO, "no driver"));

        ResponseEntity<byte[]> response = controller.export(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void unexpectedFailureAndNoFilesAre500() {
        ExportRequest request = new ExportRequest("csv", null, null, null);
        when(exportService.export(request)).thenThrow(new IllegalStateException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.export(request).getStatusCode());

        reset(exportService);
        when(exportService.export(request)).thenReturn(List.of());
        ResponseEntity<byte[]> response = controller.export(request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    private static ExportTypeDescriptor descriptor(String id) {
        return new ExportTypeDescriptor(id, id, "csv", "text/csv", true, false, null);
    }

    private static Map<String, String> unzip(byte[] zip) throws IOException {
        Map<String, String> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                entries.put(entry.getName(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }

    private static TransferError error(ResponseEntity<?> response) {
        Object body = response.getBody();
        assertInstanceOf(TransferError.class, body);
        return (TransferError) body;
    }
}
