package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.data.transfer.legacy.LegacyImport;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Imports a Lince 1 observation instrument ({@code .ilince}) as the target project's observation tool (LO-349).
 * Headless version of the "Import Lince 1 tool" menu entry (the former {@code LoadInstrumentoObservacional} command followed by
 * {@code ensureImportCompatibility()}).
 * <p>
 * Scope {@link ImportScope#FULL} (the default) replaces the instrument, as the menu does; criteria without
 * categories are skipped and, when none is left, the instrument is kept. Existing observations are not touched
 * and keep referring to the previous categories, as in the menu; a warning says so.
 * <p>
 * Scope {@link ImportScope#OBSERVATIONS} imports nothing: a tool file has no observations, and replacing the
 * instrument is exactly what that scope promises not to do. The project is left unchanged and the outcome
 * carries a warning.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Lince1ToolImporter implements LinceImporter {

    public static final String TYPE_ID = "lince1-tool";
    public static final String EXTENSION = "ilince";

    private final LegacyBridge bridge;

    public Lince1ToolImporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ImportTypeDescriptor descriptor() {
        return new ImportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("lince.tool"),
                List.of(EXTENSION), List.of());
    }

    @Override
    public ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options) {
        if (scope == ImportScope.OBSERVATIONS) {
            return LegacyImportSupport.outcome(target, 0,
                    List.of("A Lince 1 tool has no observations: nothing imported (use scope full to replace the instrument)"));
        }
        File file = LegacyImportSupport.spool(in, "." + EXTENSION);
        try {
            return bridge.withLegacyModel(() -> {
                InstrumentoObservacional instrument = loadLegacyTool(bridge, file);
                LegacyImport imported = bridge.fromLegacyInstrument(instrument);
                List<String> warnings = new ArrayList<>();
                int skipped = instrument.getCriterios().length - imported.criteria().size();
                if (skipped > 0) {
                    warnings.add(skipped + " criteria without categories skipped");
                }
                if (imported.criteria().isEmpty()) {
                    warnings.add("The Lince 1 tool has no criteria with categories: instrument unchanged");
                } else {
                    if (LegacyImportSupport.hasObservations(target)) {
                        warnings.add("Instrument replaced: existing observations keep the previous categories");
                    }
                    target.setObservationTool(new ArrayList<Criteria>(imported.criteria()));
                }
                return LegacyImportSupport.outcome(target, 0, warnings);
            });
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    /**
     * Loads a Lince 1 instrument file into the {@link InstrumentoObservacional} singleton and starts an empty
     * {@link Registro}, the core of the former {@code LoadInstrumentoObservacional} command. Runs under the legacy model lock of
     * {@code bridge}; the returned singleton is only stable while the caller holds that lock.
     *
     * @param bridge legacy model lock holder
     * @param file   .ilince file
     * @return the loaded instrument (the singleton)
     * @throws ImportException UNREADABLE when the file is not a Lince 1 instrument
     */
    public static InstrumentoObservacional loadLegacyTool(LegacyBridge bridge, File file) {
        return bridge.withLegacyModel(() -> {
            try {
                InstrumentoObservacional.loadInstance(file);
            } catch (IOException | RuntimeException e) {
                throw new ImportException(TransferException.Reason.UNREADABLE, "Not a Lince 1 tool: " + e.getMessage(), e);
            }
            Registro.loadNewInstance();
            return InstrumentoObservacional.getInstance();
        });
    }
}
