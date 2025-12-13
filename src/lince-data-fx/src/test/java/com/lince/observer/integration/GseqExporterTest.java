package com.lince.observer.integration;

import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.export.*;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GSEQ Exporter classes.
 * <p>
 * These tests verify that the new object-oriented exporter design produces
 * the same output as the existing Registro methods.
 */
class GseqExporterTest {

    private Registro registro;
    private List<Criterio> criterios;

    @BeforeEach
    void setUp() throws Exception {
        List<LegacyToolException> exceptions = new ArrayList<>();

        // Load test files
        File linceRegister = new File(getClass().getResource("/lince1/import/Registro_prueba.rlince").getFile());
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());

        InstrumentoObservacional.loadInstance(linceInstrument);
        registro = Registro.cargarRegistro(linceRegister, exceptions);

        assertNotNull(registro, "Registro should be loaded successfully");
        criterios = Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
    }

    @Test
    void testEventExporter_ProducesSameOutputAsRegistro() {
        // Export using existing method
        String expectedOutput = registro.exportToSdisGseqEvento(criterios);

        // Export using new exporter
        GseqExporter exporter = GseqExporterFactory.createEventExporter();
        String actualOutput = exporter.export(criterios, registro.datosVariables);

        assertEquals(expectedOutput, actualOutput,
            "EventExporter should produce the same output as Registro.exportToSdisGseqEvento()");
    }

    @Test
    void testMultieventExporter_ProducesSameOutputAsRegistro() {
        // Export using existing method
        String expectedOutput = registro.exportToSdisGseqMultievento(criterios);

        // Export using new exporter
        GseqExporter exporter = GseqExporterFactory.createMultieventExporter();
        String actualOutput = exporter.export(criterios, registro.datosVariables);

        assertEquals(expectedOutput, actualOutput,
            "MultieventExporter should produce the same output as Registro.exportToSdisGseqMultievento()");
    }

    @Test
    void testTimedExporter_ProducesSameOutputAsRegistro() {
        // Export using existing method
        String expectedOutput = registro.exportToSdisGseqEventoConTiempo(criterios);

        // Export using new exporter
        GseqExporter exporter = GseqExporterFactory.createTimedExporter();
        String actualOutput = exporter.export(criterios, registro.datosVariables);

        assertEquals(expectedOutput, actualOutput,
            "TimedExporter should produce the same output as Registro.exportToSdisGseqEventoConTiempo()");
    }

    @Test
    void testStateExporter_ProducesSameOutputAsRegistro() {
        // Export using existing method
        String expectedOutput = registro.exportToSdisGseqEstado(criterios);

        // Export using new exporter
        GseqExporter exporter = GseqExporterFactory.createStateExporter();
        String actualOutput = exporter.export(criterios, registro.datosVariables);

        assertEquals(expectedOutput, actualOutput,
            "StateExporter should produce the same output as Registro.exportToSdisGseqEstado()");
    }

    @Test
    void testIntervalExporter_ProducesSameOutputAsRegistro() {
        // Export using existing method
        String expectedOutput = registro.exportToSdisGseqIntervalo(criterios);

        // Export using new exporter with auto-calculated interval
        GseqExporter exporter = GseqExporterFactory.createIntervalExporter(registro.datosVariables);
        String actualOutput = exporter.export(criterios, registro.datosVariables);

        assertEquals(expectedOutput, actualOutput,
            "IntervalExporter should produce the same output as Registro.exportToSdisGseqIntervalo()");
    }

    @Test
    void testGseqExporterFactory_CreateEventExporter() {
        GseqExporter exporter = GseqExporterFactory.createExporter(GseqExporterFactory.GseqFormatType.EVENT);
        assertNotNull(exporter);
        assertInstanceOf(EventExporter.class, exporter);
        assertEquals("Event", exporter.getFormatName());
    }

    @Test
    void testGseqExporterFactory_CreateMultieventExporter() {
        GseqExporter exporter = GseqExporterFactory.createExporter(GseqExporterFactory.GseqFormatType.MULTIEVENT);
        assertNotNull(exporter);
        assertInstanceOf(MultieventExporter.class, exporter);
        assertEquals("Multievent", exporter.getFormatName());
    }

    @Test
    void testGseqExporterFactory_CreateTimedExporter() {
        GseqExporter exporter = GseqExporterFactory.createExporter(GseqExporterFactory.GseqFormatType.TIMED);
        assertNotNull(exporter);
        assertInstanceOf(TimedExporter.class, exporter);
        assertEquals("Timed", exporter.getFormatName());
    }

    @Test
    void testGseqExporterFactory_CreateStateExporter() {
        GseqExporter exporter = GseqExporterFactory.createExporter(GseqExporterFactory.GseqFormatType.STATE);
        assertNotNull(exporter);
        assertInstanceOf(StateExporter.class, exporter);
        assertEquals("State", exporter.getFormatName());
    }

    @Test
    void testGseqExporterFactory_IntervalRequiresSpecialMethod() {
        assertThrows(IllegalArgumentException.class, () -> {
            GseqExporterFactory.createExporter(GseqExporterFactory.GseqFormatType.INTERVAL);
        }, "Creating INTERVAL exporter via createExporter() should throw exception");
    }

    @Test
    void testGseqExporterFactory_NullFormatTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GseqExporterFactory.createExporter(null);
        }, "Creating exporter with null format type should throw exception");
    }

    @Test
    void testIntervalExporter_WithSpecificInterval() {
        IntervalExporter exporter = GseqExporterFactory.createIntervalExporter(15);
        assertEquals(15, exporter.getIntervalSeconds());

        String output = exporter.export(criterios, registro.datosVariables);
        assertTrue(output.startsWith("Interval=15'"),
            "Output should start with specified interval duration");
    }

    @Test
    void testIntervalExporter_SetIntervalSeconds() {
        IntervalExporter exporter = GseqExporterFactory.createIntervalExporter(10);
        assertEquals(10, exporter.getIntervalSeconds());

        exporter.setIntervalSeconds(20);
        assertEquals(20, exporter.getIntervalSeconds());

        String output = exporter.export(criterios, registro.datosVariables);
        assertTrue(output.startsWith("Interval=20'"),
            "Output should reflect updated interval duration");
    }

    @Test
    void testAllExporters_ProduceValidGseqFormat() {
        // Test that all exporters produce valid GSEQ format structure
        GseqExporter[] exporters = {
            GseqExporterFactory.createEventExporter(),
            GseqExporterFactory.createMultieventExporter(),
            GseqExporterFactory.createTimedExporter(),
            GseqExporterFactory.createStateExporter(),
            GseqExporterFactory.createIntervalExporter(registro.datosVariables)
        };

        for (GseqExporter exporter : exporters) {
            String output = exporter.export(criterios, registro.datosVariables);

            // All GSEQ formats must contain variable declarations
            assertTrue(output.contains("($") || output.contains("  "),
                exporter.getFormatName() + " output should contain variable declarations");

            // All GSEQ formats must end with forward slash
            assertTrue(output.contains("/"),
                exporter.getFormatName() + " output should contain sequence terminator (/)");

            // All GSEQ formats must use Windows line endings
            assertTrue(output.contains("\r\n"),
                exporter.getFormatName() + " output should use Windows line endings (\\r\\n)");

            // Output should not be empty
            assertFalse(output.trim().isEmpty(),
                exporter.getFormatName() + " output should not be empty");
        }
    }
}
