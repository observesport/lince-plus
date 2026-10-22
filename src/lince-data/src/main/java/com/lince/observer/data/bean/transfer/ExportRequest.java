package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Body of {@code POST /api/export} (LO-349). The same shape serves every export type.
 *
 * @param type        export type id from {@code GET /api/export/types}
 * @param researchId  register (observer) to export, matched against {@code LinceRegisterWrapper.id} or its
 *                    user profile register code; null exports the first register
 * @param criteriaIds criteria to include, empty means all; never null. See {@code ExportSelection} for the
 *                    identity rule
 * @param options     per-type options as strings, never null
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExportRequest(String type, UUID researchId, List<String> criteriaIds, Map<String, String> options) {

    public ExportRequest {
        criteriaIds = criteriaIds == null ? List.of() : List.copyOf(criteriaIds);
        options = options == null ? Map.of() : Map.copyOf(options);
    }
}
