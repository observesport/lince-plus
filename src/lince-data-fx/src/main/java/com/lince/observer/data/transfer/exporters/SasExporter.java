package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SAS export (LO-349): the register as a tab separated {@code sas.sds} with identical rows collapsed and counted,
 * what {@code SasExportComponent} writes. The export service rejects an empty criteria selection. The panel also
 * offered the fixed and mixed information nodes of a Lince 1 instrument; a converted project has none, so only
 * criteria are selectable. The panel refused an empty register; here an empty register exports an empty file.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class SasExporter implements LinceExporter {

    public static final String TYPE_ID = "sas";
    static final String FILE_NAME = "sas.sds";

    private final LegacyBridge bridge;

    public SasExporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        return new ExportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("SAS"), "sds",
                LegacyExportSupport.TEXT_PLAIN, true, false, List.of());
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        return bridge.export(selection, legacy -> {
            LegacyExportSupport.logWarnings(TYPE_ID, legacy);
            return List.of(ExportedFile.text(FILE_NAME, LegacyExportSupport.TEXT_PLAIN,
                    content(legacy.register(), new ArrayList<>(legacy.selectedCriterios()))));
        });
    }

    /**
     * @param columns criteria ({@code Criterio}) and information nodes ({@code NodoInformacion}) in column order
     * @return the SAS content
     */
    private static String content(Registro registro, List<Object> columns) {
        return registro.exportToSas(columns);
    }
}
