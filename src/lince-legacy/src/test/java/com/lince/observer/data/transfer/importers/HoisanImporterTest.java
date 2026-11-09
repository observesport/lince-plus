package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * No HOISAN fixture is checked in: the database is generated from the Lince 1 fixtures with the legacy HOISAN
 * writer ({@code HoisanTool.exportFile}, pure Java through Jackcess, no ODBC driver needed).
 */
class HoisanImporterTest {

    private final HoisanImporter importer = new HoisanImporter(new LegacyBridge());

    @TempDir
    Path dir;

    @Test
    void descriptorDetectsMdbFiles() {
        assertEquals("hoisan", importer.descriptor().id());
        assertTrue(importer.supports("study.MDB"));
    }

    @Test
    void criteriaAndObservationsOfAHoisanDatabaseAreImported() throws Exception {
        Registro registro = Lince1Fixtures.loadLegacy();
        List<String> criteriaNames = Stream.of(InstrumentoObservacional.getInstance().getCriterios())
                .filter(c -> c.getCategoriasHijo().length > 0).map(Criterio::getNombre).toList();
        int rows = registro.datosVariables.size();
        File mdb = dir.resolve("study.mdb").toFile();
        assertNull(new HoisanTool().exportFile(mdb));
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        Set<String> tempsBefore = Lince1Fixtures.tempFileNames("lince-import-");

        ImportOutcome outcome;
        try (InputStream in = Files.newInputStream(mdb.toPath())) {
            outcome = importer.importInto(project, in, ImportScope.FULL, Map.of());
        }

        assertEquals(criteriaNames, project.getObservationTool().stream().map(Criteria::getName).toList());
        assertEquals(1, project.getRegister().size(), "one observer per HOISAN video");
        List<RegisterItem> items = project.getRegister().get(0).getRegisterData();
        assertEquals(rows, items.size());
        assertEquals(rows, outcome.observationCount());
        assertTrue(items.stream().anyMatch(i -> !i.getRegister().isEmpty()), "categories resolved");
        List<FilaRegistro> legacyRows = registro.datosVariables;
        assertEquals(legacyRows.get(0).getRegisterFrameValue(), items.get(0).getFrames());
        assertEquals(Set.of(), Lince1Fixtures.newTempFiles(tempsBefore, "lince-import-"), "temp file deleted");
        assertEquals(List.of("study.mdb"), Stream.of(dir.toFile().list()).toList(), "no Lince 1 files written");
    }

    @Test
    void aFileThatIsNotAnAccessDatabaseIsUnreadable() {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>());

        ImportException ex = assertThrows(ImportException.class, () -> importer.importInto(project,
                new ByteArrayInputStream("nope".getBytes(StandardCharsets.UTF_8)), ImportScope.FULL, Map.of()));

        assertEquals(TransferException.Reason.UNREADABLE, ex.getReason());
        assertFalse(project.getRegister() != null && !project.getRegister().isEmpty());
    }
}
