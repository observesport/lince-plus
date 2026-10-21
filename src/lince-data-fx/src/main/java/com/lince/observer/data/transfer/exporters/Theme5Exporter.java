package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Theme 5 export (LO-349): the register as {@code theme5.csv} and the instrument as {@code theme5.vvt}, the two
 * files the "Theme 5" panel writes with its two buttons. Both carry the same base name, the pairing rule of
 * {@code GenericExportComponent.executePairedExport}. Needs at least one criterion.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Theme5Exporter implements LinceExporter {

    public static final String TYPE_ID = "theme5";
    static final String BASE_NAME = "theme5";

    private final LegacyBridge bridge;

    public Theme5Exporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        return new ExportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("THEME 5"), "csv",
                LegacyExportSupport.TEXT_CSV, true, true, List.of());
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        selection.requireCriteria();
        return bridge.export(selection, legacy -> {
            LegacyExportSupport.logWarnings(TYPE_ID, legacy);
            List<Criterio> criterios = legacy.selectedCriterios();
            return List.of(
                    LegacyExportSupport.text(BASE_NAME + ".csv", LegacyExportSupport.TEXT_CSV,
                            registerContent(legacy.register(), criterios)),
                    LegacyExportSupport.text(BASE_NAME + ".vvt", LegacyExportSupport.TEXT_PLAIN,
                            toolContent(legacy.instrument(), criterios)));
        });
    }

    /**
     * @return the Theme 5 register (.csv) for the criteria, what the "export register" button writes
     */
    public static String registerContent(Registro registro, List<Criterio> criterios) {
        return registro.exportToTheme5(criterios);
    }

    /**
     * @return the Theme instrument (.vvt) for the criteria, what the "export instrument" button writes
     */
    public static String toolContent(InstrumentoObservacional instrument, List<Criterio> criterios) {
        return instrument.exportToTheme(criterios);
    }
}
