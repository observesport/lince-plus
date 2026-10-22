package com.lince.observer.data.transfer.exporters;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.importers.HoisanImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The .mdb carries dates and auto numbers, so it is compared by content, not bytes.
 */
class HoisanExporterTest {

    private final ExporterFixture fixture = new ExporterFixture();
    private final HoisanExporter exporter = new HoisanExporter(fixture.bridge);

    @TempDir
    Path dir;

    @Test
    void descriptor() {
        assertEquals("hoisan", exporter.descriptor().id());
        assertEquals("mdb", exporter.descriptor().extension());
        assertEquals("application/x-msaccess", exporter.descriptor().mimeType());
        assertFalse(exporter.descriptor().supportsCriteriaSelection());
    }

    @Test
    void writesEveryCriterionAndObservationOfTheSelectedObserver() throws Exception {
        List<String> names = fixture.project.getObservationTool().stream().map(Criteria::getName).toList();
        int rows = fixture.project.getRegister().get(0).getRegisterData().size();
        long tempsBefore = Lince1Fixtures.tempFiles("lince-export-");

        List<ExportedFile> files = exporter.export(fixture.selection(), Map.of());

        assertEquals(1, files.size());
        assertEquals("hoisan.mdb", files.get(0).name());
        assertEquals(tempsBefore, Lince1Fixtures.tempFiles("lince-export-"), "temp file deleted");
        File mdb = dir.resolve("out.mdb").toFile();
        Files.write(mdb.toPath(), files.get(0).content());
        Database db = Database.open(mdb, true);
        try {
            List<String> criteria = new ArrayList<>();
            for (java.util.Map<String, Object> row : db.getTable("Criterios")) {
                criteria.add((String) row.get("Nombre_Criterio"));
            }
            assertEquals(names, criteria, "all criteria, the selection does not apply");
            Table times = db.getTable("TiemposObservaciones");
            assertEquals(rows, times.getRowCount());
        } finally {
            db.close();
        }

        LinceFileProjectWrapper back = new LinceFileProjectWrapper();
        new HoisanImporter(fixture.bridge).importInto(back, new ByteArrayInputStream(files.get(0).content()),
                ImportScope.FULL, Map.of());
        assertEquals(names, back.getObservationTool().stream().map(Criteria::getName).toList());
        assertEquals(rows, back.getRegister().get(0).getRegisterData().size());
    }

    @Test
    void legacyWriterClosesTheDatabaseWhenAStoreStepFails() {
        Database[] opened = new Database[1];
        HoisanTool failing = new HoisanTool() {
            @Override
            protected void store(Database db) throws IOException {
                opened[0] = db;
                throw new IOException("forced store failure");
            }
        };

        String error = failing.exportFile(dir.resolve("broken.mdb").toFile());

        assertNotNull(error);
        assertNotNull(opened[0], "store step reached");
        assertFalse(opened[0].getPageChannel().isOpen(), "database closed after the failure");
    }
}
