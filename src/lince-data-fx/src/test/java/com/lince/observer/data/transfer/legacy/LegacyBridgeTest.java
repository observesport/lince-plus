package com.lince.observer.data.transfer.legacy;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyBridgeTest {

    private final LegacyBridge bridge = new LegacyBridge();
    private LinceFileProjectWrapper project;
    private Criteria pase;
    private Criteria zona;
    private Criteria vacio;
    private LinceRegisterWrapper first;
    private LinceRegisterWrapper second;

    @BeforeEach
    void setUp() {
        pase = criteria(1, "Pase", "P", "PC", "PL");
        zona = criteria(2, "Zona", "Z", "Z1", "Z2");
        vacio = criteria(3, "Vacio", "V");
        project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(List.of(pase, zona, vacio)));
        first = new LinceRegisterWrapper();
        first.setRegisterData(new ArrayList<>(List.of(
                row(1.0, cat(pase, 0), cat(zona, 1)),
                row(2.5, cat(pase, 1)))));
        second = new LinceRegisterWrapper();
        second.setRegisterData(new ArrayList<>(List.of(row(4.0, cat(zona, 0)))));
        project.setRegister(new ArrayList<>(List.of(first, second)));
    }

    @Test
    void exportBuildsTheLegacyInstrumentInProjectOrder() {
        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, null, List.of()));

        assertSame(InstrumentoObservacional.getInstance(), legacy.instrument());
        Criterio[] criterios = legacy.instrument().getCriterios();
        assertEquals(List.of("Pase", "Zona", "Vacio"), Stream.of(criterios).map(Criterio::getNombre).toList());
        assertEquals(List.of("PC", "PL"), Stream.of(criterios[0].getCategoriasHijo()).map(Categoria::getCodigo).toList());
        assertEquals("Pase description", criterios[0].getDescripcion());
        assertTrue(criterios[0].isPersistente());
    }

    @Test
    void selectedModernCriteriaMapToTheirLegacyCriterioInSelectionOrder() {
        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, null, List.of("2", "1")));

        Criterio[] criterios = legacy.instrument().getCriterios();
        assertEquals(List.of(criterios[1], criterios[0]), legacy.selectedCriterios());
        assertSame(criterios[1], legacy.selectedCriterios().get(0));
        assertSame(criterios[0], legacy.mapping().toLegacy(pase));
        assertSame(zona, legacy.mapping().toModern(criterios[1]));
    }

    @Test
    void registerRowsComeFromTheSelectedResearch() {
        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, first.getId(), List.of()));

        Registro registro = legacy.register();
        assertSame(Registro.getInstance(), registro);
        assertEquals(2, registro.datosVariables.size());
        FilaRegistro fila = registro.datosVariables.get(0);
        assertEquals(1000, fila.getMilis());
        Criterio[] criterios = legacy.instrument().getCriterios();
        assertEquals("PC", fila.getCategoria(criterios[0]).getCodigo());
        assertEquals("Z2", fila.getCategoria(criterios[1]).getCodigo());
        assertSame(criterios[1].getCategoriasHijo()[1], fila.getCategoria(criterios[1]));
        assertEquals(List.of("PL", ""), List.of(registro.getColumna(criterios[0])[1], registro.getColumna(criterios[1])[1]));

        LegacyProject other = bridge.toLegacy(ExportSelection.of(project, second.getId(), List.of()));
        assertEquals(1, other.register().datosVariables.size());
        assertEquals(4000, other.register().datosVariables.get(0).getMilis());
    }

    @Test
    void categoriesWhoseCriteriaIsMissingBecomeWarnings() {
        Category orphan = new Category(99, "Orphan", 42);
        orphan.setCode("ORF");
        first.getRegisterData().add(row(3.0, orphan));

        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, first.getId(), List.of()));

        assertEquals(3, legacy.register().datosVariables.size());
        assertEquals(1, legacy.warnings().size());
        assertTrue(legacy.warnings().get(0).contains("ORF"), legacy.warnings().get(0));
    }

    @Test
    void mappingRejectsCriteriaThatAreNotInTheInstrument() {
        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, null, List.of()));
        Criteria foreign = criteria(7, "Foreign", "F", "F1");

        ExportException ex = assertThrows(ExportException.class, () -> legacy.mapping().toLegacy(List.of(pase, foreign)));

        assertEquals(ExportException.Reason.UNKNOWN_CRITERIA, ex.getReason());
        assertEquals(List.of("7"), ex.getOffendingIds());
    }

    @Test
    void importConvertsTheLegacyInstrumentToModernCriteria() {
        InstrumentoObservacional instrument = legacyInstrument();

        LegacyImport imported = bridge.fromLegacyInstrument(instrument);

        List<Criteria> criteria = imported.criteria();
        assertEquals(2, criteria.size(), "criteria without categories are skipped");
        Criteria golpe = criteria.get(0);
        assertEquals(1, golpe.getId());
        assertEquals("Golpe", golpe.getName());
        assertEquals("Golpe description", golpe.getDescription());
        assertNotNull(golpe.getCode());
        assertFalse(golpe.getCode().isBlank());
        assertEquals(List.of("GD", "GR"), golpe.getInnerCategories().stream().map(Category::getCode).toList());
        assertEquals(List.of(11, 12), golpe.getInnerCategories().stream().map(Category::getId).toList());
        assertEquals(1, golpe.getInnerCategories().get(0).getParent());
        assertEquals(2, criteria.get(1).getId());
        assertTrue(criteria.stream().map(Criteria::getCode).distinct().count() == 2, "codes are unique");

        Criterio legacyGolpe = instrument.getCriterios()[0];
        assertSame(golpe, imported.mapping().toModern(legacyGolpe));
        assertSame(legacyGolpe, imported.mapping().toLegacy(golpe));
    }

    @Test
    void importConvertsLegacyRowsResolvingCategoriesByCode() {
        InstrumentoObservacional instrument = legacyInstrument();
        List<Criteria> tool = bridge.fromLegacyInstrument(instrument).criteria();
        Criterio golpe = instrument.getCriterios()[0];
        Map<Criterio, Categoria> known = new HashMap<>();
        known.put(golpe, golpe.getCategoriasHijo()[1]);
        Map<Criterio, Categoria> unknown = new HashMap<>();
        Categoria lost = new Categoria("Lost");
        lost.setCodigo("XX");
        unknown.put(golpe, lost);
        List<FilaRegistro> rows = List.of(
                new FilaRegistro(2000, known, new HashMap<>(), 25.0),
                new FilaRegistro(4000, unknown, new HashMap<>(), 25.0));
        List<String> warnings = new ArrayList<>();

        List<RegisterItem> items = bridge.fromLegacyRows(rows, tool, warnings);

        assertEquals(2, items.size());
        RegisterItem item = items.get(0);
        assertEquals(1, item.getId());
        assertEquals(2.0, item.getVideoTime());
        assertEquals(50, item.getFrames());
        assertEquals("f.50", item.getName());
        assertEquals(1, item.getRegister().size());
        assertSame(tool.get(0).getInnerCategories().get(1), item.getRegister().get(0));
        assertTrue(items.get(1).getRegister().isEmpty());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("XX"), warnings.get(0));
    }

    @Test
    void modernCriteriaSurviveARoundTripThroughTheLegacyModel() {
        LegacyProject legacy = bridge.toLegacy(ExportSelection.of(project, null, List.of()));

        List<Criteria> back = bridge.fromLegacyInstrument(legacy.instrument()).criteria();

        assertEquals(List.of("Pase", "Zona"), back.stream().map(Criteria::getName).toList());
        assertEquals(List.of("Z1", "Z2"), back.get(1).getInnerCategories().stream().map(Category::getCode).toList());
    }

    @Test
    void withLegacyModelIsReentrant() {
        String result = bridge.withLegacyModel(() -> bridge.withLegacyModel(() ->
                bridge.export(ExportSelection.of(project, null, List.of("1")), l -> l.selectedCriterios().get(0).getNombre())));

        assertEquals("Pase", result);
    }

    @Test
    void bridgeSourcesHaveNoUiImports() throws IOException {
        Path dir = Path.of("src/main/java/com/lince/observer/data/transfer/legacy");
        try (Stream<Path> files = Files.list(dir)) {
            List<Path> sources = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(sources.isEmpty());
            for (Path source : sources) {
                String code = Files.readString(source);
                for (String forbidden : List.of("import javafx.", "import javax.swing", "import java.awt",
                        "Platform.runLater", "JOptionPane")) {
                    assertFalse(code.contains(forbidden), source + " uses " + forbidden);
                }
            }
        }
    }

    private InstrumentoObservacional legacyInstrument() {
        InstrumentoObservacional.loadNewInstance();
        InstrumentoObservacional instrument = InstrumentoObservacional.getInstance();
        Criterio golpe = instrument.addCriterio("Golpe");
        golpe.setDescripcion("Golpe description");
        instrument.addCategoria(golpe, "Derecha").setCodigo("GD");
        instrument.addCategoria(golpe, "Reves").setCodigo("GR");
        instrument.addCriterio("Sin categorias");
        Criterio lado = instrument.addCriterio("Golpe!");
        instrument.addCategoria(lado, "Izquierda").setCodigo("LI");
        return instrument;
    }

    private static Criteria criteria(int id, String name, String code, String... categoryCodes) {
        Criteria criteria = new Criteria(id, name);
        criteria.setCode(code);
        criteria.setDescription(name + " description");
        criteria.setPersistence(true);
        LinkedList<Category> categories = new LinkedList<>();
        int index = 1;
        for (String categoryCode : categoryCodes) {
            Category category = new Category(id * 10 + index, name + " " + index, id);
            category.setCode(categoryCode);
            categories.add(category);
            index++;
        }
        criteria.setInnerCategories(categories);
        return criteria;
    }

    private static Category cat(Criteria criteria, int index) {
        return criteria.getInnerCategories().get(index);
    }

    private static RegisterItem row(double seconds, Category... categories) {
        return new RegisterItem(seconds, categories);
    }
}
