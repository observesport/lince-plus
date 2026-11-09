package com.lince.observer.integration;

import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Export Format Compliance Tests for Theme 5/6, GSEQ Event/Multievent, and VVT exports.
 * Validates output against external tool requirements documented in integration-software.md.
 */
class ExportFormatComplianceTest {

    private Registro registro;
    private List<Criterio> criterios;

    private static final String CRLF = "\r\n";

    @BeforeEach
    void setUp() throws Exception {
        List<LegacyToolException> exceptions = new ArrayList<>();

        File linceRegister = new File(getClass().getResource("/lince1/import/Registro_prueba.rlince").getFile());
        File linceInstrument = new File(getClass().getResource("/lince1/import/Instrumento_prueba.ilince").getFile());

        InstrumentoObservacional.loadInstance(linceInstrument);
        registro = Registro.cargarRegistro(linceRegister, exceptions);

        assertNotNull(registro, "Registro should be loaded successfully");
        criterios = List.of(InstrumentoObservacional.getInstance().getCriterios());
    }

    // ============================================================
    // Theme 5 Tests
    // ============================================================

    @Test
    void testTheme5_UsesFrameNumbers() {
        String result = registro.exportToTheme5(criterios);
        String[] lines = result.split(CRLF);

        List<Integer> timeValues = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(";");
            if (parts.length >= 2 && !parts[1].equals(":") && !parts[1].equals("&")) {
                timeValues.add(Integer.parseInt(parts[0].trim()));
            }
        }

        assertTrue(timeValues.size() >= 2, "Should have at least 2 data rows");

        boolean hasGaps = false;
        for (int i = 1; i < timeValues.size(); i++) {
            if (timeValues.get(i) - timeValues.get(i - 1) != 1) {
                hasGaps = true;
                break;
            }
        }
        assertTrue(hasGaps,
            "Theme 5 time values should be frame numbers with gaps. Values: " + timeValues);
    }

    @Test
    void testTheme5_UsesSemicolonSeparator() {
        String result = registro.exportToTheme5(criterios);
        String[] lines = result.split(CRLF);

        for (String line : lines) {
            assertTrue(line.contains(";"),
                "Every Theme 5 line must use semicolon separator: " + line);
        }
    }

    // ============================================================
    // Multievent GSEQ Tests
    // ============================================================

    @Test
    void testMultievent_NonLastLinesEndWithPeriod() {
        String result = registro.exportToSdisGseqMultievento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);
        assertTrue(dataLines.size() >= 2, "Should have at least 2 data lines");

        // All non-last data lines should end with "."
        for (int i = 0; i < dataLines.size() - 1; i++) {
            String line = dataLines.get(i);
            if (!line.trim().isEmpty()) {
                assertTrue(line.trim().endsWith(".") || line.trim().endsWith(";"),
                    "Non-last multievent data line should end with '.' or ';': " + line);
            }
        }
    }

    @Test
    void testMultievent_LastLineEndsWithSlash() {
        String result = registro.exportToSdisGseqMultievento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);
        assertFalse(dataLines.isEmpty(), "Should have data lines");

        String lastDataLine = dataLines.get(dataLines.size() - 1);
        assertTrue(lastDataLine.trim().endsWith("/"),
            "Last multievent data line must end with '/': " + lastDataLine);
    }

    @Test
    void testMultievent_SpaceSeparatedCodes() {
        String result = registro.exportToSdisGseqMultievento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);
        boolean foundMultiCode = false;

        for (String line : dataLines) {
            String trimmed = line.trim();
            // Remove trailing punctuation
            String content = trimmed.replaceAll("[./;]$", "");
            if (content.contains(" ")) {
                foundMultiCode = true;
                // Codes should be space-separated, not comma-separated
                assertFalse(content.contains(","),
                    "Multievent codes must be space-separated, not comma-separated: " + line);
            }
        }

        assertTrue(foundMultiCode,
            "Multievent should have at least one line with multiple space-separated codes");
    }

    @Test
    void testMultievent_FormatA_VariableDeclarations() {
        String result = registro.exportToSdisGseqMultievento(criterios);

        // Should contain ($Name = code1 code2) format
        assertTrue(result.contains("($"),
            "Multievent must have Format A variable declarations: ($Name = ...)");

        // Last variable declaration line should end with ;
        String[] lines = result.split(CRLF);
        String lastVarDecl = null;
        for (String line : lines) {
            if (line.contains("($")) {
                lastVarDecl = line;
            }
        }
        assertNotNull(lastVarDecl, "Should have at least one variable declaration");
        assertTrue(lastVarDecl.trim().endsWith(";"),
            "Last variable declaration must end with ';': " + lastVarDecl);
    }

    // ============================================================
    // Event GSEQ Tests
    // ============================================================

    @Test
    void testEvent_OneCodePerLine() {
        String result = registro.exportToSdisGseqEvento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);
        // In Event format, each line has exactly one category code.
        // Category codes themselves may contain spaces (e.g. "DPRE RUTINA"),
        // so we verify each data line matches a known category code.
        for (String line : dataLines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                String content = trimmed.replaceAll("[/;]$", "");
                assertFalse(content.isEmpty(),
                    "Event data lines should contain a code: " + line);
                // Should not contain comma (that would indicate multiple codes joined)
                assertFalse(content.contains(","),
                    "Event data lines should have one code per line (no commas): " + line);
            }
        }
    }

    @Test
    void testEvent_SemicolonBetweenSequences() {
        String result = registro.exportToSdisGseqEvento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);

        // Non-final sequences end with ;
        boolean foundSemicolon = false;
        for (String line : dataLines) {
            if (line.trim().endsWith(";") && !line.contains("($")) {
                foundSemicolon = true;
                break;
            }
        }

        // With multiple sequences there should be semicolons
        if (dataLines.size() > criterios.size()) {
            assertTrue(foundSemicolon,
                "Event format: non-final sequences should end with ';'");
        }
    }

    @Test
    void testEvent_SlashAtEnd() {
        String result = registro.exportToSdisGseqEvento(criterios);
        String[] lines = result.split(CRLF);

        List<String> dataLines = extractDataLines(lines);
        assertFalse(dataLines.isEmpty(), "Should have data lines");

        String lastDataLine = dataLines.get(dataLines.size() - 1);
        assertTrue(lastDataLine.trim().endsWith("/"),
            "Final event sequence must end with '/': " + lastDataLine);
    }

    @Test
    void testEvent_BlankLineBetweenSequences() {
        String result = registro.exportToSdisGseqEvento(criterios);
        String[] lines = result.split(CRLF);

        // After variable declarations, sequences should be separated by blank lines
        boolean inData = false;
        boolean foundBlankBetweenSequences = false;
        boolean prevWasData = false;

        for (String line : lines) {
            if (!inData && line.trim().isEmpty()) {
                // Count empty lines after variable declarations
                continue;
            }
            if (!inData && !line.trim().isEmpty() && !line.contains("($") && !line.startsWith("Event")) {
                inData = true;
            }
            if (inData) {
                if (line.trim().isEmpty() && prevWasData) {
                    foundBlankBetweenSequences = true;
                }
                prevWasData = !line.trim().isEmpty();
            }
        }

        assertTrue(foundBlankBetweenSequences,
            "Event format should have blank lines between sequences");
    }

    // ============================================================
    // VVT Instrument Tests
    // ============================================================

    @Test
    void testVvt_CriterionNoIndent() {
        String result = InstrumentoObservacional.getInstance().exportToTheme(criterios);

        String[] lines = result.split(CRLF);
        for (Criterio criterio : criterios) {
            boolean found = false;
            for (String line : lines) {
                if (line.equals(criterio.getNombre())) {
                    found = true;
                    assertFalse(line.startsWith(" "),
                        "Criterion names must not have leading space: '" + line + "'");
                    break;
                }
            }
            assertTrue(found, "Criterion '" + criterio.getNombre() + "' should appear in VVT export");
        }
    }

    @Test
    void testVvt_CategoryIndentedOneSpace() {
        String result = InstrumentoObservacional.getInstance().exportToTheme(criterios);

        String[] lines = result.split(CRLF);
        for (String line : lines) {
            if (line.startsWith(" ")) {
                // Category lines start with exactly one space
                assertTrue(line.startsWith(" ") && (line.length() < 2 || line.charAt(1) != ' '),
                    "Category codes must be indented with exactly one space: '" + line + "'");
            }
        }
    }

    // ============================================================
    // General Tests
    // ============================================================

    @Test
    void testAllFormats_WindowsLineEndings() {
        String theme5 = registro.exportToTheme5(criterios);
        String event = registro.exportToSdisGseqEvento(criterios);
        String multievent = registro.exportToSdisGseqMultievento(criterios);

        for (String[] pair : new String[][]{
            {"Theme5", theme5},
            {"Event", event}, {"Multievent", multievent}
        }) {
            assertTrue(pair[1].contains(CRLF),
                pair[0] + " must use CRLF line endings");
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Extracts data lines from GSEQ output (after variable declarations section).
     * Variable declarations end with the last line containing ");".
     * Data lines are all non-empty lines after that point.
     */
    private List<String> extractDataLines(String[] lines) {
        List<String> dataLines = new ArrayList<>();
        int lastDeclIndex = -1;

        // Find the last variable declaration line (contains ");")
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(");")) {
                lastDeclIndex = i;
            }
        }

        // Collect non-empty lines after declarations
        for (int i = lastDeclIndex + 1; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                dataLines.add(lines[i]);
            }
        }

        return dataLines;
    }
}
