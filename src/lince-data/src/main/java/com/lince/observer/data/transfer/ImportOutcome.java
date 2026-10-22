package com.lince.observer.data.transfer;

import com.lince.observer.data.bean.transfer.ImportResult;
import com.lince.observer.data.bean.transfer.ImportScope;

import java.util.List;

/**
 * What a {@link LinceImporter} did to the target project (LO-349). The import service turns it into the
 * {@link ImportResult} returned to the client.
 *
 * @param criteriaCount    criteria in the target project after the import
 * @param registerCount    registers in the target project after the import
 * @param observationCount observations imported
 * @param profileCount     research profiles in the target project after the import
 * @param warnings         non fatal issues, never null
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public record ImportOutcome(int criteriaCount, int registerCount, int observationCount, int profileCount,
                            List<String> warnings) {

    public ImportOutcome {
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    /**
     * @param type  import type id actually used
     * @param scope scope applied; {@link ImportScope#FULL} for types that ignore it
     * @return the REST response body
     */
    public ImportResult toResult(String type, ImportScope scope) {
        return new ImportResult(type, scope, criteriaCount, registerCount, observationCount, profileCount, warnings);
    }
}
