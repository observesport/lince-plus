package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.controller.ExportController;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.TransferException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Desktop implementation of {@link ExportController} (LO-349): {@code POST /api/export} answers the exported file
 * as an attachment, or a {@code <type>-export.zip} when the exporter produced several files. Status codes: see
 * {@link TransferResponses}. {@code Content-Disposition} is exposed to cross-origin callers so the dashboard can
 * read the file name.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@CrossOrigin(maxAge = 3600, exposedHeaders = HttpHeaders.CONTENT_DISPOSITION)
@RestController
@RequestMapping(value = ExportController.RQ_MAPPING_NAME)
public class ExportControllerImpl implements ExportController {

    static final String ZIP_SUFFIX = "-export.zip";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ExportService exportService;

    public ExportControllerImpl(ExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    @RequestMapping(value = TYPES_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExportTypeDescriptor>> getTypes() {
        try {
            return new ResponseEntity<>(exportService.types(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME + TYPES_PATH, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = TYPES_PATH + "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExportTypeDescriptor> getType(@PathVariable("id") String id) {
        try {
            return exportService.type(id)
                    .map(type -> new ResponseEntity<>(type, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME + TYPES_PATH + "/" + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> export(@RequestBody ExportRequest request) {
        if (request == null) {
            return TransferResponses.badRequest(TransferError.code(TransferException.Reason.UNKNOWN_TYPE),
                    "Missing export request");
        }
        try {
            List<ExportedFile> files = exportService.export(request);
            if (files == null || files.isEmpty()) {
                log.error("{}: export type {} produced no file", RQ_MAPPING_NAME, request.type());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (files.size() == 1) {
                ExportedFile file = files.get(0);
                return attachment(file.name(), mediaType(file.mimeType()), file.content());
            }
            return attachment(zipName(request.type()), ZIP_MEDIA_TYPE, zip(files));
        } catch (TransferException e) {
            return TransferResponses.failure(e, log, RQ_MAPPING_NAME);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Names the zip after the exporter id that matched the request (same trimmed, case-insensitive match as the
     * service), so {@code " Theme6"} gives {@code theme6-export.zip}; the trimmed request type otherwise.
     */
    private String zipName(String requestedType) {
        String type = StringUtils.trimToEmpty(requestedType);
        String id = exportService.types().stream()
                .map(ExportTypeDescriptor::id)
                .filter(type::equalsIgnoreCase)
                .findFirst()
                .orElse(type);
        return id + ZIP_SUFFIX;
    }

    private static ResponseEntity<byte[]> attachment(String fileName, MediaType mediaType, byte[] content) {
        ContentDisposition disposition = StandardCharsets.US_ASCII.newEncoder().canEncode(fileName)
                ? ContentDisposition.attachment().filename(fileName).build()
                : ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(disposition);
        headers.setContentLength(content.length);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    private static MediaType mediaType(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    static byte[] zip(List<ExportedFile> files) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (ExportedFile file : files) {
                zip.putNextEntry(new ZipEntry(file.name()));
                zip.write(file.content());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
