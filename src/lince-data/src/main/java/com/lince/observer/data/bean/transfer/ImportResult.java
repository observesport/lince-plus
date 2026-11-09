package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response of {@code POST /api/import} (LO-349): what was imported and the resulting project counts.
 *
 * @param type             import type id actually used
 * @param scope            scope applied, null for types that ignore it
 * @param criteriaCount    criteria in the project after the import
 * @param registerCount    registers (observers) in the project after the import
 * @param observationCount observations imported
 * @param profileCount     research profiles in the project after the import
 * @param warnings         non fatal issues (unmatched criteria, skipped rows), never null
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImportResult(String type, ImportScope scope, int criteriaCount, int registerCount,
                           int observationCount, int profileCount, List<String> warnings) {

    public ImportResult {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }
}
