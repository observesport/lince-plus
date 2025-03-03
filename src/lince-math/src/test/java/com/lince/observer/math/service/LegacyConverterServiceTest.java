package com.lince.observer.math.service;


import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceFileImporter;
import com.lince.observer.data.component.PackageAwareXMLSerializer;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.math.LinceServiceTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LinceServiceTestConfig.class)
class LegacyConverterServiceTest {

    private final Logger logger = LoggerFactory.getLogger(LegacyConverterServiceTest.class);

    @Autowired
    private LegacyConverterService legacyConverterService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private DataHubService dataHubService;

    @BeforeEach
    void setUp() {
    }

    private ILinceProject getDemoProject() {
        LinceFileImporter linceFileImporter = new LinceFileImporter();
        return linceFileImporter.importLinceProject(
                new File(Objects.requireNonNull(getClass().getClassLoader().getResource("linceProjectTemplate.xml")).getFile()));

    }

    private Object loadLegacyObject(String testFileName) throws IOException {
        logger.info("Loading file: {}", testFileName);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(testFileName);
             PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(inputStream)) {
            logger.info(PackageAwareXMLSerializer.getPackageMappingsText());
            return decoder.readObject();
        } catch (IOException e) {
            fail("IO Exception: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        return null;
    }

    @Test
    void testInitializeLegacyInstances() {
        // Test with forceClean = false
        legacyConverterService.initializeLegacyInstances(false);
        assertNotNull(Registro.getInstance());
        assertNotNull(InstrumentoObservacional.getInstance());

        // Test with forceClean = true
        legacyConverterService.initializeLegacyInstances(true);
        assertNotNull(Registro.getInstance());
        assertNotNull(InstrumentoObservacional.getInstance());
    }

    //
    @Test
    void testSaveRegisterForNewUser_WithEmptyList() {
        // Setup
        String observerName = "Test Observer";
        List<RegisterItem> registerItems = new ArrayList<>();

        // Get initial size of data register
        int initialSize = dataHubService.getDataRegister().size();

        // Execute
        legacyConverterService.saveRegisterForNewUser(observerName, registerItems);

        // Verify - no new data should be added
        assertEquals(initialSize, dataHubService.getDataRegister().size());
    }

    @Test
    void testSaveRegisterForNewUser_WithNullList() {
        // Setup
        String observerName = "Test Observer";

        // Get initial size of data register
        int initialSize = dataHubService.getDataRegister().size();

        // Execute
        legacyConverterService.saveRegisterForNewUser(observerName, null);

        // Verify - no new data should be added
        assertEquals(initialSize, dataHubService.getDataRegister().size());
    }


    @Test
    void testGetCategoryFromLegacy() {
        // Setup
        Criteria criteria = new Criteria();
        criteria.setId(1);

        Categoria legacyCategory = new Categoria();
        legacyCategory.setNombre("Test Category");
        legacyCategory.setCodigo("TC");
        legacyCategory.setDescripcion("Test Description");

        // Use reflection to access private method
        try {
            Method method = LegacyConverterService.class.getDeclaredMethod(
                    "getCategoryFromLegacy", Criteria.class, Categoria.class, Integer.class);
            method.setAccessible(true);

            // Execute
            Category result = (Category) method.invoke(legacyConverterService, criteria, legacyCategory, 2);

            // Verify
            assertNotNull(result);
            assertEquals("Test Category", result.getName());
            assertEquals("TC", result.getCode());
            assertEquals("Test Description", result.getDescription());
            assertEquals(1, result.getParent());
            assertEquals(2, result.getId());
        } catch (Exception e) {
            fail("Exception during test: " + e.getMessage());
        }
    }

    @Test
    void testMigrateDataToLegacy() {
        // Setup
        ILinceProject linceProject = getDemoProject();
        Optional<LinceRegisterWrapper> firstRegister = linceProject.getRegister().stream().findFirst();
        assertTrue(firstRegister.isPresent(), "Register should not be empty");
        dataHubService.setLinceProject(linceProject);

        // Execute
        LegacyMigrationResult result = legacyConverterService.migrateDataToLegacy(firstRegister.get().getId());

        // Verify register
        assertNotNull(result.register());

        // Verify instrument
        assertNotNull(result.instrument(), "Instrument should not be null");
        Criterio[] criterios = result.instrument().getCriterios();
        assertNotNull(criterios, "Criterios should not be null");
        assertTrue(criterios.length > 0, "Should have at least one criterio");

        // Verify criteria match between project and legacy instrument
        List<Criteria> projectCriteria = linceProject.getObservationTool().stream().toList();
        assertEquals(criterios.length, projectCriteria.size(),
                "Number of criteria should match between project and legacy instrument");

        // Compare criteria names and inner categories
        for (Criterio legacyCriterio : criterios) {
            Optional<Criteria> matchingCriteria = projectCriteria.stream()
                    .filter(c -> c.getName().equals(legacyCriterio.getNombre()))
                    .findFirst();
            assertTrue(matchingCriteria.isPresent(),
                    "Should find matching criteria for: " + legacyCriterio.getNombre());

            Categoria[] legacyCategories = legacyCriterio.getCategoriasHijo();
            List<Category> projectCategories = matchingCriteria.get().getInnerCategories();
            assertEquals(legacyCategories.length, projectCategories.size(),
                    "Number of categories should match for criteria: " + legacyCriterio.getNombre());
        }
    }


    @Test
    void testMigrateDataFromLegacy() throws IOException {
        // Setup
        ILinceProject linceProject = getDemoProject();
        Optional<LinceRegisterWrapper> firstRegister = linceProject.getRegister().stream().findFirst();
        assertTrue(firstRegister.isPresent(), "Register should not be empty");
        dataHubService.setLinceProject(linceProject);
        Registro register = null;
        InstrumentoObservacional instrument = null;

        // execute
        try (InputStream instrumentStream = getClass().getClassLoader().getResourceAsStream("template.ilince")) {
            instrument = legacyConverterService.migrateDataFromLegacyInstrument(instrumentStream);
            assertNotNull(instrument, "Instrument should not be null");
            Criterio[] criterios = instrument.getCriterios();
            assertNotNull(criterios, "Criterios should not be null");
            assertTrue(criterios.length > 0, "Should have at least one criterio");
        }
        try (InputStream registerStream = getClass().getClassLoader().getResourceAsStream("template.rlince")) {
            register = legacyConverterService.migrateDataFromLegacyRegister(registerStream);
            assertNotNull(register, "Register should not be null");
            assertNotNull(register.datosVariables, "DatosVariables should not be null");
            assertTrue(register.datosVariables.size() > 0, "Should have at least one registro");
        }

        // verify
        assertNotNull(instrument, "Instrument should be loaded for comparison");
        assertNotNull(register, "Register should be loaded for comparison");

        List<Criteria> projectCriteria = linceProject.getObservationTool().stream().toList();
        Criterio[] legacyCriteria = instrument.getCriterios();
        assertEquals(legacyCriteria.length, projectCriteria.size(),
                "Number of criteria should match between project and legacy instrument");

        // Compare criteria names and inner categories
        for (Criterio legacyCriterio : legacyCriteria) {
            Optional<Criteria> matchingCriteria = projectCriteria.stream()
                    .filter(c -> c.getName().equals(legacyCriterio.getNombre()))
                    .findFirst();
            assertTrue(matchingCriteria.isPresent(),
                    "Should find matching criteria for: " + legacyCriterio.getNombre());
            Categoria[] legacyCategories = legacyCriterio.getCategoriasHijo();
            List<Category> projectCategories = matchingCriteria.get().getInnerCategories();
            assertEquals(legacyCategories.length, projectCategories.size(),
                    "Number of categories should match for criteria: " + legacyCriterio.getNombre());
        }

        // Compare register data size
        Optional<LinceRegisterWrapper> registerWrapper = linceProject.getRegister().stream().findFirst();
        assertTrue(registerWrapper.isPresent(), "Register wrapper should be present");
        assertEquals(register.datosVariables.size(), registerWrapper.get().getRegisterData().size(),
                "Number of register items should match");
    }
}
