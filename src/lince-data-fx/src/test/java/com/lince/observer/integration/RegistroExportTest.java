package com.lince.observer.integration;

import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.Registro;
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
 * Comprehensive unit tests for all Registro export methods
 * Created by Claude Code
 */
class RegistroExportTest {

    private Registro registro;
    private List<Criterio> criterios;
    private List<LegacyToolException> exceptions;

    @BeforeEach
    void setUp() throws Exception {
        exceptions = new ArrayList<>();

        // Load test files
        File linceRegister = new File(getClass().getResource("/lince1/import/Registro_prueba.rlince").getFile());
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());

        InstrumentoObservacional.loadInstance(linceInstrument);
        registro = Registro.cargarRegistro(linceRegister, exceptions);

        assertNotNull(registro, "Registro should be loaded successfully");
        criterios = Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
    }

    // ============================================================
    // GSEQ Interval Export Tests
    // ============================================================

    @Test
    void testExportToSdisGseqIntervalo_WithValidData() {
        // Act
        String result = registro.exportToSdisGseqIntervalo(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");

        // Should start with Interval specification
        assertTrue(result.startsWith("Interval="),
            "Export should start with 'Interval=' specification");

        // Should contain interval time value
        String firstLine = result.split("\r\n")[0];
        assertTrue(firstLine.matches("Interval=\\d+'"),
            "First line should be in format 'Interval=N': " + firstLine);

        // Should contain variable declarations
        assertTrue(result.contains("="), "Should contain variable declarations");

        // Should contain session marker
        assertTrue(result.contains("<S01>"), "Should contain session marker <S01>");

        // Should contain timing markers (*)
        assertTrue(result.contains("*"), "Should contain timing markers");

        // Should end with session closing
        assertTrue(result.contains(")/"), "Should contain session end marker");
    }

    @Test
    void testExportToSdisGseqIntervalo_TimingFormat() {
        // Act
        String result = registro.exportToSdisGseqIntervalo(criterios);

        // Assert - Timing should be in format: *N,code where N is the interval
        String[] lines = result.split("\r\n");
        boolean foundTimingPattern = false;

        for (String line : lines) {
            if (line.contains("*") && line.contains(",")) {
                // Check if timing comes before data: *N,data
                assertTrue(line.matches(".*\\*\\d+,.*"),
                    "Timing should be in format *N,data: " + line);
                foundTimingPattern = true;
            }
        }

        assertTrue(foundTimingPattern, "Should contain timing patterns in the output");
    }

    @Test
    void testExportToSdisGseqIntervalo_EmptyCriteriosList() {
        // Act
        String result = registro.exportToSdisGseqIntervalo(Arrays.asList());

        // Assert
        assertNotNull(result, "Export should not return null even with empty criterios");
        assertTrue(result.startsWith("Interval="), "Should still have Interval specification");
    }

    @Test
    void testExportToSdisGseqIntervalo_IntervalCalculation() {
        // Act
        String result = registro.exportToSdisGseqIntervalo(criterios);

        // Assert - Extract and validate interval value
        String firstLine = result.split("\r\n")[0];
        String intervalValue = firstLine.replace("Interval=", "").replace("'", "");
        int interval = Integer.parseInt(intervalValue);

        // Should be a positive value
        assertTrue(interval > 0, "Interval should be positive");

        // Should be reasonable (between 1 and 3600 seconds)
        assertTrue(interval >= 1 && interval <= 3600,
            "Interval should be reasonable (1-3600 seconds): " + interval);
    }

    // ============================================================
    // GSEQ Event Export Tests
    // ============================================================

    @Test
    void testExportToSdisGseqEvento_WithValidData() {
        // Act
        String result = registro.exportToSdisGseqEvento(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.startsWith("Event\r\n"), "Export should start with 'Event'");
        assertTrue(result.contains("="), "Should contain variable declarations");
        assertTrue(result.contains("/"), "Should contain end marker '/'");
    }

    @Test
    void testExportToSdisGseqEvento_EmptyCriteriosList() {
        // Act
        String result = registro.exportToSdisGseqEvento(Arrays.asList());

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("Event\r\n"));
    }

    // ============================================================
    // GSEQ Multievent Export Tests
    // ============================================================

    @Test
    void testExportToSdisGseqMultievento_WithValidData() {
        // Act
        String result = registro.exportToSdisGseqMultievento(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.startsWith("Multievent\r\n"), "Export should start with 'Multievent'");
        assertTrue(result.contains("="), "Should contain variable declarations");
        assertTrue(result.contains("/"), "Should contain end marker '/'");
    }

    // ============================================================
    // GSEQ Timed Event Export Tests
    // ============================================================

    @Test
    void testExportToSdisGseqEventoConTiempo_WithValidData() {
        // Act
        String result = registro.exportToSdisGseqEventoConTiempo(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.startsWith("Timed\r\n"), "Export should start with 'Timed'");
        assertTrue(result.contains("="), "Should contain variable declarations");
        assertTrue(result.contains(",") || result.contains("-"), "Should contain time information");
        assertTrue(result.contains("/"), "Should contain end marker '/'");
    }

    // ============================================================
    // GSEQ State Export Tests
    // ============================================================

    @Test
    void testExportToSdisGseqEstado_WithValidData() {
        // Act
        String result = registro.exportToSdisGseqEstado(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.startsWith("State\r\n"), "Export should start with 'State'");
        assertTrue(result.contains("="), "Should contain variable declarations");
        assertTrue(result.contains("/"), "Should contain end marker '/'");
    }

    // ============================================================
    // Theme Export Tests
    // ============================================================

    @Test
    void testExportToTheme5_WithValidData() {
        // Act
        String result = registro.exportToTheme5(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.contains("DATANAME"), "Should contain DATANAME header");
        assertTrue(result.contains("TIME"), "Should contain TIME header");
        assertTrue(result.contains("EVENT"), "Should contain EVENT header");
        assertTrue(result.contains(";"), "Should use semicolon separator");
    }

    @Test
    void testExportToTheme6_WithValidData() {
        // Act
        String result = registro.exportToTheme6(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.contains("TIME"), "Should contain TIME header");
        assertTrue(result.contains("EVENT"), "Should contain EVENT header");
        assertFalse(result.contains("DATANAME"), "Should not contain DATANAME in Theme6");
        assertTrue(result.contains("\t"), "Should use tab separator");
    }

    // ============================================================
    // SAS Export Tests
    // ============================================================

    @Test
    void testExportToSas_WithValidData() {
        // Act
        List<Object> criteriosList = new ArrayList<>(criterios);
        String result = registro.exportToSas(criteriosList);

        // Assert
        assertNotNull(result, "Export result should not be null");
        // SAS export may be empty if no matching data, so we just check it doesn't throw
    }

    @Test
    void testExportToSas_WithEmptyList() {
        // Act & Assert
        assertDoesNotThrow(() -> registro.exportToSas(new ArrayList<>()));
    }

    // ============================================================
    // CSV Export Tests
    // ============================================================

    @Test
    void testExportToCsv_WithCommaDelimiter() {
        // Arrange
        List<Object> columnas = new ArrayList<>(criterios);

        // Act
        String result = registro.exportToCsv(columnas, true);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.contains(","), "Should use comma delimiter");

        // Should have header row
        String[] lines = result.split("\r\n");
        assertTrue(lines.length > 0, "Should have at least a header row");
    }

    @Test
    void testExportToCsv_WithSemicolonDelimiter() {
        // Arrange
        List<Object> columnas = new ArrayList<>(criterios);

        // Act
        String result = registro.exportToCsv(columnas, false);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");
        assertTrue(result.contains(";"), "Should use semicolon delimiter");

        // Should have header row
        String[] lines = result.split("\r\n");
        assertTrue(lines.length > 0, "Should have at least a header row");
    }

    @Test
    void testExportToCsv_EmptyList() {
        // Act
        String result = registro.exportToCsv(new ArrayList<>(), true);

        // Assert
        assertNotNull(result);
        // Empty list may result in just a header or empty string, both are valid
        // Just verify it doesn't throw an exception (already done by getting here)
    }

    // ============================================================
    // General Export Tests
    // ============================================================

    @Test
    void testAllExportMethods_DoNotThrowException() {
        // This test ensures all export methods can be called without throwing exceptions

        assertDoesNotThrow(() -> registro.exportToSdisGseqIntervalo(criterios),
            "exportToSdisGseqIntervalo should not throw");
        assertDoesNotThrow(() -> registro.exportToSdisGseqEvento(criterios),
            "exportToSdisGseqEvento should not throw");
        assertDoesNotThrow(() -> registro.exportToSdisGseqMultievento(criterios),
            "exportToSdisGseqMultievento should not throw");
        assertDoesNotThrow(() -> registro.exportToSdisGseqEventoConTiempo(criterios),
            "exportToSdisGseqEventoConTiempo should not throw");
        assertDoesNotThrow(() -> registro.exportToSdisGseqEstado(criterios),
            "exportToSdisGseqEstado should not throw");
        assertDoesNotThrow(() -> registro.exportToTheme5(criterios),
            "exportToTheme5 should not throw");
        assertDoesNotThrow(() -> registro.exportToTheme6(criterios),
            "exportToTheme6 should not throw");
        assertDoesNotThrow(() -> registro.exportToCsv(new ArrayList<>(criterios), true),
            "exportToCsv with comma should not throw");
        assertDoesNotThrow(() -> registro.exportToCsv(new ArrayList<>(criterios), false),
            "exportToCsv with semicolon should not throw");
    }

    @Test
    void testExportConsistency_SameInputProducesSameOutput() {
        // All export methods should be deterministic (produce same output for same input)

        String result1 = registro.exportToSdisGseqIntervalo(criterios);
        String result2 = registro.exportToSdisGseqIntervalo(criterios);
        assertEquals(result1, result2, "exportToSdisGseqIntervalo should be deterministic");

        result1 = registro.exportToSdisGseqEvento(criterios);
        result2 = registro.exportToSdisGseqEvento(criterios);
        assertEquals(result1, result2, "exportToSdisGseqEvento should be deterministic");

        result1 = registro.exportToSdisGseqEstado(criterios);
        result2 = registro.exportToSdisGseqEstado(criterios);
        assertEquals(result1, result2, "exportToSdisGseqEstado should be deterministic");

        result1 = registro.exportToTheme5(criterios);
        result2 = registro.exportToTheme5(criterios);
        assertEquals(result1, result2, "exportToTheme5 should be deterministic");

        result1 = registro.exportToCsv(new ArrayList<>(criterios), true);
        result2 = registro.exportToCsv(new ArrayList<>(criterios), true);
        assertEquals(result1, result2, "exportToCsv should be deterministic");
    }

    @Test
    void testExports_ContainValidStructure() {
        // Verify basic structure of exports

        String intervalExport = registro.exportToSdisGseqIntervalo(criterios);
        assertTrue(intervalExport.contains("\r\n"), "Should use Windows line endings");

        String csvExport = registro.exportToCsv(new ArrayList<>(criterios), true);
        assertTrue(csvExport.contains("\r\n"), "Should use Windows line endings");
    }
}
