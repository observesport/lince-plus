package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * HOISAN export (LO-349): the whole instrument and the selected observer's register as a HOISAN Access database
 * ({@code hoisan.mdb}), what the former {@code AbrirExportarHoisan} command wrote. The legacy writer always exports every criterion,
 * so the criteria selection does not apply. The database is built in a temp file that is deleted afterwards.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class HoisanExporter implements LinceExporter {

    public static final String TYPE_ID = "hoisan";
    private static final String MIME_TYPE = "application/x-msaccess";
    static final String FILE_NAME = "hoisan.mdb";

    private final LegacyBridge bridge;

    public HoisanExporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        return new ExportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("HOISAN"), "mdb", MIME_TYPE,
                false, false, List.of());
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        return bridge.export(selection, legacy -> {
            LegacyExportSupport.logWarnings(TYPE_ID, legacy);
            Path tmp = null;
            try {
                tmp = Files.createTempFile("lince-export-", ".mdb");
                writeTo(bridge, tmp.toFile());
                return List.of(new ExportedFile(FILE_NAME, MIME_TYPE, Files.readAllBytes(tmp)));
            } catch (IOException e) {
                throw new ExportException(TransferException.Reason.IO, "HOISAN export failed: " + e.getMessage(), e);
            } finally {
                FileUtils.deleteQuietly(tmp == null ? null : tmp.toFile());
            }
        });
    }

    /**
     * Writes the current legacy instrument and register as a HOISAN database, under the legacy model lock of
     * {@code bridge}.
     *
     * @param bridge legacy model lock holder
     * @param file   target, its extension is forced to .mdb by the legacy writer
     * @throws ExportException IO when the database cannot be written
     */
    private static void writeTo(LegacyBridge bridge, File file) {
        String error = bridge.withLegacyModel(() -> new HoisanTool().exportFile(file));
        if (error != null) {
            throw new ExportException(TransferException.Reason.IO, "HOISAN export failed: " + error);
        }
    }
}
