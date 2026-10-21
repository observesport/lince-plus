package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.datos.ControladorArchivos;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Lince 1 tool export (LO-349): the project's instrument as {@code tool.ilince}, the bytes
 * the former {@code SaveInstrumentoObservacionalAs} command wrote through {@code InstrumentoObservacional.save()}. The whole
 * instrument is written, so the criteria selection does not apply.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Lince1ToolExporter implements LinceExporter {

    public static final String TYPE_ID = "lince1-tool";
    static final String FILE_NAME = "tool.ilince";

    private final LegacyBridge bridge;

    public Lince1ToolExporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        return new ExportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("lince.tool"), "ilince",
                LegacyExportSupport.APPLICATION_XML, false, false, List.of());
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        return bridge.export(selection, legacy -> List.of(
                new ExportedFile(FILE_NAME, LegacyExportSupport.APPLICATION_XML, content(legacy.instrument()))));
    }

    /**
     * @return the .ilince bytes of the instrument, identical to what {@link InstrumentoObservacional#save()} writes
     */
    public static byte[] content(InstrumentoObservacional instrument) {
        return ControladorArchivos.getInstance().serializar(instrument.getSaveContent());
    }
}
