package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.controller.ImportController;
import com.lince.observer.data.service.ImportService;
import com.lince.observer.data.transfer.TransferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Desktop implementation of {@link ImportController} (LO-349): {@code POST /api/import} merges an uploaded file
 * into the loaded project. Status codes: see {@link TransferResponses}; an invalid {@code scope} or an empty upload
 * is a 400 as well.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(value = ImportController.RQ_MAPPING_NAME)
public class ImportControllerImpl implements ImportController {

    static final String INVALID_SCOPE = "invalid_scope";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ImportService importService;

    public ImportControllerImpl(ImportService importService) {
        this.importService = importService;
    }

    @Override
    @RequestMapping(value = TYPES_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ImportTypeDescriptor>> getTypes() {
        try {
            return new ResponseEntity<>(importService.types(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME + TYPES_PATH, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = TYPES_PATH + "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportTypeDescriptor> getType(@PathVariable("id") String id) {
        try {
            return importService.type(id)
                    .map(type -> new ResponseEntity<>(type, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME + TYPES_PATH + "/" + id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportResult> importFile(@RequestParam(FILE_PARAM) MultipartFile file,
                                                   @RequestParam(value = TYPE_PARAM, required = false) String type,
                                                   @RequestParam(value = SCOPE_PARAM, required = false) String scope,
                                                   @RequestParam Map<String, String> params) {
        ImportScope importScope;
        try {
            importScope = ImportScope.fromValue(scope);
        } catch (IllegalArgumentException e) {
            log.warn("{} {}: {}", RQ_MAPPING_NAME, INVALID_SCOPE, e.getMessage());
            return TransferResponses.badRequest(INVALID_SCOPE, e.getMessage());
        }
        if (file == null || file.isEmpty()) {
            return TransferResponses.badRequest(TransferError.code(TransferException.Reason.UNREADABLE), "Empty file");
        }
        try (InputStream in = file.getInputStream()) {
            ImportResult result = importService.importFile(file.getOriginalFilename(), in, type, importScope,
                    ImportController.extractOptions(params));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (TransferException e) {
            return TransferResponses.failure(e, log, RQ_MAPPING_NAME);
        } catch (Exception e) {
            log.error(RQ_MAPPING_NAME, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
