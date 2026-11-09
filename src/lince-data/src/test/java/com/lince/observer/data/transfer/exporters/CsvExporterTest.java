package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.TransferOption;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.TransferFixtures;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    private final CsvExporter exporter = new CsvExporter();

    @Test
    void descriptor() {
        ExportTypeDescriptor d = exporter.descriptor();
        assertEquals("csv", d.id());
        assertEquals("csv", d.extension());
        assertEquals("text/csv", d.mimeType());
        assertTrue(d.supportsCriteriaSelection());
        assertFalse(d.multiFile());
        assertEquals(3, d.options().size());
        TransferOption separator = d.options().get(0);
        assertEquals("separator", separator.key());
        assertEquals(TransferOption.Type.ENUM, separator.type());
        assertEquals("semicolon", separator.defaultValue());
        assertEquals(List.of("comma", "semicolon"), separator.values());
        TransferOption timeColumns = d.options().get(1);
        assertEquals("timeColumns", timeColumns.key());
        assertEquals("TFrames,DuracionFr,TSegundos,DuracionSeg,TMilisegundos,DuracionMiliseg", timeColumns.defaultValue());
        TransferOption excelSafe = d.options().get(2);
        assertEquals("excelSafe", excelSafe.key());
        assertEquals("Excel-safe cells", excelSafe.label());
        assertEquals(TransferOption.Type.BOOLEAN, excelSafe.type());
        assertEquals("false", excelSafe.defaultValue());
        assertFalse(excelSafe.nullable());
    }

    @Test
    void fieldsWithSeparatorQuoteOrLineBreakRoundTripThroughACsvParser() {
        String criteriaCode = "CRI,0;\"main\"\r\nnext";
        List<String> categoryCodes = List.of("a,b", "c;d", "say \"hi\"", "line\nbreak", "cr\rlf\r\n", "plain",
                "\"", ",;\"\n");
        for (String separator : List.of("comma", "semicolon")) {
            LinceFileProjectWrapper project = oneCriteriaProject(criteriaCode, categoryCodes);
            project.getRegister().get(0).getUserProfile().setUserName("Observer, \"one\"; \nsecond line");

            ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of()),
                    Map.of("separator", separator, "timeColumns", "TMilisegundos")));

            List<String[]> rows = parse(new String(file.content(), Charset.defaultCharset()),
                    "comma".equals(separator) ? ',' : ';');
            assertEquals(categoryCodes.size() + 2, rows.size(), separator);
            rows.forEach(row -> assertEquals(2, row.length, separator + ": " + String.join("|", row)));
            List<String> column = rows.stream().map(row -> row[1]).toList();
            List<String> expected = new ArrayList<>(List.of(criteriaCode, ""));
            expected.addAll(categoryCodes);
            assertEquals(expected, column, separator);
            assertEquals("TMilisegundos", rows.get(0)[0]);
        }
    }

    @Test
    void plainFieldsAreNotQuoted() {
        LinceFileProjectWrapper project = oneCriteriaProject("CRI", List.of("A-1", "B 2", "'quoted'"));

        String content = new String(single(exporter.export(new ExportSelection(project, null, List.of()),
                Map.of("separator", "comma", "timeColumns", ""))).content(), Charset.defaultCharset());

        assertEquals(List.of("CRI", "", "A-1", "B 2", "'quoted'"), content.lines().toList());
    }

    @Test
    void excelSafePrefixesFormulaLeadingCellsWithAQuote() {
        List<String> codes = List.of("=1+1", "+1", "-", "@x", "\tTAB", "\rCR", "=a,b", "safe", "a=b");
        LinceFileProjectWrapper project = oneCriteriaProject("=HEADER", codes);

        String safe = new String(single(exporter.export(new ExportSelection(project, null, List.of()),
                Map.of("separator", "comma", "timeColumns", "", "excelSafe", "TRUE"))).content(), Charset.defaultCharset());
        String raw = new String(single(exporter.export(new ExportSelection(project, null, List.of()),
                Map.of("separator", "comma", "timeColumns", "", "excelSafe", "false"))).content(), Charset.defaultCharset());
        String byDefault = new String(single(exporter.export(new ExportSelection(project, null, List.of()),
                Map.of("separator", "comma", "timeColumns", ""))).content(), Charset.defaultCharset());

        List<String> safeLines = safe.lines().toList();
        assertEquals("'=HEADER", safeLines.get(0));
        assertEquals("'=1+1", safeLines.get(2));
        assertEquals("'+1", safeLines.get(3));
        assertEquals("'-", safeLines.get(4));
        assertEquals("'@x", safeLines.get(5));
        assertEquals("'\tTAB", safeLines.get(6));
        assertEquals(List.of("'=HEADER", "", "'=1+1", "'+1", "'-", "'@x", "'\tTAB", "'\rCR", "'=a,b", "safe", "a=b"),
                parse(safe, ',').stream().map(CsvExporterTest::first).toList());
        assertTrue(safe.contains("\"'\rCR\""), "a CR cell is quoted after the prefix");
        assertTrue(safe.contains("\"'=a,b\""), "the prefix goes inside the quotes");

        assertEquals(raw, byDefault);
        List<String> rawColumn = new ArrayList<>(List.of("=HEADER", ""));
        rawColumn.addAll(codes);
        assertEquals(rawColumn, parse(raw, ',').stream().map(CsvExporterTest::first).toList());
        assertEquals("=1+1", raw.lines().toList().get(2));
    }

    @Test
    void excelSafeAcceptsOnlyBooleans() {
        LinceFileProjectWrapper project = oneCriteriaProject("CRI", List.of("A"));
        for (String value : List.of("yes", "1", "", "on")) {
            ExportException e = assertThrows(ExportException.class, () -> exporter.export(
                    new ExportSelection(project, null, List.of()), Map.of("excelSafe", value)), value);
            assertEquals(TransferException.Reason.INVALID_OPTION, e.getReason(), value);
            assertEquals(List.of("excelSafe"), e.getOffendingIds(), value);
        }
    }

    @Test
    void timeColumnsOptionSelectsAndOrdersTheTimeColumns() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        Criteria first = project.getObservationTool().get(0);

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of(first.getCode())),
                Map.of("separator", "comma", "timeColumns", "TSegundos, TFrames")));

        LinceCsvExporter csv = new LinceCsvExporter();
        csv.setUseCsvComma(true);
        assertArrayEquals(csv.executeFormatConversion(project, List.of("TSegundos", "TFrames", first.getCode())).getBytes(),
                file.content());

        ExportedFile noTime = single(exporter.export(new ExportSelection(project, null, List.of(first.getCode())),
                Map.of("timeColumns", "")));
        assertEquals(first.getCode(), new String(noTime.content(), Charset.defaultCharset()).lines().findFirst().orElseThrow());

        ExportException invalid = assertThrows(ExportException.class, () -> exporter.export(
                new ExportSelection(project, null, List.of(first.getCode())), Map.of("timeColumns", "TFrames,CRI0")));
        assertEquals(TransferException.Reason.INVALID_OPTION, invalid.getReason());
        assertEquals(List.of("timeColumns"), invalid.getOffendingIds());
    }

    @Test
    void allCriteriaWithCommaIsByteIdenticalToTheFxCommand() throws IOException {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of()), Map.of("separator", "comma")));

        assertTrue(file.name().endsWith(".csv"));
        assertArrayEquals(fxCommand(project, true, null, project.getObservationTool()), file.content());
        String expected = readResource("multipleObserverExample-export-25fps.csv");
        assertEquals(expected.trim(), new String(file.content(), Charset.defaultCharset()).trim());
    }

    @Test
    void defaultSeparatorIsSemicolon() throws IOException {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, null), Map.of()));

        assertArrayEquals(fxCommand(project, false, null, project.getObservationTool()), file.content());
        String expected = readResource("multipleObserverExample-export-25fps.csv")
                .replace(LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA, LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON);
        assertEquals(expected.trim(), new String(file.content(), Charset.defaultCharset()).trim());
    }

    @Test
    void criteriaSubsetKeepsTimeColumnsAndRequestOrder() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);
        List<Criteria> tool = project.getObservationTool();
        Criteria third = tool.get(2);
        Criteria first = tool.get(0);

        ExportedFile file = single(exporter.export(new ExportSelection(project, null,
                List.of(String.valueOf(third.getId()), first.getCode())), Map.of("separator", "SEMICOLON")));

        assertArrayEquals(fxCommand(project, false, null, List.of(third, first)), file.content());
        String header = new String(file.content(), Charset.defaultCharset()).lines().findFirst().orElseThrow();
        assertTrue(header.endsWith(";" + third.getCode() + ";" + first.getCode()), header);
    }

    @Test
    void researchIdSelectsTheRegister() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);
        UUID second = project.getRegister().get(1).getId();

        ExportedFile file = single(exporter.export(new ExportSelection(project, second, List.of()), Map.of()));

        assertArrayEquals(fxCommand(project, false, second, project.getObservationTool()), file.content());
        assertEquals(project.getRegister().get(1).getRegisterData().size() + 2,
                new String(file.content(), Charset.defaultCharset()).lines().count());
    }

    @Test
    void projectWithoutObservationsExportsHeaderOnly() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setRegister(new ArrayList<>());

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of()), Map.of()));

        List<String> lines = new String(file.content(), Charset.defaultCharset()).lines().toList();
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith(LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString()));
    }

    @Test
    void timeColumnsAloneAreAValidExport() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setObservationTool(new ArrayList<>());

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of()),
                Map.of("separator", "comma", "timeColumns", "TFrames")));

        List<String> lines = new String(file.content(), Charset.defaultCharset()).lines().toList();
        assertEquals("TFrames", lines.get(0));
        assertEquals(project.getRegister().get(0).getRegisterData().size() + 2, lines.size());
    }

    @Test
    void noColumnAtAllFails() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setObservationTool(new ArrayList<>());

        ExportException e = assertThrows(ExportException.class,
                () -> exporter.export(new ExportSelection(project, null, List.of()), Map.of("timeColumns", "")));

        assertEquals(TransferException.Reason.EMPTY_SELECTION, e.getReason());
    }

    @Test
    void mimeTypeNamesTheCharset() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);

        ExportedFile file = single(exporter.export(new ExportSelection(project, null, List.of()), Map.of()));

        assertEquals("text/csv; charset=" + Charset.defaultCharset().name(), file.mimeType());
    }

    @Test
    void unknownSeparatorIsRejected() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);

        String code = project.getObservationTool().get(0).getCode();
        ExportException e = assertThrows(ExportException.class,
                () -> exporter.export(new ExportSelection(project, null, List.of(code)), Map.of("separator", "tab")));
        assertEquals(TransferException.Reason.INVALID_OPTION, e.getReason());
        assertEquals(List.of("separator"), e.getOffendingIds());
    }

    /**
     * What the JavaFX {@code CsvExportComponent} exports: time columns of the selection panel followed by the selected
     * criteria codes, {@code String.getBytes()} with the platform charset. A non null {@code research} exports that
     * register only, the way the JavaFX menu hands {@link LinceCsvExporter} a one-register project.
     */
    private static byte[] fxCommand(LinceFileProjectWrapper project, boolean comma, UUID research, List<Criteria> criteria) {
        LinceCsvExporter csv = new LinceCsvExporter();
        csv.setUseCsvComma(comma);
        List<String> columns = Stream.concat(
                csv.getDefaultColumnDefinitions(project).stream().limit(LinceDataConstants.ColumnType.values().length),
                criteria.stream().map(Criteria::getCode)).toList();
        LinceFileProjectWrapper source = project;
        if (research != null) {
            source = new LinceFileProjectWrapper();
            source.setObservationTool(project.getObservationTool());
            source.setProfiles(project.getProfiles());
            source.setRegister(new ArrayList<>(project.getRegister().stream()
                    .filter(r -> research.equals(r.getId())).toList()));
        }
        return csv.executeFormatConversion(source, columns).getBytes();
    }

    /**
     * A project with one criteria and one register holding one row per category, in order.
     */
    private static LinceFileProjectWrapper oneCriteriaProject(String criteriaCode, List<String> categoryCodes) {
        Criteria criteria = new Criteria(1, "criteria");
        criteria.setCode(criteriaCode);
        LinkedList<Category> categories = new LinkedList<>();
        LinceRegisterWrapper register = new LinceRegisterWrapper();
        int id = 10;
        double time = 1;
        for (String code : categoryCodes) {
            Category category = new Category(++id, "name of " + code, 1);
            category.setCode(code);
            categories.add(category);
            register.getRegisterData().add(new RegisterItem(time++, category));
        }
        criteria.setInnerCategories(categories);
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(List.of(criteria)));
        project.setRegister(new ArrayList<>(List.of(register)));
        return project;
    }

    /**
     * Parses with univocity, keeping whitespace, line endings inside quotes and empty fields as they are.
     */
    private static List<String[]> parse(String content, char delimiter) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(delimiter);
        settings.getFormat().setLineSeparator(System.lineSeparator());
        settings.setNormalizeLineEndingsWithinQuotes(false);
        settings.setIgnoreLeadingWhitespaces(false);
        settings.setIgnoreTrailingWhitespaces(false);
        settings.setIgnoreLeadingWhitespacesInQuotes(false);
        settings.setIgnoreTrailingWhitespacesInQuotes(false);
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setSkipEmptyLines(false);
        return new CsvParser(settings).parseAll(new StringReader(content));
    }

    /**
     * @return the first field, empty for a blank line (which the parser reads as no field at all)
     */
    private static String first(String[] row) {
        return row.length == 0 || row[0] == null ? "" : row[0];
    }

    private static ExportedFile single(List<ExportedFile> files) {
        assertEquals(1, files.size());
        return files.get(0);
    }

    private static String readResource(String name) throws IOException {
        try (InputStream in = TransferFixtures.open(name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
