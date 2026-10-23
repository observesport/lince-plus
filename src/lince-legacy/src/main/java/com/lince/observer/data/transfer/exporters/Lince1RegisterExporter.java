package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.datos.ControladorArchivos;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Lince 1 register export (LO-349): the selected observer's register as {@code register.rlince}, the bytes
 * the former {@code SaveRegistroAs} command wrote with {@code PackageAwareXMLSerializer}. The legacy format stores every criterion, so the
 * criteria selection does not apply.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class Lince1RegisterExporter implements LinceExporter {

    public static final String TYPE_ID = "lince1-register";
    static final String FILE_NAME = "register.rlince";

    private final LegacyBridge bridge;

    public Lince1RegisterExporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        return new ExportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("lince.register"), "rlince",
                LegacyExportSupport.APPLICATION_XML, false, false, List.of());
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        return bridge.export(selection, legacy -> {
            LegacyExportSupport.logWarnings(TYPE_ID, legacy);
            return List.of(new ExportedFile(FILE_NAME, LegacyExportSupport.APPLICATION_XML, content(legacy.register())));
        });
    }

    /**
     * @return the .rlince bytes of the register, the {@code PackageAwareXMLSerializer} encoding of {@link Registro#getSaveContent()}
     */
    static byte[] content(Registro registro) {
        return ControladorArchivos.getInstance().serializar(registro.getSaveContent());
    }
}
