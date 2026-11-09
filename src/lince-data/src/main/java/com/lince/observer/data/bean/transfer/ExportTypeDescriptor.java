package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Describes one export type for {@code GET /api/export/types} (LO-349).
 *
 * @param id                        stable type id sent as {@link ExportRequest#type()}
 * @param label                     human readable label
 * @param extension                 extension of the main file, without the dot
 * @param mimeType                  MIME type of the main file (a multi-file export is returned as a zip)
 * @param supportsCriteriaSelection true when {@link ExportRequest#criteriaIds()} filters the output
 * @param multiFile                 true when the export produces several files, returned as one zip
 * @param options                   per-type options, never null
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExportTypeDescriptor(String id, String label, String extension, String mimeType,
                                   boolean supportsCriteriaSelection, boolean multiFile,
                                   List<TransferOption> options) {

    public ExportTypeDescriptor {
        options = options == null ? List.of() : List.copyOf(options);
    }
}
