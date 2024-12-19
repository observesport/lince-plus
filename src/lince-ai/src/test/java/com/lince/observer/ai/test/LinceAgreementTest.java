package com.lince.observer.ai.test;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.agreement.AgreementResult;
import com.lince.observer.ai.agreement.LinceDkproAdapter;
import com.lince.observer.data.component.LinceFileImporter;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * com.lince.observer.ai.test
 * Class LinceAgreementTest
 * 12/04/2019
 * <p>
 * <p>
 * Loads lince demo file, converts it to dkpro resource and calculates agreement
 *
 * @author berto (alberto.soto@gmail.com)
 */
class LinceAgreementTest {

    private static final String NAME = "Lince Agreement test";
    private static final Logger log = LoggerFactory.getLogger(LinceAgreementTest.class);
    private static LinceDkproAdapter adapter = null;

    @BeforeAll
    static void init() {
        setAdapter();
    }

    /**
     * Loads file automatically
     *
     * @param uuids uuids
     */
    private static void setAdapter(UUID... uuids) {
        LinceFileImporter fileImporter = new LinceFileImporter();
        File file = new File(LinceAgreementTest.class
                .getResource("/multipleObserverExample.xml").getFile());
        ILinceProject linceProject = fileImporter.importLinceProject(file);
        if (linceProject != null) {
            adapter = new LinceDkproAdapter(linceProject.getRegister(), linceProject.getObservationTool(), uuids);
        } else {
            throw new RuntimeException("Failed to import Lince project from file: " + file.getAbsolutePath());
        }
    }

    @Test
    void getStudiesFromProject() {
        List<CodingAnnotationStudy> studies = adapter.getStudies();
        for (CodingAnnotationStudy std : studies) {
            log.info(String.format("Studio -obs %s -tamaño %s -items %s %n %s",
                    std.getRaterCount(), std.getUnitCount(), std.getItemCount(), std.getItems().toString()));
        }
        log.info("check value");
        Assertions.assertEquals("UNI1", studies.get(0).getItem(0).getUnit(0).getCategory());
    }

    private void printResults(List<AgreementResult> l) {
        for (AgreementResult std : l) {
            log.info(String.format("Criterio %s - analisis %s: valor %s %n Desacuerdo observado %s - esperado %s"
                    , std.getCriteria().getName(), std.getType().toString(), std.getAgreement()
                    , std.getObservedDisagreement(), std.getExpectedDisagreement()));
        }
    }

    @Test
    void getKappas() {
        List<AgreementResult> l = adapter.calculateKappaIndex();
        printResults(l);
        setAdapter(UUID.fromString("d336e1e4-549d-4980-899b-bda0930c0952")
                , UUID.fromString("a62b15b3-4cf0-4dd9-8b1b-748fd5a778b3"));
        List<AgreementResult> l2 = adapter.calculateKappaIndex();
        Assertions.assertEquals(l.get(0).getAgreement(), l2.get(0).getAgreement());
        setAdapter();
    }

    @Test
    void getPercentageAgreement() {
        List<AgreementResult> l = adapter.calculatePercentageAgreement();
        printResults(l);
        Assertions.assertFalse(l.isEmpty(), "Percentage agreement results should not be empty");
    }

    @Test
    void getKrippendorfAgreement() {
        List<AgreementResult> l1 = adapter.calculateKrippendorfAlphaAgreement();
        printResults(l1);
        Assertions.assertFalse(l1.isEmpty(), "Krippendorf's Alpha results should not be empty");
    }
}
