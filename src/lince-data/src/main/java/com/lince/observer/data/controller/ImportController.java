package com.lince.observer.data.controller;

import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Imports a file into the loaded project (LO-349).
 * <p>
 * {@code GET /api/import/types} lists the importable types. {@code POST /api/import} takes a multipart
 * {@code file}, an optional {@code type} (detected from the extension otherwise), an optional {@code scope}
 * ({@code full} default, {@code observations}) and per-type options as {@code options.<key>} request params.
 * Responses: 200 {@link ImportResult}; 400 undetectable/unknown type or unreadable file; 415 type contradicting
 * the extension; 500 internal error with empty body. Cloud must add {@code /api/import/**} behind its normal auth.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public interface ImportController {

    String RQ_MAPPING_NAME = "/api/import";
    String TYPES_PATH = "/types";
    String FILE_PARAM = "file";
    String TYPE_PARAM = "type";
    String SCOPE_PARAM = "scope";
    String OPTION_PREFIX = "options.";

    @RequestMapping(value = TYPES_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ImportTypeDescriptor>> getTypes();

    @RequestMapping(value = TYPES_PATH + "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ImportTypeDescriptor> getType(@PathVariable("id") String id);

    /**
     * @param file   uploaded file
     * @param type   explicit type id, nullable
     * @param scope  {@code full} | {@code observations}, nullable; parse with
     *               {@link com.lince.observer.data.bean.transfer.ImportScope#fromValue(String)}
     * @param params every request param; {@link #extractOptions(Map)} keeps the {@code options.*} ones
     */
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ImportResult> importFile(@RequestParam(FILE_PARAM) MultipartFile file,
                                            @RequestParam(value = TYPE_PARAM, required = false) String type,
                                            @RequestParam(value = SCOPE_PARAM, required = false) String scope,
                                            @RequestParam Map<String, String> params);

    /**
     * @param params all request params
     * @return the {@code options.<key>} params keyed by {@code <key>}
     */
    static Map<String, String> extractOptions(Map<String, String> params) {
        Map<String, String> options = new LinkedHashMap<>();
        if (params != null) {
            params.forEach((key, value) -> {
                if (StringUtils.startsWith(key, OPTION_PREFIX) && key.length() > OPTION_PREFIX.length()) {
                    options.put(key.substring(OPTION_PREFIX.length()), value);
                }
            });
        }
        return options;
    }
}
