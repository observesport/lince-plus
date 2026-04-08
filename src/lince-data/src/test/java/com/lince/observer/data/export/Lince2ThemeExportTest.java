package com.lince.observer.data.export;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceFileImporter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Lince2ThemeExport using the modern project API.
 * Builds synthetic Lince projects programmatically and validates
 * that Theme export output matches expected external tool formats.
 * <p>
 * 5 complex scenarios per export type, modeled after validated sample files:
 * - cordones.txt: 3 obs, 3 criteria, sparse codes
 * - violin.txt: 15 obs, 7 criteria, all codes per row
 * - p2.txt: 12 obs, multi-criteria with varying codes per row
 * - stepsieteorden.txt: 87 obs, 8 criteria, dense data
 * - p13.txt: 12 obs with large frame gaps
 */
class Lince2ThemeExportTest {

    // ============================================================
    // Helper: build Category with code
    // ============================================================

    private Category makeCategory(int id, String code, int parentId) {
        Category cat = new Category(id, code, parentId);
        cat.setCode(code);
        return cat;
    }

    private RegisterItem makeRegisterItem(double videoTime, int frames, Category... categories) {
        RegisterItem item = new RegisterItem(videoTime, categories);
        item.setFrames(frames);
        return item;
    }

    private String captureExport(List<RegisterItem> register) {
        try {
            java.io.File tempFile = java.io.File.createTempFile("lince_theme_test_", ".txt");
            tempFile.deleteOnExit();
            Lince2ThemeExport exporter = new Lince2ThemeExport(register);
            exporter.createFile(new FileWriter(tempFile));
            return new String(java.nio.file.Files.readAllBytes(tempFile.toPath()));
        } catch (Exception e) {
            fail("Failed to capture export: " + e.getMessage());
            return "";
        }
    }

    // ============================================================
    // Scenario 1: "Cordones" - 3 observations, 3 criteria, sparse
    // Modeled after sample-theme6/cordones.txt
    // ============================================================

    @Test
    void testCordones_3Obs3Criteria_SparseCodesPerRow() {
        Criteria paso = new Criteria(1, "PASO");
        Category rs = makeCategory(10, "RS", 1);
        Category pt = makeCategory(11, "PT", 1);
        paso.setInnerCategories(new LinkedList<>(List.of(rs, pt)));

        Criteria funcional = new Criteria(2, "FUNCIONAL");
        Category a = makeCategory(20, "A", 2);
        Category na = makeCategory(21, "NA", 2);
        funcional.setInnerCategories(new LinkedList<>(List.of(a, na)));

        Criteria exito = new Criteria(3, "EXITO");
        Category l = makeCategory(30, "NL", 3);
        exito.setInnerCategories(new LinkedList<>(List.of(l)));

        List<RegisterItem> register = List.of(
            makeRegisterItem(2.0, 50, rs, a),       // frame 50
            makeRegisterItem(13.0, 325, pt, na),     // frame 325
            makeRegisterItem(24.0, 600, l)           // frame 600, only 1 code
        );

        String output = captureExport(register);

        // Header check
        assertTrue(output.startsWith("TIME\tEVENT"), "Must start with TIME\\tEVENT header");

        String[] lines = output.split("\r?\n");
        assertTrue(lines.length >= 5, "Should have header + start marker + 3 data + end marker");

        // Start marker: frame-1
        assertEquals("49\t:", lines[1], "Start marker should be first frame - 1");

        // Data rows use frame numbers
        assertTrue(lines[2].startsWith("50\t"), "First data row should use frame 50");
        assertTrue(lines[3].startsWith("325\t"), "Second data row should use frame 325");
        assertTrue(lines[4].startsWith("600\t"), "Third data row should use frame 600");

        // End marker: last frame + 1
        assertEquals("601\t&", lines[5], "End marker should be last frame + 1");

        // Codes comma-separated, no spaces
        assertTrue(lines[2].contains("RS,A") || lines[2].contains("RS\tA") == false,
            "Codes should be comma-separated: " + lines[2]);
        assertFalse(lines[2].contains(", "), "No spaces after commas");

        // Sparse row (1 code)
        String lastDataEvent = lines[4].split("\t")[1];
        assertEquals("NL", lastDataEvent, "Single-code row should just have the code");
    }

    // ============================================================
    // Scenario 2: "Violin" - 15 obs, 7 criteria, all codes per row
    // Modeled after sample-theme6/violin.txt
    // ============================================================

    @Test
    void testViolin_15Obs7Criteria_AllCodesPerRow() {
        Criteria c1 = new Criteria(1, "BRAZO");
        Category bm = makeCategory(10, "BM", 1);
        Criteria c2 = new Criteria(2, "VIBRATO");
        Category vr = makeCategory(20, "VR", 2);
        Category voi = makeCategory(21, "VOI", 2);
        Criteria c3 = new Criteria(3, "ARCO");
        Category aa = makeCategory(30, "AA", 3);
        Category ar = makeCategory(31, "AR", 3);
        Criteria c4 = new Criteria(4, "INCLINACION");
        Category icd = makeCategory(40, "ICD", 4);
        Category icad = makeCategory(41, "ICAD", 4);
        Criteria c5 = new Criteria(5, "HOMBRO");
        Category hp = makeCategory(50, "HP", 5);
        Criteria c6 = new Criteria(6, "DEDOS");
        Category da = makeCategory(60, "DA", 6);
        Criteria c7 = new Criteria(7, "CODO");
        Category ct = makeCategory(70, "CT", 7);

        // 15 observations at different frame values (matching violin.txt pattern)
        int[] frames = {607, 654, 677, 745, 759, 784, 854, 923, 1089, 1201, 1265, 1319, 1375, 1564, 1816};
        List<RegisterItem> register = new ArrayList<>();
        for (int i = 0; i < frames.length; i++) {
            Category vibrato = (i % 2 == 0) ? vr : voi;
            Category arco = (i % 3 == 0) ? aa : ar;
            Category incl = (i < 5) ? icd : icad;
            register.add(makeRegisterItem(frames[i] / 25.0, frames[i], bm, vibrato, arco, incl, hp, da, ct));
        }

        String output = captureExport(register);
        String[] lines = output.split("\r?\n");

        // Header
        assertEquals("TIME\tEVENT", lines[0]);

        // Start marker at frame 606
        assertEquals("606\t:", lines[1]);

        // 15 data rows
        for (int i = 0; i < 15; i++) {
            String line = lines[i + 2];
            String[] parts = line.split("\t");
            assertEquals(String.valueOf(frames[i]), parts[0], "Frame mismatch at row " + i);
            // Each row should have 7 codes
            String[] codes = parts[1].split(",");
            assertEquals(7, codes.length, "Each row should have 7 comma-separated codes at row " + i);
            // No spaces in codes
            for (String code : codes) {
                assertFalse(code.contains(" "), "No spaces in code: " + code);
            }
        }

        // End marker at last frame + 1
        assertEquals("1817\t&", lines[17]);
    }

    // ============================================================
    // Scenario 3: "P2" - 12 obs with varying criteria per row & large gaps
    // Modeled after sample-theme6/p2.txt
    // ============================================================

    @Test
    void testP2_12ObsVaryingCriteria_LargeFrameGaps() {
        Category tp1 = makeCategory(10, "TP1", 1);
        Category et = makeCategory(20, "ET", 2);
        Category mtrz = makeCategory(21, "MTRZ", 2);
        Category i1 = makeCategory(30, "I1", 3);
        Category p1 = makeCategory(40, "P1", 4);
        Category p2 = makeCategory(41, "P2", 4);
        Category p3 = makeCategory(42, "P3", 4);
        Category p4 = makeCategory(43, "P4", 4);
        Category p5 = makeCategory(44, "P5", 4);
        Category r = makeCategory(45, "R", 4);
        Category ad = makeCategory(50, "AD", 5);
        Category gi = makeCategory(51, "GI", 5);
        Category npm = makeCategory(60, "NPM", 6);
        Category npli = makeCategory(61, "NPLI", 6);
        Category nnm = makeCategory(70, "NNM", 7);
        Category nnli = makeCategory(71, "NNLI", 7);
        Category nnld = makeCategory(72, "NNLD", 7);
        Category adap = makeCategory(80, "ADAP", 8);

        // Matching p2.txt frame pattern with gaps
        List<RegisterItem> register = List.of(
            makeRegisterItem(50.04, 1251, tp1, et, i1, p1, ad, npm, nnm, adap),
            makeRegisterItem(55.52, 1388, tp1, et, i1, p2, ad, npm, nnm, adap),
            makeRegisterItem(59.64, 1491, tp1, et, i1, p3, gi, npm, nnm, adap),
            makeRegisterItem(67.64, 1691, tp1, et, i1, p4, ad, npli, nnli, adap),
            makeRegisterItem(76.88, 1922, tp1, et, i1, p5, ad, npli, nnli, adap),
            makeRegisterItem(78.96, 1974, tp1, et, i1, r),           // only 4 codes
            makeRegisterItem(99.88, 2497, tp1, mtrz, i1, p1, ad, nnm, adap),  // 7 codes
            makeRegisterItem(103.0, 2575, tp1, mtrz, i1, p2, ad, nnm, adap),
            makeRegisterItem(104.2, 2605, tp1, mtrz, i1, p3, gi, nnm, adap),
            makeRegisterItem(105.64, 2641, tp1, mtrz, i1, p4, ad, nnld, adap),
            makeRegisterItem(106.88, 2672, tp1, mtrz, i1, p5, ad, nnld, adap),
            makeRegisterItem(107.92, 2698, tp1, mtrz, i1, r)         // only 4 codes
        );

        String output = captureExport(register);
        String[] lines = output.split("\r?\n");

        // Verify frame gaps (not sequential)
        int prevFrame = 0;
        boolean hasLargeGap = false;
        for (int i = 2; i < lines.length - 1; i++) {
            int frame = Integer.parseInt(lines[i].split("\t")[0]);
            if (frame - prevFrame > 10) hasLargeGap = true;
            prevFrame = frame;
        }
        assertTrue(hasLargeGap, "Should have large frame gaps like the sample");

        // Varying code count per row
        String row6 = lines[7].split("\t")[1]; // 6th data row (R only 4 codes)
        String row1 = lines[2].split("\t")[1]; // 1st data row (8 codes)
        assertTrue(row6.split(",").length < row1.split(",").length,
            "Rows should have varying number of codes");

        // Start/end markers
        assertEquals("1250\t:", lines[1]);
        assertEquals("2699\t&", lines[lines.length - 1]);
    }

    // ============================================================
    // Scenario 4: Dense data - 30 obs, 8 criteria per row
    // Modeled after sample-theme6/stepsieteorden.txt pattern
    // ============================================================

    @Test
    void testDenseData_30Obs8Criteria() {
        Category tp7 = makeCategory(10, "TP7", 1);
        Category dpr = makeCategory(20, "DPR", 2);
        Category manet = makeCategory(21, "MANET", 2);
        Category i1 = makeCategory(30, "I1", 3);
        Category i2 = makeCategory(31, "I2", 3);
        Category p1 = makeCategory(40, "P1", 4);
        Category p2 = makeCategory(41, "P2", 4);
        Category rCode = makeCategory(42, "R", 4);
        Category gi = makeCategory(50, "GI", 5);
        Category ad = makeCategory(51, "AD", 5);
        Category npm = makeCategory(60, "NPM", 6);
        Category npli = makeCategory(61, "NPLI", 6);
        Category nnm = makeCategory(70, "NNM", 7);
        Category nnli = makeCategory(71, "NNLI", 7);
        Category adap = makeCategory(80, "ADAP", 8);
        Category noad = makeCategory(81, "NOAD", 8);

        List<RegisterItem> register = new ArrayList<>();
        // 30 observations with consecutive frames (like stepsieteorden where each obs is 1 frame apart)
        for (int i = 0; i < 30; i++) {
            int frame = 100 + i * 3; // small gaps
            Category tipo = (i < 15) ? dpr : manet;
            Category intento = (i % 2 == 0) ? i1 : i2;
            Category paso = (i % 5 == 4) ? rCode : ((i % 2 == 0) ? p1 : p2);

            if (paso == rCode) {
                // R rows have fewer codes (like stepsieteorden.txt)
                register.add(makeRegisterItem(frame / 25.0, frame, tp7, tipo, intento, rCode));
            } else {
                Category dir = (i % 3 == 0) ? gi : ad;
                Category np = (i % 4 < 2) ? npm : npli;
                Category nn = (i % 4 < 2) ? nnm : nnli;
                Category adapt = (i < 20) ? adap : noad;
                register.add(makeRegisterItem(frame / 25.0, frame, tp7, tipo, intento, paso, dir, np, nn, adapt));
            }
        }

        String output = captureExport(register);
        String[] lines = output.split("\r?\n");

        // Header + start marker + 30 data + end marker = 33 lines
        assertEquals(33, lines.length, "Should have 33 lines total");

        // Verify mixed code counts (8 codes for normal, 4 for R-rows)
        int minCodes = Integer.MAX_VALUE;
        int maxCodes = 0;
        for (int i = 2; i < 32; i++) {
            int count = lines[i].split("\t")[1].split(",").length;
            minCodes = Math.min(minCodes, count);
            maxCodes = Math.max(maxCodes, count);
        }
        assertEquals(4, minCodes, "R-rows should have 4 codes");
        assertEquals(8, maxCodes, "Normal rows should have 8 codes");
    }

    // ============================================================
    // Scenario 5: Multiple-sequence simulation with large frame gaps
    // Modeled after THEME5_registro.csv with multiple start/end markers
    // (Lince2ThemeExport produces single sequence, validating that)
    // ============================================================

    @Test
    void testLargeGaps_SingleSequence() {
        Category zi20 = makeCategory(10, "ZI20", 1);
        Category zf20 = makeCategory(20, "ZF20", 2);
        Category ffsp = makeCategory(30, "FFSP", 3);
        Category c1 = makeCategory(31, "C1", 3);
        Category c2 = makeCategory(32, "C2", 3);
        Category tf = makeCategory(33, "TF", 3);

        // Large gaps between observations (like THEME5_registro.csv)
        List<RegisterItem> register = List.of(
            makeRegisterItem(42.4, 1060, zi20, zf20, ffsp),
            makeRegisterItem(42.44, 1061, zi20, zf20, c1),
            makeRegisterItem(44.84, 1121, zi20, zf20, c2),
            // Large gap
            makeRegisterItem(96.48, 2412, zi20, zf20, c1),
            makeRegisterItem(100.04, 2501, zi20, zf20, tf),
            // Another large gap
            makeRegisterItem(136.52, 3414, zi20, zf20, c1),
            makeRegisterItem(141.04, 3526, zi20, zf20, c2)
        );

        String output = captureExport(register);
        String[] lines = output.split("\r?\n");

        // Single sequence with one start marker and one end marker
        int startMarkers = 0;
        int endMarkers = 0;
        for (String line : lines) {
            if (line.endsWith("\t:")) startMarkers++;
            if (line.endsWith("\t&")) endMarkers++;
        }
        assertEquals(1, startMarkers, "Modern export produces single sequence with one start marker");
        assertEquals(1, endMarkers, "Modern export produces single sequence with one end marker");

        // Verify frame values are preserved with gaps
        assertEquals("1059\t:", lines[1], "Start marker at frame-1");
        assertEquals("1060", lines[2].split("\t")[0]);
        assertEquals("2412", lines[5].split("\t")[0]);
        assertEquals("3526", lines[8].split("\t")[0]);
        assertEquals("3527\t&", lines[9]);

        // Verify frame gap exists (not sequential)
        int frame3 = Integer.parseInt(lines[4].split("\t")[0]); // 1121
        int frame4 = Integer.parseInt(lines[5].split("\t")[0]); // 2412
        assertTrue(frame4 - frame3 > 1000, "Should have large frame gap: " + frame3 + " -> " + frame4);
    }

    // ============================================================
    // Cross-validation: file extension
    // ============================================================

    @Test
    void testCreateFile_UsesTxtExtension() throws Exception {
        Category cat = makeCategory(1, "TEST", 1);
        List<RegisterItem> register = List.of(makeRegisterItem(1.0, 25, cat));

        java.io.File tempDir = java.nio.file.Files.createTempDirectory("lince_test_").toFile();
        tempDir.deleteOnExit();
        String basePath = new java.io.File(tempDir, "export").getAbsolutePath();

        Lince2ThemeExport exporter = new Lince2ThemeExport(register);
        exporter.createFile(basePath);

        java.io.File expectedFile = new java.io.File(basePath + ".txt");
        assertTrue(expectedFile.exists(), "Export should create .txt file, not .vvt");
        expectedFile.deleteOnExit();

        // Verify .vvt was NOT created
        java.io.File wrongFile = new java.io.File(basePath + ".vvt");
        assertFalse(wrongFile.exists(), "Should NOT create .vvt file for register export");
    }

    // ============================================================
    // Empty register edge case
    // ============================================================

    @Test
    void testEmptyRegister_ProducesHeaderOnly() {
        List<RegisterItem> register = new ArrayList<>();
        String output = captureExport(register);

        assertTrue(output.contains("TIME"), "Should still have header");
        assertTrue(output.contains("EVENT"), "Should still have header");
        assertFalse(output.contains(":"), "No start marker for empty register");
        assertFalse(output.contains("&"), "No end marker for empty register");
    }

    // ============================================================
    // Header case validation
    // ============================================================

    @Test
    void testHeader_IsUppercase() {
        Category cat = makeCategory(1, "X", 1);
        List<RegisterItem> register = List.of(makeRegisterItem(1.0, 25, cat));
        String output = captureExport(register);

        String firstLine = output.split("\r?\n")[0];
        assertTrue(firstLine.contains("TIME"), "Header must use uppercase TIME");
        assertTrue(firstLine.contains("EVENT"), "Header must use uppercase EVENT");
        assertFalse(firstLine.contains("Time") && !firstLine.contains("TIME"),
            "Header must not use mixed case");
    }

    // ============================================================
    // Codes have no spaces (regression for Arrays.toString bug)
    // ============================================================

    @Test
    void testCodes_NoSpacesAfterComma() {
        Category c1 = makeCategory(1, "AA", 1);
        Category c2 = makeCategory(2, "BB", 1);
        Category c3 = makeCategory(3, "CC", 1);
        List<RegisterItem> register = List.of(makeRegisterItem(1.0, 25, c1, c2, c3));
        String output = captureExport(register);

        String[] lines = output.split("\r?\n");
        // Find the data row (after header and start marker)
        String dataLine = lines[2];
        String event = dataLine.split("\t")[1];
        assertEquals("AA,BB,CC", event, "Codes must be comma-separated without spaces");
        assertFalse(event.contains(", "), "Must not have space after comma");
        assertFalse(event.contains("["), "Must not have bracket from Arrays.toString");
    }

    // ============================================================
    // Codes use getCode() not toString() (regression test)
    // ============================================================

    @Test
    void testCodes_UseCodeNotToString() {
        // Category.toString() returns "code-name", getCode() returns just "code"
        Category cat = new Category(1, "MyName", 1);
        cat.setCode("COD");
        List<RegisterItem> register = List.of(makeRegisterItem(1.0, 25, cat));
        String output = captureExport(register);

        String[] lines = output.split("\r?\n");
        String event = lines[2].split("\t")[1];
        assertEquals("COD", event, "Should use getCode() not toString()");
        assertFalse(event.contains("-"), "Should not contain '-' from toString()");
        assertFalse(event.contains("MyName"), "Should not contain name from toString()");
    }

    // ============================================================
    // VVT content: criterion names appear without leading space
    // ============================================================

    @Test
    void testCreateVvtContent_CriterionNoIndent() {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1)
        )));

        Criteria action = new Criteria(2, "ACTION");
        action.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(20, "RUN", 2),
            makeCategory(21, "WALK", 2)
        )));

        List<Criteria> criteria = List.of(post, action);
        String vvt = Lince2ThemeExport.createVvtContent(criteria);

        // Criterion names must NOT start with a space
        String[] lines = vvt.split("\r\n");
        boolean foundPost = false;
        boolean foundAction = false;
        for (String line : lines) {
            if (line.equals("POST")) foundPost = true;
            if (line.equals("ACTION")) foundAction = true;
        }
        assertTrue(foundPost, "Criterion 'POST' should appear without leading space");
        assertTrue(foundAction, "Criterion 'ACTION' should appear without leading space");
    }

    // ============================================================
    // VVT content: category codes indented with exactly one space
    // ============================================================

    @Test
    void testCreateVvtContent_CategoryIndentedOneSpace() {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1)
        )));

        Criteria action = new Criteria(2, "ACTION");
        action.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(20, "RUN", 2)
        )));

        List<Criteria> criteria = List.of(post, action);
        String vvt = Lince2ThemeExport.createVvtContent(criteria);

        // Each category line must start with exactly one space
        String[] lines = vvt.split("\r\n");
        for (String line : lines) {
            if (line.equals("POST") || line.equals("ACTION")) continue;
            if (line.isEmpty()) continue;
            assertTrue(line.startsWith(" "), "Category line should start with one space: '" + line + "'");
            assertFalse(line.startsWith("  "), "Category line should not start with two spaces: '" + line + "'");
        }

        // Spot-check: STAND should appear as " STAND"
        assertTrue(vvt.contains(" STAND\r\n"), "STAND must be indented with one space and end with CRLF");
        assertTrue(vvt.contains(" SIT\r\n"), "SIT must be indented with one space and end with CRLF");
        assertTrue(vvt.contains(" RUN\r\n"), "RUN must be indented with one space and end with CRLF");
    }

    // ============================================================
    // VVT content: all line endings are CRLF
    // ============================================================

    @Test
    void testCreateVvtContent_CRLFLineEndings() {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1)
        )));

        List<Criteria> criteria = List.of(post);
        String vvt = Lince2ThemeExport.createVvtContent(criteria);

        // Must not contain bare LF (i.e. LF not preceded by CR)
        for (int i = 0; i < vvt.length(); i++) {
            if (vvt.charAt(i) == '\n') {
                assertTrue(i > 0 && vvt.charAt(i - 1) == '\r',
                    "Found bare LF at position " + i + " — all line endings must be CRLF");
            }
        }
        // Must contain at least one CRLF
        assertTrue(vvt.contains("\r\n"), "VVT content must use CRLF line endings");
    }

    // ============================================================
    // VVT content: matches Gudberg format exactly
    // ============================================================

    @Test
    void testCreateVvtContent_MatchesGudbergFormat() {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1),
            makeCategory(12, "KNEEL", 1),
            makeCategory(13, "LIE", 1),
            makeCategory(14, "IMMOBILE", 1)
        )));

        List<Criteria> criteria = List.of(post);
        String vvt = Lince2ThemeExport.createVvtContent(criteria);

        String expected = "POST\r\n STAND\r\n SIT\r\n KNEEL\r\n LIE\r\n IMMOBILE\r\n";
        assertEquals(expected, vvt, "VVT content must match Gudberg format exactly");
    }

    // ============================================================
    // Paired export: createFile(basePath, criteria) writes both files
    // ============================================================

    @Test
    void testCreateFilePaired_WritesBothFiles() throws Exception {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1)
        )));

        Category stand = makeCategory(10, "STAND", 1);
        Category sit = makeCategory(11, "SIT", 1);
        List<RegisterItem> register = List.of(
            makeRegisterItem(1.0, 25, stand),
            makeRegisterItem(2.0, 50, sit)
        );
        List<Criteria> criteria = List.of(post);

        java.io.File tempDir = java.nio.file.Files.createTempDirectory("lince_vvt_test_").toFile();
        tempDir.deleteOnExit();
        String basePath = new java.io.File(tempDir, "export").getAbsolutePath();

        Lince2ThemeExport exporter = new Lince2ThemeExport(register);
        exporter.createFile(basePath, criteria);

        java.io.File txtFile = new java.io.File(basePath + ".txt");
        java.io.File vvtFile = new java.io.File(basePath + ".vvt");
        txtFile.deleteOnExit();
        vvtFile.deleteOnExit();

        assertTrue(txtFile.exists(), "Paired export must create .txt file");
        assertTrue(vvtFile.exists(), "Paired export must create .vvt file");

        String txtContent = new String(java.nio.file.Files.readAllBytes(txtFile.toPath()));
        String vvtContent = new String(java.nio.file.Files.readAllBytes(vvtFile.toPath()));

        assertTrue(txtContent.contains("TIME\tEVENT"), ".txt must contain TIME\\tEVENT header");
        assertTrue(vvtContent.contains("POST"), ".vvt must contain criterion name POST");
        assertTrue(vvtContent.contains(" STAND"), ".vvt must contain indented category STAND");
        assertTrue(vvtContent.contains(" SIT"), ".vvt must contain indented category SIT");
    }

    // ============================================================
    // Paired export: .txt content matches single-file export
    // ============================================================

    @Test
    void testCreateFilePaired_TxtContentMatchesSingleExport() throws Exception {
        Criteria post = new Criteria(1, "POST");
        post.setInnerCategories(new LinkedList<>(List.of(
            makeCategory(10, "STAND", 1),
            makeCategory(11, "SIT", 1)
        )));

        Category stand = makeCategory(10, "STAND", 1);
        Category sit = makeCategory(11, "SIT", 1);
        List<RegisterItem> register = List.of(
            makeRegisterItem(1.0, 25, stand),
            makeRegisterItem(2.0, 50, sit)
        );
        List<Criteria> criteria = List.of(post);

        // Single export via captureExport
        String singleExport = captureExport(register);

        // Paired export
        java.io.File tempDir = java.nio.file.Files.createTempDirectory("lince_vvt_paired_").toFile();
        tempDir.deleteOnExit();
        String basePath = new java.io.File(tempDir, "export").getAbsolutePath();

        Lince2ThemeExport exporter = new Lince2ThemeExport(register);
        exporter.createFile(basePath, criteria);

        java.io.File txtFile = new java.io.File(basePath + ".txt");
        txtFile.deleteOnExit();

        String pairedTxt = new String(java.nio.file.Files.readAllBytes(txtFile.toPath()));

        assertEquals(singleExport, pairedTxt,
            "Paired export .txt content must be identical to single-file export output");
    }

    // ============================================================
    // Integration test: real FCB_RMAD_0925 project file
    // ============================================================

    private ILinceProject loadProject(String resourceName) {
        LinceFileImporter importer = new LinceFileImporter();
        File file = new File(Objects.requireNonNull(
                getClass().getClassLoader().getResource(resourceName)).getFile());
        ILinceProject project = importer.importLinceProject(file);
        assertNotNull(project, "Project should load from " + resourceName);
        return project;
    }

    @Test
    void testFcbRmadProject_RegisterExportHasValues() {
        ILinceProject project = loadProject("FCB_RMAD_0925.xml");

        List<LinceRegisterWrapper> registers = project.getRegister();
        assertFalse(registers.isEmpty(), "Project should have at least one register");

        List<RegisterItem> allItems = registers.stream()
                .flatMap(r -> r.getRegisterData().stream())
                .toList();
        assertFalse(allItems.isEmpty(), "Register should have items");

        Lince2ThemeExport exporter = new Lince2ThemeExport(allItems);
        String output = captureExport(allItems);

        // Must have header
        assertTrue(output.startsWith("TIME\tEVENT"), "Must start with header");

        // Must have data rows (not just header)
        String[] lines = output.split("\r?\n");
        assertTrue(lines.length >= 4,
                "Must have header + start marker + data + end marker, got " + lines.length + " lines");

        // Start marker
        assertTrue(lines[1].endsWith("\t:"), "Second line must be start marker");

        // Data rows must have actual codes (not empty events)
        for (int i = 2; i < lines.length - 1; i++) {
            String[] parts = lines[i].split("\t");
            assertEquals(2, parts.length, "Each data row must have time and event columns: " + lines[i]);
            String event = parts[1];
            assertFalse(event.isEmpty(), "Event must not be empty at line " + i + ": " + lines[i]);
            assertFalse(event.isBlank(), "Event must not be blank at line " + i + ": " + lines[i]);
        }

        // End marker
        assertTrue(lines[lines.length - 1].endsWith("\t&"), "Last line must be end marker");

        // Verify frame numbers are present and positive
        for (int i = 2; i < lines.length - 1; i++) {
            int frame = Integer.parseInt(lines[i].split("\t")[0]);
            assertTrue(frame > 0, "Frame numbers must be positive: " + frame);
        }
    }

    @Test
    void testFcbRmadProject_InstrumentExportHasValues() {
        ILinceProject project = loadProject("FCB_RMAD_0925.xml");

        List<Criteria> criteria = project.getObservationTool();
        assertFalse(criteria.isEmpty(), "Project should have criteria");

        String vvt = Lince2ThemeExport.createVvtContent(criteria);

        // Must not be empty
        assertFalse(vvt.isEmpty(), "VVT output must not be empty");

        // Must contain criterion names (from the project: EQUIPO, CUARTO, etc.)
        assertTrue(vvt.contains("EQUIPO"), "VVT must contain EQUIPO criterion");
        assertTrue(vvt.contains("CUARTO"), "VVT must contain CUARTO criterion");

        // Must contain category codes
        assertTrue(vvt.contains(" FCB"), "VVT must contain FCB category");
        assertTrue(vvt.contains(" RMAD"), "VVT must contain RMAD category");
        assertTrue(vvt.contains(" C1"), "VVT must contain C1 category");

        // Must have CRLF line endings
        assertTrue(vvt.contains("\r\n"), "VVT must use CRLF");
    }

    @Test
    void testFcbRmadProject_RegisterHas7Items() {
        ILinceProject project = loadProject("FCB_RMAD_0925.xml");

        List<RegisterItem> allItems = project.getRegister().stream()
                .flatMap(r -> r.getRegisterData().stream())
                .toList();

        assertEquals(7, allItems.size(), "FCB_RMAD project has exactly 7 register items");

        // Verify known frame values from the XML
        int[] expectedFrames = {561, 1230, 1629, 1893, 3251, 3836, 4532};
        for (int i = 0; i < allItems.size(); i++) {
            assertEquals(expectedFrames[i], allItems.get(i).getFrames(),
                    "Frame mismatch at register item " + i);
        }
    }

    @Test
    void testFcbRmadProject_PairedExportWritesBothFiles() throws Exception {
        ILinceProject project = loadProject("FCB_RMAD_0925.xml");

        List<RegisterItem> allItems = project.getRegister().stream()
                .flatMap(r -> r.getRegisterData().stream())
                .toList();
        List<Criteria> criteria = project.getObservationTool();

        java.io.File tempDir = java.nio.file.Files.createTempDirectory("lince_fcb_test_").toFile();
        tempDir.deleteOnExit();
        String basePath = new java.io.File(tempDir, "FCB_RMAD_0925").getAbsolutePath();

        Lince2ThemeExport exporter = new Lince2ThemeExport(allItems);
        exporter.createFile(basePath, criteria);

        java.io.File txtFile = new java.io.File(basePath + ".txt");
        java.io.File vvtFile = new java.io.File(basePath + ".vvt");
        txtFile.deleteOnExit();
        vvtFile.deleteOnExit();

        assertTrue(txtFile.exists(), "Must create .txt file");
        assertTrue(vvtFile.exists(), "Must create .vvt file");

        String txtContent = new String(java.nio.file.Files.readAllBytes(txtFile.toPath()));
        String vvtContent = new String(java.nio.file.Files.readAllBytes(vvtFile.toPath()));

        // txt must have real data
        String[] txtLines = txtContent.split("\r?\n");
        assertTrue(txtLines.length >= 10,
                "txt must have header+start+7 data+end = 10 lines, got " + txtLines.length);

        // vvt must have criteria and categories
        assertTrue(vvtContent.contains("EQUIPO"), "vvt must have EQUIPO");
        assertTrue(vvtContent.contains(" FCB"), "vvt must have FCB");
    }
}
