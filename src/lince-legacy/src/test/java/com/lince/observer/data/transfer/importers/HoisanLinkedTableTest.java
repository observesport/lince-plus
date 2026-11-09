package com.lince.observer.data.transfer.importers;

import com.healthmarketscience.jackcess.Database;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A HOISAN upload can declare a HOISAN table as an Access linked table naming any path (or a UNC share). Jackcess
 * would open that file read-write when the table is read, and its error text differs for missing and existing
 * files, so the import must refuse the link without touching the file or echoing the path (LO-349). The fixtures
 * are built with Jackcess {@code createLinkedTable}, which writes the same catalog row Access does.
 */
class HoisanLinkedTableTest {

    private final HoisanImporter importer = new HoisanImporter(new LegacyBridge());

    @TempDir
    Path dir;

    @Test
    void aLinkedHoisanTableIsNotFollowedWhenTheLinkedFileIsMissing() throws Exception {
        File missing = dir.resolve("secret-missing.mdb").toFile();

        assertLinkedTableRejected(linkedHoisan(missing), missing);
    }

    @Test
    void aLinkedHoisanTableIsNotFollowedWhenTheLinkedFileExists() throws Exception {
        File existing = dir.resolve("secret-existing.mdb").toFile();
        Database.create(existing).close();
        assertTrue(existing.setLastModified(existing.lastModified() - 60_000));
        long modified = existing.lastModified();
        long size = existing.length();

        assertLinkedTableRejected(linkedHoisan(existing), existing);

        assertEquals(modified, existing.lastModified(), "linked file not opened read-write");
        assertEquals(size, existing.length(), "linked file not opened read-write");
    }

    @Test
    void hoisanToolRefusesToResolveLinkedTables() throws Exception {
        File existing = dir.resolve("secret-existing.mdb").toFile();
        Database.create(existing).close();

        IOException ex = assertThrows(IOException.class, () -> new HoisanTool().read(linkedHoisan(existing)));

        assertEquals("Linked tables are not supported", ex.getMessage());
    }

    /**
     * An Access database whose HOISAN {@code Criterios} table is a link to {@code linkee} (Jackcess
     * {@code createLinkedTable}, the catalog row of type linked table that Access writes).
     */
    private File linkedHoisan(File linkee) throws IOException {
        File mdb = dir.resolve("linked.mdb").toFile();
        Files.deleteIfExists(mdb.toPath());
        try (Database db = Database.create(mdb)) {
            db.createLinkedTable("Criterios", linkee.getAbsolutePath(), "Criterios");
        }
        return mdb;
    }

    private void assertLinkedTableRejected(File mdb, File linkee) throws IOException {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>());
        Set<String> tempsBefore = Lince1Fixtures.tempFileNames("lince-import-");

        ImportException ex;
        try (InputStream in = Files.newInputStream(mdb.toPath())) {
            ex = assertThrows(ImportException.class, () -> importer.importInto(project, in, ImportScope.FULL, Map.of()));
        }

        assertEquals(TransferException.Reason.UNREADABLE, ex.getReason());
        assertEquals("Not a HOISAN database", ex.getMessage());
        assertFalse(ex.getMessage().contains(linkee.getName()), "linked path not echoed");
        assertFalse(ex.getMessage().contains(dir.toString()), "temp dir not echoed");
        assertFalse(ex.getMessage().contains(System.getProperty("java.io.tmpdir")), "temp dir not echoed");
        assertNotNull(project.getObservationTool());
        assertTrue(project.getObservationTool().isEmpty(), "instrument untouched");
        assertTrue(project.getRegister() == null || project.getRegister().isEmpty(), "register untouched");
        assertEquals(Set.of(), Lince1Fixtures.newTempFiles(tempsBefore, "lince-import-"), "spool file deleted");
    }
}
