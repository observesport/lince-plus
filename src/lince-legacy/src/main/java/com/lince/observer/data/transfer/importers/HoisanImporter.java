package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.transfer.ImportTypeDescriptor;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.data.transfer.legacy.LegacyImport;
import com.lince.observer.legacy.Registro;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Imports a HOISAN Access database ({@code .mdb}) into the target project (LO-349). Headless version of the
 * "Import HOISAN" menu entry (the former {@code AbrirImportarHoisan} command followed by
 * {@code ensureImportCompatibility()}).
 * <p>
 * The upload is spooled to a temp file (the reader needs a {@link File}) that is deleted afterwards. The HOISAN
 * criteria replace the instrument, as in the menu (kept when the database has no criteria with categories), and
 * each HOISAN video becomes a new observer named after the video. The menu only migrated the last video's rows,
 * as "Lince v1 observer", and wrote the Lince 1 files next to the database; this importer writes no files. The
 * scope is ignored.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class HoisanImporter implements LinceImporter {

    public static final String TYPE_ID = "hoisan";
    public static final String EXTENSION = "mdb";

    private static final Logger LOG = LoggerFactory.getLogger(HoisanImporter.class);
    private static final String NOT_HOISAN = "Not a HOISAN database";

    private final LegacyBridge bridge;

    public HoisanImporter(LegacyBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public ImportTypeDescriptor descriptor() {
        return new ImportTypeDescriptor(TYPE_ID, ResourceBundleHelper.getI18NLabel("HOISAN"), List.of(EXTENSION), List.of());
    }

    @Override
    public ImportOutcome importInto(ILinceProject target, InputStream in, ImportScope scope, Map<String, String> options) {
        File file = LegacyImportSupport.spool(in, "." + EXTENSION);
        try {
            return bridge.withLegacyModel(() -> {
                HoisanTool.HoisanData data = read(file);
                List<String> warnings = new ArrayList<>();
                LegacyImport imported = bridge.fromLegacyInstrument(data.instrument());
                List<Criteria> tool;
                if (imported.criteria().isEmpty()) {
                    warnings.add("The HOISAN database has no criteria with categories: instrument unchanged");
                    tool = target.getObservationTool();
                } else {
                    tool = new ArrayList<>(imported.criteria());
                    target.setObservationTool(tool);
                }
                int observations = 0;
                for (Map.Entry<String, Registro> video : data.registers().entrySet()) {
                    List<RegisterItem> rows = bridge.fromLegacyRows(video.getValue().datosVariables, tool, warnings);
                    if (!rows.isEmpty()) {
                        LegacyImportSupport.addObserver(target, observerName(video.getKey()), rows);
                        observations += rows.size();
                    }
                }
                return ImportOutcome.of(target, observations, warnings);
            });
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    private static HoisanTool.HoisanData read(File file) {
        try {
            return new HoisanTool().read(file);
        } catch (IOException | RuntimeException e) {
            // the cause can name server paths (the upload spool, a linked table's file): log it, never echo it
            LOG.warn("Unreadable HOISAN database", e);
            throw new ImportException(TransferException.Reason.UNREADABLE, NOT_HOISAN, e);
        }
    }

    private static String observerName(String videoName) {
        String name = StringUtils.substringAfterLast(StringUtils.replace(StringUtils.defaultString(videoName), "\\", "/"), "/");
        if (name.isEmpty()) {
            name = StringUtils.defaultString(videoName);
        }
        return StringUtils.defaultIfBlank(name, "HOISAN observer");
    }
}
