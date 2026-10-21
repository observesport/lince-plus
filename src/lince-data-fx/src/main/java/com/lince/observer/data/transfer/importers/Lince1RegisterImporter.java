package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LegacyToolException;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.RegistroException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Imports a Lince 1 register ({@code .rlince}) as a new observer of the target project (LO-349). Headless
 * version of the "Import Lince 1 register" menu entry ({@code LoadRegistro} + {@code ensureImportCompatibility()}).
 * <p>
 * A Lince 1 register only stores category codes, which the legacy loader resolves against the current legacy
 * instrument, so the target project must already have an instrument: its criteria are installed as the legacy
 * instrument (what the menu did by migrating the instrument before the load), the file is loaded, and the rows
 * are converted back to observations against the same criteria. Codes the instrument does not know are dropped
 * and reported as warnings, as are the loader's own inconsistencies (criteria or fixed data missing from the
 * instrument), which the menu showed in a dialog without stopping the import. The scope is ignored.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Lince1RegisterImporter implements LinceImporter {

    public static final String TYPE_ID = "lince1-register";
    public static final String EXTENSION = "rlince";
    /**
     * User name of the observer created for the imported rows, as {@code LegacyConverterService} names it.
     */
    public static final String OBSERVER_NAME = "Lince v1 observer";

    private final LegacyBridge bridge;

    public Lince1RegisterImporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ImportTypeDescriptor descriptor() {
        return new ImportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("lince.register"),
                List.of(EXTENSION), List.of());
    }

    @Override
    public ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options) {
        List<Criteria> tool = target.getObservationTool();
        if (tool == null || tool.isEmpty()) {
            throw new ImportException(TransferException.Reason.INSTRUMENT_REQUIRED,
                    "A Lince 1 register needs an observation instrument: import or create the instrument first");
        }
        File file = LegacyImportSupport.spool(in, "." + EXTENSION);
        try {
            return bridge.withLegacyModel(() -> {
                bridge.installInstrument(tool);
                List<String> warnings = new ArrayList<>();
                Registro registro = loadLegacyRegister(bridge, file, warnings);
                List<RegisterItem> rows = bridge.fromLegacyRows(registro.datosVariables, tool, warnings);
                if (!rows.isEmpty()) {
                    LegacyImportSupport.addObserver(target, OBSERVER_NAME, rows);
                }
                return LegacyImportSupport.outcome(target, rows.size(), warnings);
            });
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    /**
     * Loads a Lince 1 register file against the current legacy instrument and makes it the current
     * {@link Registro}, the core of {@code LoadRegistro}. Runs under the legacy model lock of {@code bridge}.
     *
     * @param bridge   legacy model lock holder
     * @param file     .rlince file
     * @param problems receives the loader's inconsistencies (unknown criteria, categories or fixed data); the
     *                 register is loaded anyway, without them
     * @return the loaded register
     * @throws ImportException UNREADABLE when the file is not a Lince 1 register; the current register is kept
     */
    public static Registro loadLegacyRegister(LegacyBridge bridge, File file, List<String> problems) {
        return bridge.withLegacyModel(() -> {
            List<LegacyToolException> errors = new ArrayList<>();
            Registro registro;
            try {
                registro = Registro.cargarRegistro(file, errors);
            } catch (RegistroException | RuntimeException e) {
                throw new ImportException(TransferException.Reason.UNREADABLE, "Not a Lince 1 register: " + e.getMessage(), e);
            }
            List<String> messages = errors.stream().map(Throwable::getMessage).toList();
            if (registro == null) {
                throw new ImportException(TransferException.Reason.UNREADABLE,
                        "Not a Lince 1 register: " + String.join("; ", messages));
            }
            Registro.useInstance(registro);
            problems.addAll(messages);
            return registro;
        });
    }
}
