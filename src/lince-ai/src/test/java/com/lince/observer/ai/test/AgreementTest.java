package com.lince.observer.ai.test;

import com.lince.observer.ai.agreement.LinceContingencyMatrixPrinter;
import org.dkpro.statistics.agreement.coding.*;
import org.dkpro.statistics.agreement.distance.NominalDistanceFunction;
import org.dkpro.statistics.agreement.visualization.ContingencyMatrixPrinter;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

/**
 * com.lince.observer.ai.test
 * Class AgreementTest
 * 12/04/2019
 * <p>
 * Other tests types:
 * v1 - Implemented here
 * v2 - Documentation example from flatfiles/db
 * CodingAnnotationStudy study = new CodingAnnotationStudy(3);
 * BufferedReader reader = new BufferedReader( new FileReader("flatfile.tsv"));
 * String line;
 * while ((line = reader.readLine()) != null) {
 * study.addItemAsArray(line.split("\t"));
 * }
 * reader.close();
 * <p>
 * v3 - Use UIMA annotations (or a similar data format from your framework).
 * UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(2, jcas.getDocumentText().length());
 * for (Annotation a : JCasUtil.select(jcas, Annotation.class)) {
 * study.addUnit(a.getBegin(), a.getEnd() - a.getBegin(), a.getRaterIdx(), true);
 * }
 * <p>
 * v4 - Implement your interfaces
 *
 * @author berto (alberto.soto@gmail.com)
 */
class AgreementTest {
    private static final int NUM_OBSERVERS = 2;
    private static final int NUM_ITEMS = 5;
    private static CodingAnnotationStudy study = null;
    private static final String STAT_MESSAGE = "Test de tipo %s - resultado %s";

    @AfterAll
    static void end() {
        System.out.println("- Finishing agreement tests - ");
    }

    /**
     * Defines a basic study to try
     */
    @BeforeAll
    static void init() {
        System.out.println("- Starting agreement test -");
        study = new CodingAnnotationStudy(NUM_OBSERVERS);
        //It must be used by category
        //parameters mas have same length than observers
        study.addItem("A", "A");
        study.addItem("B", "B");
        study.addItem("C", "A");
        study.addItem("D", "D");
        study.addItem("A", "A");
        /*
        study.addItem(null, "A");
        study.addItem("A", "A");*/
        //we can add them simultaneously
        //study.addMultipleItems(2,"A", "B",null, "A");
    }

    @Test
    void createTestStudy() {
        Assertions.assertEquals(NUM_ITEMS, study.getItemCount());
    }

    @Test
    void calculatePercentageAgreement() {
        PercentageAgreement pa = new PercentageAgreement(study);
        System.out.println(String.format(STAT_MESSAGE, "Porcentaje", pa.calculateAgreement()));

    }

    @Test
    void calculateKappaIndex() {
        FleissKappaAgreement kappa = new FleissKappaAgreement(study);
        System.out.println(kappa.calculateAgreement());
        System.out.println(String.format(STAT_MESSAGE, "Kappa", kappa.calculateAgreement()));
    }

    @Test
    void calculateKrippendorfAlphaIndex() {
        KrippendorffAlphaAgreement alpha = new KrippendorffAlphaAgreement(study, new NominalDistanceFunction());
        System.out.println(String.format(STAT_MESSAGE, "Krippendorf", alpha.calculateAgreement()));
        System.out.println(alpha.calculateObservedDisagreement());
        System.out.println(alpha.calculateExpectedDisagreement());
    }

    @Test
    void printMatrix() {
        PrintStream ps = new PrintStream(System.out);
        ContingencyMatrixPrinter aux = new ContingencyMatrixPrinter();
        aux.print(ps, study);
    }

    @Test
    void calculateCohenKappa(){
        CohenKappaAgreement alpha = new CohenKappaAgreement(study);
        System.out.println(String.format(STAT_MESSAGE, "Cohen kappa", alpha.calculateAgreement()));
    }

    @Test
    void printJSONContingencyMatrix() {
        LinceContingencyMatrixPrinter printer = new LinceContingencyMatrixPrinter(study);
        JSONArray result = printer.getContingencyMatrixJson(true);
        System.out.println(result);
    }

    @Test
    void arrayIteration() {
        int[][] arrData = {
                {13, 23, 45, 67, 56},
                {43, 65, 76, 89, 90},
                {43, 45, 76, 98, 90},
                {34, 56, 76, 43, 87}};
        for (int[] rowData : arrData) {
            for (int cellData : rowData) {
                System.out.println("the indiviual data is" + cellData);
            }
        }
    }
}
