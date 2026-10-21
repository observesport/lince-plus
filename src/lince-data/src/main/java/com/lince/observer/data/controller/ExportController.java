package com.lince.observer.data.controller;

import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Exports the loaded project (LO-349).
 * <p>
 * {@code GET /api/export/types} lists the export types; {@code GET /api/export/types/{id}} answers 404 for an
 * unknown id. {@code POST /api/export} takes an {@link ExportRequest} JSON body and answers the file bytes with
 * its MIME type and {@code Content-Disposition: attachment; filename=...}; a multi-file export answers one
 * {@link #ZIP_MEDIA_TYPE} archive. Errors: 400 unknown type, unknown criteria/research ids or empty selection;
 * 500 internal error with empty body. Cloud must add {@code /api/export/**} behind its normal auth.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface ExportController {

    String RQ_MAPPING_NAME = "/api/export";
    String TYPES_PATH = "/types";
    String ZIP_MIME_TYPE = "application/zip";
    MediaType ZIP_MEDIA_TYPE = MediaType.parseMediaType(ZIP_MIME_TYPE);

    @RequestMapping(value = TYPES_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExportTypeDescriptor>> getTypes();

    @RequestMapping(value = TYPES_PATH + "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExportTypeDescriptor> getType(@PathVariable("id") String id);

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<byte[]> export(@RequestBody ExportRequest request);
}
