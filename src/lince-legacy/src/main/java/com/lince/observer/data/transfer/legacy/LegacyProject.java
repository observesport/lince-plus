package com.lince.observer.data.transfer.legacy;

import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import java.util.List;

/**
 * Legacy state produced by {@link LegacyBridge#toLegacy} for one export (LO-349). {@code instrument} and
 * {@code register} are the current {@link InstrumentoObservacional} and {@link Registro} singletons, so they are
 * only valid inside {@link LegacyBridge#withLegacyModel} / {@link LegacyBridge#export}.
 *
 * @param instrument         the legacy instrument (singleton)
 * @param register           the legacy register of the selected research (singleton), empty for imports
 * @param selectedCriterios  legacy criteria for the export selection, in selection order; what the
 *                           {@code Registro.exportTo*} / {@code InstrumentoObservacional.exportTo*} methods take
 * @param warnings           rows categories that could not be converted
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public record LegacyProject(InstrumentoObservacional instrument, Registro register,
                            List<Criterio> selectedCriterios, List<String> warnings) {

    public LegacyProject {
        selectedCriterios = List.copyOf(selectedCriterios);
        warnings = List.copyOf(warnings);
    }
}
