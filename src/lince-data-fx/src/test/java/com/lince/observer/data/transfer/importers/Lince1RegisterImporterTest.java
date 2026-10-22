package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.user.UserProfile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Lince1RegisterImporterTest {

    private final LegacyBridge bridge = new LegacyBridge();
    private final Lince1RegisterImporter importer = new Lince1RegisterImporter(bridge);
    private LinceFileProjectWrapper project;
    private ResearchProfile research;

    @BeforeEach
    void setUp() throws Exception {
        project = new LinceFileProjectWrapper();
        research = new ResearchProfile();
        project.setProfiles(new ArrayList<>(List.of(research)));
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.TOOL)) {
            new Lince1ToolImporter(bridge).importInto(project, in, ImportScope.FULL, Map.of());
        }
    }

    @Test
    void descriptorDetectsRlinceFiles() {
        assertEquals("lince1-register", importer.descriptor().id());
        assertTrue(importer.supports("obs.rlince"));
        assertFalse(importer.supports("obs.ilince"));
    }

    @Test
    void rowsBecomeANewObserverResolvedAgainstTheProjectInstrument() throws Exception {
        List<FilaRegistro> legacyRows = Lince1Fixtures.loadLegacy().datosVariables;
        List<Criteria> tool = project.getObservationTool();
        long tempsBefore = Lince1Fixtures.tempFiles("lince-import-");

        ImportOutcome outcome;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.REGISTER)) {
            outcome = importer.importInto(project, in, ImportScope.FULL, Map.of());
        }

        assertSame(tool, project.getObservationTool(), "the instrument is not replaced");
        assertEquals(1, project.getRegister().size());
        LinceRegisterWrapper register = project.getRegister().get(0);
        UserProfile observer = register.getUserProfile();
        assertEquals(Lince1RegisterImporter.OBSERVER_NAME, observer.getUserName());
        assertEquals(register.getId(), observer.getRegisterCode());
        assertTrue(research.getUserProfiles().contains(observer), "observer added to the research profile");

        List<RegisterItem> rows = register.getRegisterData();
        assertFalse(legacyRows.isEmpty());
        assertEquals(legacyRows.size(), rows.size());
        assertEquals(legacyRows.size(), outcome.observationCount());
        assertEquals(1, outcome.registerCount());
        assertEquals(1, outcome.profileCount());
        List<String> knownCodes = tool.stream().flatMap(c -> c.getInnerCategories().stream()).map(Category::getCode).toList();
        for (int i = 0; i < rows.size(); i++) {
            FilaRegistro legacy = legacyRows.get(i);
            List<String> expectedCodes = legacy.getRegistro().values().stream().filter(Objects::nonNull)
                    .map(Categoria::getCodigo).filter(knownCodes::contains).sorted().toList();
            List<String> codes = rows.get(i).getRegister().stream().map(Category::getCode).sorted().toList();
            assertEquals(expectedCodes, codes, "row " + i);
            assertEquals(legacy.getMilis(), rows.get(i).getVideoTimeMillis(), "row " + i);
            for (Category category : rows.get(i).getRegister()) {
                assertTrue(tool.stream().anyMatch(c -> c.getInnerCategories().contains(category)));
            }
        }
        assertTrue(outcome.warnings().stream().anyMatch(w -> w.contains("BAJA SUP")),
                "sub-categories are not part of the converted instrument and are reported: " + outcome.warnings());
        assertEquals(tempsBefore, Lince1Fixtures.tempFiles("lince-import-"), "temp file deleted");
    }

    @Test
    void aProjectWithoutInstrumentIsRejected() throws Exception {
        ILinceProject empty = new LinceFileProjectWrapper();

        ImportException ex;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.REGISTER)) {
            ex = assertThrows(ImportException.class, () -> importer.importInto(empty, in, ImportScope.FULL, Map.of()));
        }

        assertEquals(TransferException.Reason.INSTRUMENT_REQUIRED, ex.getReason());
        assertTrue(empty.getRegister().isEmpty());
    }

    @Test
    void criteriaMissingFromTheInstrumentAreWarningsNotFailures() throws Exception {
        List<Criteria> tool = new ArrayList<>(project.getObservationTool());
        tool.remove(0);
        project.setObservationTool(tool);

        ImportOutcome outcome;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.REGISTER)) {
            outcome = importer.importInto(project, in, ImportScope.FULL, Map.of());
        }

        assertFalse(outcome.warnings().isEmpty());
        assertEquals(1, project.getRegister().size());
    }

    @Test
    void contentThatIsNotARegisterIsUnreadableAndLeavesTheLegacyRegister() {
        Registro before = Registro.getInstance();

        ImportException ex = assertThrows(ImportException.class, () -> importer.importInto(project,
                new ByteArrayInputStream("<java/>".getBytes(StandardCharsets.UTF_8)), ImportScope.FULL, Map.of()));

        assertEquals(TransferException.Reason.UNREADABLE, ex.getReason());
        assertTrue(project.getRegister().isEmpty());
        assertFalse(before == null);
    }

    @Test
    void unreadableContentDoesNotLeakTheSpoolPath() {
        for (String content : List.of("not xml", "<java/>", "")) {
            ImportException ex = assertThrows(ImportException.class, () -> importer.importInto(project,
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ImportScope.FULL, Map.of()));

            assertEquals(TransferException.Reason.UNREADABLE, ex.getReason());
            assertTrue(ex.getMessage().startsWith("Cannot read Lince 1 register file"), ex.getMessage());
            assertNoSpoolPath(ex);
        }
    }

    /**
     * A 400 body must not reveal the server spool path (it names the OS user on Windows).
     */
    static void assertNoSpoolPath(ImportException ex) {
        String message = String.valueOf(ex.getMessage());
        String tmp = System.getProperty("java.io.tmpdir").replaceAll("[/\\\\]+$", "");
        assertFalse(message.contains("lince-import-"), message);
        assertFalse(message.contains(tmp), message);
        assertFalse(message.contains(new java.io.File(tmp).getAbsolutePath()), message);
    }
}

