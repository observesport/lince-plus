package com.lince.observer.data.transfer.importers;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ImportException;
import com.lince.observer.data.transfer.ImportOutcome;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Lince1ToolImporterTest {

    private final Lince1ToolImporter importer = new Lince1ToolImporter(new LegacyBridge());

    @Test
    void descriptorDetectsIlinceFiles() {
        assertEquals("lince1-tool", importer.descriptor().id());
        assertEquals(List.of("ilince"), importer.descriptor().extensions());
        assertTrue(importer.supports("tool.ILINCE"));
        assertFalse(importer.supports("register.rlince"));
    }

    @Test
    void fullScopeReplacesTheInstrumentWithTheConvertedLegacyCriteria() throws Exception {
        Lince1Fixtures.loadLegacy();
        List<Criterio> expected = Lince1Fixtures.convertibleCriterios();
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(List.of(new Criteria(9, "Old"))));
        long tempsBefore = Lince1Fixtures.tempFiles("lince-import-");

        ImportOutcome outcome;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.TOOL)) {
            outcome = importer.importInto(project, in, ImportScope.FULL, Map.of());
        }

        List<Criteria> tool = project.getObservationTool();
        assertFalse(expected.isEmpty());
        assertEquals(expected.stream().map(Criterio::getNombre).toList(), tool.stream().map(Criteria::getName).toList());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(Stream.of(expected.get(i).getCategoriasHijo()).map(Categoria::getCodigo).toList(),
                    tool.get(i).getInnerCategories().stream().map(Category::getCode).toList());
            assertEquals(i + 1, tool.get(i).getId());
        }
        assertEquals(tool.size(), outcome.criteriaCount());
        assertEquals(0, outcome.observationCount());
        assertEquals(tempsBefore, Lince1Fixtures.tempFiles("lince-import-"), "temp file deleted");
    }

    @Test
    void replacingTheInstrumentOfAProjectWithObservationsWarns() throws Exception {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        LinceRegisterWrapper register = new LinceRegisterWrapper();
        register.setRegisterData(new ArrayList<>(List.of(new RegisterItem(1.0))));
        project.setRegister(new ArrayList<>(List.of(register)));

        ImportOutcome outcome;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.TOOL)) {
            outcome = importer.importInto(project, in, ImportScope.FULL, Map.of());
        }

        assertTrue(outcome.warnings().stream().anyMatch(w -> w.contains("existing observations")), outcome.warnings().toString());
        assertSame(register, project.getRegister().get(0));
    }

    @Test
    void observationsScopeLeavesTheProjectUntouched() throws Exception {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        List<Criteria> before = new ArrayList<>(List.of(new Criteria(9, "Old")));
        project.setObservationTool(before);

        ImportOutcome outcome;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.TOOL)) {
            outcome = importer.importInto(project, in, ImportScope.OBSERVATIONS, Map.of());
        }

        assertSame(before, project.getObservationTool());
        assertEquals(1, project.getObservationTool().size());
        assertEquals(1, outcome.warnings().size());
    }

    @Test
    void contentThatIsNotAnInstrumentIsUnreadable() throws Exception {
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();

        ImportException garbage = assertThrows(ImportException.class, () -> importer.importInto(project,
                new ByteArrayInputStream("not xml".getBytes(StandardCharsets.UTF_8)), ImportScope.FULL, Map.of()));
        ImportException register;
        try (InputStream in = Lince1Fixtures.stream(Lince1Fixtures.REGISTER)) {
            register = assertThrows(ImportException.class, () -> importer.importInto(project, in, ImportScope.FULL, Map.of()));
        }

        assertEquals(TransferException.Reason.UNREADABLE, garbage.getReason());
        assertEquals(TransferException.Reason.UNREADABLE, register.getReason());
        assertTrue(project.getObservationTool().isEmpty());
    }
}
