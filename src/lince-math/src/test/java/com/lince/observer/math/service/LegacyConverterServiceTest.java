package com.lince.observer.math.service;


import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceFileImporter;
import com.lince.observer.data.service.AnalysisService;
import com.lince.observer.data.service.CategoryService;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.math.LinceServiceTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = LinceServiceTestConfig.class)
class LegacyConverterServiceTest {

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
}
