package com.lince.observer.legacy.instrumentoObservacional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InstrumentoObservacional export methods
 * Created by Claude Code
 */
class InstrumentoObservacionalExportTest {

    private InstrumentoObservacional instrumento;
    private List<Criterio> criterios;

    @BeforeEach
    void setUp() throws Exception {
        // Load a test instrument file
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());
        InstrumentoObservacional.loadInstance(linceInstrument);
        instrumento = InstrumentoObservacional.getInstance();

        // Get all criterios for testing
        criterios = Arrays.asList(instrumento.getCriterios());
    }

    @Test
    void testExportToSdisGseq_WithValidCriterios() {
        // Act
        String result = instrumento.exportToSdisGseq(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");

        // Should contain variable declarations with format "  name = code"
        assertTrue(result.contains("="), "Should contain variable assignments");

        // Should start with two spaces for indentation
        String[] lines = result.split("\r\n");
        boolean hasIndentedLines = false;
        for (String line : lines) {
            if (line.startsWith("  ") && line.contains("=")) {
                hasIndentedLines = true;
                break;
            }
        }
        assertTrue(hasIndentedLines, "Should have indented variable declarations");

        // Should end with metadata line
        assertTrue(result.contains("* Metadata;"), "Should contain metadata line");
    }

    @Test
    void testExportToSdisGseq_WithEmptyCriterios() {
        // Act
        String result = instrumento.exportToSdisGseq(Arrays.asList());

        // Assert
        assertNotNull(result, "Export result should not be null");
        // Should still have metadata line even with no criterios
        assertTrue(result.contains("* Metadata;"), "Should contain metadata line even with empty criterios");
    }

    @Test
    void testExportToSdisGseq_FormatValidation() {
        // Act
        String result = instrumento.exportToSdisGseq(criterios);

        // Assert
        String[] lines = result.split("\r\n");

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("*")) {
                continue; // Skip empty lines and metadata
            }

            // Each variable declaration should have format: "  name = code"
            if (line.contains("=")) {
                assertTrue(line.startsWith("  "),
                    "Variable declarations should start with two spaces: " + line);

                String[] parts = line.trim().split("=");
                assertEquals(2, parts.length,
                    "Variable declaration should have exactly one '=' sign: " + line);

                assertFalse(parts[0].trim().isEmpty(),
                    "Variable name should not be empty: " + line);
                assertFalse(parts[1].trim().isEmpty(),
                    "Variable code should not be empty: " + line);
            }
        }
    }

    @Test
    void testExportToTheme_WithValidCriterios() {
        // Act
        String result = instrumento.exportToTheme(criterios);

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertFalse(result.isEmpty(), "Export result should not be empty");

        // Should contain criterion names
        for (Criterio criterio : criterios) {
            assertTrue(result.contains(criterio.getNombre()),
                "Should contain criterion name: " + criterio.getNombre());
        }
    }

    @Test
    void testExportToTheme_WithEmptyCriterios() {
        // Act
        String result = instrumento.exportToTheme(Arrays.asList());

        // Assert
        assertNotNull(result, "Export result should not be null");
        assertTrue(result.isEmpty(), "Export result should be empty for empty criterios list");
    }

    @Test
    void testExportToSdisGseq_VariableNaming() {
        // Act
        String result = instrumento.exportToSdisGseq(criterios);

        // Assert
        String[] lines = result.split("\r\n");

        for (String line : lines) {
            if (line.contains("=") && !line.startsWith("*")) {
                String[] parts = line.trim().split("=");
                String varName = parts[0].trim();

                // Variable names should not contain spaces
                assertFalse(varName.contains(" "),
                    "Variable name should not contain spaces: " + varName);

                // Variable names should be lowercase
                assertEquals(varName, varName.toLowerCase(),
                    "Variable name should be lowercase: " + varName);
            }
        }
    }

    @Test
    void testExportToSdisGseq_ConsistentFormat() {
        // Act - export twice
        String result1 = instrumento.exportToSdisGseq(criterios);
        String result2 = instrumento.exportToSdisGseq(criterios);

        // Assert - should produce identical output
        assertEquals(result1, result2,
            "Multiple exports with same input should produce identical output");
    }
}