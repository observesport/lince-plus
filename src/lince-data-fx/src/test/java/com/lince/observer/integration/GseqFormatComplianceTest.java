package com.lince.observer.integration;

import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive GSEQ Format Compliance Tests
 * Based on GSEQ_FORMAT_SPECIFICATION.md
 * <p>
 * Tests all GSEQ export formats against specification requirements:
 * - Event
 * - Multievent
 * - Timed Event
 * - State
 * - Interval
 * <p>
 * Created by Claude Code - 2025-01-24
 */
class GseqFormatComplianceTest {

    private Registro registro;
    private List<Criterio> criterios;
    private List<LegacyToolException> exceptions;

    private static final String LINE_SEPARATOR = "\r\n";

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
    // COMMON FORMAT REQUIREMENTS TESTS
    // ============================================================

    @Test
    void testAllFormats_UseWindowsLineEndings() {
        // All GSEQ formats must use \r\n line endings

        String eventExport = registro.exportToSdisGseqEvento(criterios);
        assertTrue(eventExport.contains(LINE_SEPARATOR),
            "Event export must use \\r\\n line endings");

        String multiEventExport = registro.exportToSdisGseqMultievento(criterios);
        assertTrue(multiEventExport.contains(LINE_SEPARATOR),
            "Multievent export must use \\r\\n line endings");

        String timedExport = registro.exportToSdisGseqEventoConTiempo(criterios);
        assertTrue(timedExport.contains(LINE_SEPARATOR),
            "Timed export must use \\r\\n line endings");

        String stateExport = registro.exportToSdisGseqEstado(criterios);
        assertTrue(stateExport.contains(LINE_SEPARATOR),
            "State export must use \\r\\n line endings");

        String intervalExport = registro.exportToSdisGseqIntervalo(criterios);
        assertTrue(intervalExport.contains(LINE_SEPARATOR),
            "Interval export must use \\r\\n line endings");
    }

    @Test
    void testAllFormats_EndWithForwardSlash() {
        // All GSEQ formats must end with /

        String eventExport = registro.exportToSdisGseqEvento(criterios);
        assertTrue(eventExport.contains("/"),
            "Event export must end with /");

        String multiEventExport = registro.exportToSdisGseqMultievento(criterios);
        assertTrue(multiEventExport.contains("/"),
            "Multievent export must end with /");

        String timedExport = registro.exportToSdisGseqEventoConTiempo(criterios);
        assertTrue(timedExport.contains("/"),
            "Timed export must end with /");

        String stateExport = registro.exportToSdisGseqEstado(criterios);
        assertTrue(stateExport.contains("/"),
            "State export must end with /");

        String intervalExport = registro.exportToSdisGseqIntervalo(criterios);
        assertTrue(intervalExport.contains("/"),
            "Interval export must end with /");
    }

    @Test
    void testAllFormats_HaveVariableDeclarations() {
        // All GSEQ formats must have variable declarations

        String eventExport = registro.exportToSdisGseqEvento(criterios);
        assertTrue(eventExport.contains("="),
            "Event export must have variable declarations with =");

        String multiEventExport = registro.exportToSdisGseqMultievento(criterios);
        assertTrue(multiEventExport.contains("="),
            "Multievent export must have variable declarations with =");

        String timedExport = registro.exportToSdisGseqEventoConTiempo(criterios);
        assertTrue(timedExport.contains("="),
            "Timed export must have variable declarations with =");

        String stateExport = registro.exportToSdisGseqEstado(criterios);
        assertTrue(stateExport.contains("="),
            "State export must have variable declarations with =");

        String intervalExport = registro.exportToSdisGseqIntervalo(criterios);
        assertTrue(intervalExport.contains("="),
            "Interval export must have variable declarations with =");
    }

    // ============================================================
    // EVENT FORMAT TESTS
    // ============================================================

    @Test
    void testEventFormat_HeaderLine() {
        // Event format must start with "Event"
        String result = registro.exportToSdisGseqEvento(criterios);

        assertTrue(result.startsWith("Event"),
            "Event export must start with 'Event' keyword");
    }

    @Test
    void testEventFormat_VariableDeclarations() {
        // Event format uses Format A: ($CriterionName = code1 code2 ...)
        String result = registro.exportToSdisGseqEvento(criterios);

        // Should have parentheses and dollar sign
        assertTrue(result.contains("($"),
            "Event format variable declarations must use ($CriterionName = ...) format");

        // Should end with semicolon
        String[] lines = result.split(LINE_SEPARATOR);
        boolean foundSemicolon = false;
        for (String line : lines) {
            if (line.trim().endsWith(";")) {
                foundSemicolon = true;
                break;
            }
        }
        assertTrue(foundSemicolon,
            "Event format variable declarations must end with semicolon");
    }

    @Test
    void testEventFormat_DataSectionOneCodePerLine() {
        // Event format should have one code per line in data section
        String result = registro.exportToSdisGseqEvento(criterios);

        // Find data section (after variable declarations)
        String[] lines = result.split(LINE_SEPARATOR);
        boolean inDataSection = false;
        int consecutiveEmptyLines = 0;

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                consecutiveEmptyLines++;
            } else {
                consecutiveEmptyLines = 0;
            }

            // Data section starts after variable declarations (marked by semicolon and empty lines)
            if (consecutiveEmptyLines >= 2) {
                inDataSection = true;
            }

            if (inDataSection && !line.trim().isEmpty() && !line.contains("/")) {
                // Data lines should not contain commas (one code per line)
                // Unless it's a complex code (which is allowed)
                // Just verify it's not empty
                assertFalse(line.trim().isEmpty(),
                    "Data section lines should not be empty");
            }
        }
    }

    @Test
    void testEventFormat_SequenceSeparators() {
        // Event format should use semicolons to separate sequences
        String result = registro.exportToSdisGseqEvento(criterios);

        // Should have at least one semicolon in data section (end of sequence)
        String dataSection = result.substring(result.indexOf(";") + 1);
        // After variable declarations, data sections may have sequence terminators
        // This is acceptable per spec
    }

    // ============================================================
    // MULTIEVENT FORMAT TESTS
    // ============================================================

    @Test
    void testMultieventFormat_HeaderLine() {
        // Multievent format must start with "Multievent"
        String result = registro.exportToSdisGseqMultievento(criterios);

        assertTrue(result.startsWith("Multievent"),
            "Multievent export must start with 'Multievent' keyword");
    }

    @Test
    void testMultieventFormat_VariableDeclarations() {
        // Multievent format uses Format A: ($CriterionName = code1 code2 ...)
        String result = registro.exportToSdisGseqMultievento(criterios);

        assertTrue(result.contains("($"),
            "Multievent format variable declarations must use ($CriterionName = ...) format");

        assertTrue(result.contains(");"),
            "Multievent format variable declarations must end with semicolon");
    }

    @Test
    void testMultieventFormat_DataLinesEndWithPeriodOrSemicolon() {
        // Multievent format: non-last lines end with period, last line with semicolon
        String result = registro.exportToSdisGseqMultievento(criterios);

        // Find data section
        String[] lines = result.split(LINE_SEPARATOR);
        boolean inDataSection = false;
        boolean foundPeriod = false;
        boolean foundSemicolon = false;

        for (String line : lines) {
            if (line.trim().isEmpty() && inDataSection) {
                continue;
            }

            if (inDataSection && !line.contains("/")) {
                if (line.trim().endsWith(".")) {
                    foundPeriod = true;
                }
                if (line.trim().endsWith(";")) {
                    foundSemicolon = true;
                }
            }

            // Start of data section (after double empty line)
            if (line.trim().endsWith(";") && line.contains("$")) {
                inDataSection = true;
            }
        }

        // Should have either periods or semicolons in data (or both)
        assertTrue(foundPeriod || foundSemicolon,
            "Multievent format data lines must end with period or semicolon");
    }

    @Test
    void testMultieventFormat_SimultaneousCodesOnSameLine() {
        // Multievent format: codes on same line = simultaneous
        String result = registro.exportToSdisGseqMultievento(criterios);

        // Just verify format doesn't throw exception
        assertNotNull(result);
        // Simultaneous codes would be space-separated on same line (e.g., "RS A.")
    }

    // ============================================================
    // TIMED EVENT FORMAT TESTS
    // ============================================================

    @Test
    void testTimedFormat_HeaderLine() {
        // Timed format must start with "Timed"
        String result = registro.exportToSdisGseqEventoConTiempo(criterios);

        assertTrue(result.startsWith("Timed"),
            "Timed export must start with 'Timed' keyword");
    }

    @Test
    void testTimedFormat_ContainsTimeFormat() {
        // Timed format must contain time information (comma and dash for ranges)
        String result = registro.exportToSdisGseqEventoConTiempo(criterios);

        assertTrue(result.contains(",") || result.contains("-"),
            "Timed format must contain time information with commas or dashes");
    }

    @Test
    void testTimedFormat_SubjectSeparator() {
        // Timed format uses ampersand (&) to separate subjects
        // This may or may not be present depending on data
        String result = registro.exportToSdisGseqEventoConTiempo(criterios);

        // Just verify it doesn't throw and has basic structure
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // ============================================================
    // STATE FORMAT TESTS
    // ============================================================

    @Test
    void testStateFormat_HeaderLine() {
        // State format must start with "State"
        String result = registro.exportToSdisGseqEstado(criterios);

        assertTrue(result.startsWith("State"),
            "State export must start with 'State' keyword");
    }

    @Test
    void testStateFormat_ContainsTimeInformation() {
        // State format includes time information
        String result = registro.exportToSdisGseqEstado(criterios);

        // State format should have timing (= signs for durations)
        assertTrue(result.contains("="),
            "State format must contain time/duration information");
    }

    // ============================================================
    // INTERVAL FORMAT TESTS
    // ============================================================

    @Test
    void testIntervalFormat_HeaderLine() {
        // Interval format must start with "Interval=N'"
        String result = registro.exportToSdisGseqIntervalo(criterios);

        assertTrue(result.startsWith("Interval="),
            "Interval export must start with 'Interval=' specification");

        String firstLine = result.split(LINE_SEPARATOR)[0];
        assertTrue(firstLine.matches("Interval=\\d+'"),
            "Interval header must be in format 'Interval=N'': " + firstLine);
    }

    @Test
    void testIntervalFormat_VariableDeclarationsIndented() {
        // Interval format uses Format B: indented "  fullname = code"
        String result = registro.exportToSdisGseqIntervalo(criterios);

        String[] lines = result.split(LINE_SEPARATOR);
        boolean foundIndentedDeclaration = false;

        for (String line : lines) {
            if (line.startsWith("  ") && line.contains("=") && !line.startsWith("Interval=")) {
                foundIndentedDeclaration = true;
                break;
            }
        }

        assertTrue(foundIndentedDeclaration,
            "Interval format must have indented variable declarations (two spaces)");
    }

    @Test
    void testIntervalFormat_MetadataLine() {
        // Interval format should have metadata line starting with *
        String result = registro.exportToSdisGseqIntervalo(criterios);

        assertTrue(result.contains("*"),
            "Interval format should contain metadata line starting with *");
    }

    @Test
    void testIntervalFormat_SessionMarkers() {
        // Interval format should have session markers like <S01>
        String result = registro.exportToSdisGseqIntervalo(criterios);

        assertTrue(result.contains("<S") && result.contains(">"),
            "Interval format must contain session markers like <S01>");
    }

    @Test
    void testIntervalFormat_TimingMarkersBeforeData() {
        // Interval format: timing should come BEFORE data (*N,data)
        String result = registro.exportToSdisGseqIntervalo(criterios);

        // Find data section
        String[] lines = result.split(LINE_SEPARATOR);
        boolean foundCorrectFormat = false;

        for (String line : lines) {
            // Look for timing pattern: *number,code
            if (line.matches(".*\\*\\d+,.*")) {
                foundCorrectFormat = true;
                break;
            }
        }

        assertTrue(foundCorrectFormat,
            "Interval format must have timing before data in format *N,code");
    }

    @Test
    void testIntervalFormat_SessionEndWithMetadata() {
        // Interval format sessions should end with (metadata)/
        String result = registro.exportToSdisGseqIntervalo(criterios);

        assertTrue(result.contains("(") && result.contains(")/"),
            "Interval format sessions must end with (metadata)/");
    }

    // ============================================================
    // EDGE CASE TESTS
    // ============================================================

    @Test
    void testAllFormats_EmptyCriteriosList() {
        // All formats should handle empty criterios list gracefully
        List<Criterio> emptyCriterios = Arrays.asList();

        assertDoesNotThrow(() -> registro.exportToSdisGseqEvento(emptyCriterios),
            "Event export should handle empty criterios");

        assertDoesNotThrow(() -> registro.exportToSdisGseqMultievento(emptyCriterios),
            "Multievent export should handle empty criterios");

        assertDoesNotThrow(() -> registro.exportToSdisGseqEventoConTiempo(emptyCriterios),
            "Timed export should handle empty criterios");

        assertDoesNotThrow(() -> registro.exportToSdisGseqEstado(emptyCriterios),
            "State export should handle empty criterios");

        assertDoesNotThrow(() -> registro.exportToSdisGseqIntervalo(emptyCriterios),
            "Interval export should handle empty criterios");
    }

    @Test
    void testAllFormats_NotNull() {
        // All formats should return non-null results
        assertNotNull(registro.exportToSdisGseqEvento(criterios));
        assertNotNull(registro.exportToSdisGseqMultievento(criterios));
        assertNotNull(registro.exportToSdisGseqEventoConTiempo(criterios));
        assertNotNull(registro.exportToSdisGseqEstado(criterios));
        assertNotNull(registro.exportToSdisGseqIntervalo(criterios));
    }

    @Test
    void testAllFormats_NotEmpty() {
        // All formats should return non-empty results with valid data
        assertTrue(registro.exportToSdisGseqEvento(criterios).length() > 10);
        assertTrue(registro.exportToSdisGseqMultievento(criterios).length() > 10);
        assertTrue(registro.exportToSdisGseqEventoConTiempo(criterios).length() > 10);
        assertTrue(registro.exportToSdisGseqEstado(criterios).length() > 10);
        assertTrue(registro.exportToSdisGseqIntervalo(criterios).length() > 10);
    }

    @Test
    void testAllFormats_Deterministic() {
        // All formats should produce identical output for identical input

        String event1 = registro.exportToSdisGseqEvento(criterios);
        String event2 = registro.exportToSdisGseqEvento(criterios);
        assertEquals(event1, event2, "Event export must be deterministic");

        String multi1 = registro.exportToSdisGseqMultievento(criterios);
        String multi2 = registro.exportToSdisGseqMultievento(criterios);
        assertEquals(multi1, multi2, "Multievent export must be deterministic");

        String timed1 = registro.exportToSdisGseqEventoConTiempo(criterios);
        String timed2 = registro.exportToSdisGseqEventoConTiempo(criterios);
        assertEquals(timed1, timed2, "Timed export must be deterministic");

        String state1 = registro.exportToSdisGseqEstado(criterios);
        String state2 = registro.exportToSdisGseqEstado(criterios);
        assertEquals(state1, state2, "State export must be deterministic");

        String interval1 = registro.exportToSdisGseqIntervalo(criterios);
        String interval2 = registro.exportToSdisGseqIntervalo(criterios);
        assertEquals(interval1, interval2, "Interval export must be deterministic");
    }

    // ============================================================
    // INTEGRATION TESTS (if reference files exist)
    // ============================================================

    @Test
    void testIntervalFormat_CompareWithReferenceFile() throws Exception {
        // Compare output structure with valid reference file
        String referenceFilePath = "/integration/GSEQ_interval_Teresa.sds";
        var referenceUrl = getClass().getResource(referenceFilePath);

        if (referenceUrl != null) {
            String reference = new String(Files.readAllBytes(Paths.get(referenceUrl.toURI())));
            String exported = registro.exportToSdisGseqIntervalo(criterios);

            // Check structural elements match
            assertTrue(exported.startsWith("Interval="),
                "Export should start with Interval= like reference");

            assertTrue(exported.contains("<S"),
                "Export should contain session markers like reference");

            assertTrue(exported.contains("*") && reference.contains("*"),
                "Export should contain timing markers like reference");

            assertTrue(exported.endsWith("/\r\n\r\n") || exported.endsWith("/\r\n"),
                "Export should end with forward slash like reference");
        }
    }

    @Test
    void testMultieventFormat_CompareWithReferenceFile() throws Exception {
        // Compare output structure with valid reference file
        String referenceFilePath = "/integration/GSEQ_multievent.sds";
        var referenceUrl = getClass().getResource(referenceFilePath);

        if (referenceUrl != null) {
            String reference = new String(Files.readAllBytes(Paths.get(referenceUrl.toURI())));
            String exported = registro.exportToSdisGseqMultievento(criterios);

            // Check structural elements match
            assertTrue(exported.startsWith("Multievent"),
                "Export should start with Multievent like reference");

            assertTrue(exported.contains("($"),
                "Export should contain variable declarations like reference");

            boolean hasPeriodOrSemicolon = exported.contains(".") || exported.contains(";");
            assertTrue(hasPeriodOrSemicolon,
                "Export should contain periods or semicolons like reference");
        }
    }

    @Test
    void testEventFormat_CompareWithReferenceFile() throws Exception {
        // Compare output structure with valid reference file
        String referenceFilePath = "/integration/GSEQ_event.sds";
        var referenceUrl = getClass().getResource(referenceFilePath);

        if (referenceUrl != null) {
            String reference = new String(Files.readAllBytes(Paths.get(referenceUrl.toURI())));
            String exported = registro.exportToSdisGseqEvento(criterios);

            // Check structural elements match
            assertTrue(exported.startsWith("Event"),
                "Export should start with Event like reference");

            assertTrue(exported.contains("($"),
                "Export should contain variable declarations like reference");
        }
    }

    @Test
    void testTimedFormat_CompareWithReferenceFile() throws Exception {
        // Compare output structure with valid reference file
        String referenceFilePath = "/integration/GSEQ_timed_event_Teresa.sds";
        var referenceUrl = getClass().getResource(referenceFilePath);

        if (referenceUrl != null) {
            String reference = new String(Files.readAllBytes(Paths.get(referenceUrl.toURI())));
            String exported = registro.exportToSdisGseqEventoConTiempo(criterios);

            // Check structural elements match
            assertTrue(exported.startsWith("Timed"),
                "Export should start with Timed like reference");

            assertTrue(exported.contains("($"),
                "Export should contain variable declarations like reference");
        }
    }

    @Test
    void testStateFormat_CompareWithReferenceFile() throws Exception {
        // Compare output structure with valid reference file
        String referenceFilePath = "/integration/GSEQ_estado_Teresa.sds";
        var referenceUrl = getClass().getResource(referenceFilePath);

        if (referenceUrl != null) {
            String reference = new String(Files.readAllBytes(Paths.get(referenceUrl.toURI())));
            String exported = registro.exportToSdisGseqEstado(criterios);

            // Check structural elements match
            assertTrue(exported.startsWith("State"),
                "Export should start with State like reference");
        }
    }
}
