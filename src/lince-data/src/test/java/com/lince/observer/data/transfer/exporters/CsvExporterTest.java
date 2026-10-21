package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.TransferOption;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.TransferFixtures;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        assertEquals(2, d.options().size());
        TransferOption separator = d.options().get(0);
        assertEquals("separator", separator.key());
        assertEquals(TransferOption.Type.ENUM, separator.type());
        assertEquals("semicolon", separator.defaultValue());
        assertEquals(List.of("comma", "semicolon"), separator.values());
        TransferOption timeColumns = d.options().get(1);
        assertEquals("timeColumns", timeColumns.key());
        assertEquals("TFrames,DuracionFr,TSegundos,DuracionSeg,TMilisegundos,DuracionMiliseg", timeColumns.defaultValue());
    }

    @Test
    void timeColumnsOptionSelectsAndOrdersTheTimeColumns() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        Criteria first = project.getObservationTool().get(0);

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, List.of(first.getCode())),
                Map.of("separator", "comma", "timeColumns", "TSegundos, TFrames")));

        LinceCsvExporter csv = new LinceCsvExporter();
        csv.setUseCsvComma(true);
        assertArrayEquals(csv.executeFormatConversion(project, List.of("TSegundos", "TFrames", first.getCode())).getBytes(),
                file.content());

        ExportedFile noTime = single(exporter.export(ExportSelection.of(project, null, List.of(first.getCode())),
                Map.of("timeColumns", "")));
        assertEquals(first.getCode(), new String(noTime.content(), Charset.defaultCharset()).lines().findFirst().orElseThrow());

        ExportException invalid = assertThrows(ExportException.class, () -> exporter.export(
                ExportSelection.of(project, null, List.of(first.getCode())), Map.of("timeColumns", "TFrames,CRI0")));
        assertEquals(TransferException.Reason.INVALID_OPTION, invalid.getReason());
        assertEquals(List.of("timeColumns"), invalid.getOffendingIds());
    }

    @Test
    void allCriteriaWithCommaIsByteIdenticalToTheFxCommand() throws IOException {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, List.of()), Map.of("separator", "comma")));

        assertTrue(file.name().endsWith(".csv"));
        assertArrayEquals(fxCommand(project, true, null, project.getObservationTool()), file.content());
        String expected = readResource("multipleObserverExample-export-25fps.csv");
        assertEquals(expected.trim(), new String(file.content(), Charset.defaultCharset()).trim());
    }

    @Test
    void defaultSeparatorIsSemicolon() throws IOException {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, null), Map.of()));

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

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null,
                List.of(String.valueOf(third.getId()), first.getCode())), Map.of("separator", "SEMICOLON")));

        assertArrayEquals(fxCommand(project, false, null, List.of(third, first)), file.content());
        String header = new String(file.content(), Charset.defaultCharset()).lines().findFirst().orElseThrow();
        assertTrue(header.endsWith(";" + third.getCode() + ";" + first.getCode()), header);
    }

    @Test
    void researchIdSelectsTheRegister() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);
        UUID second = project.getRegister().get(1).getId();

        ExportedFile file = single(exporter.export(ExportSelection.of(project, second, List.of()), Map.of()));

        assertArrayEquals(fxCommand(project, false, second, project.getObservationTool()), file.content());
        assertEquals(project.getRegister().get(1).getRegisterData().size() + 2,
                new String(file.content(), Charset.defaultCharset()).lines().count());
    }

    @Test
    void projectWithoutObservationsExportsHeaderOnly() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setRegister(new ArrayList<>());

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, List.of()), Map.of()));

        List<String> lines = new String(file.content(), Charset.defaultCharset()).lines().toList();
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith(LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString()));
    }

    @Test
    void timeColumnsAloneAreAValidExport() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setObservationTool(new ArrayList<>());

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, List.of()),
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
                () -> exporter.export(ExportSelection.of(project, null, List.of()), Map.of("timeColumns", "")));

        assertEquals(TransferException.Reason.EMPTY_SELECTION, e.getReason());
    }

    @Test
    void mimeTypeNamesTheCharset() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);

        ExportedFile file = single(exporter.export(ExportSelection.of(project, null, List.of()), Map.of()));

        assertEquals("text/csv; charset=" + Charset.defaultCharset().name(), file.mimeType());
    }

    @Test
    void unknownSeparatorIsRejected() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);

        String code = project.getObservationTool().get(0).getCode();
        ExportException e = assertThrows(ExportException.class,
                () -> exporter.export(ExportSelection.of(project, null, List.of(code)), Map.of("separator", "tab")));
        assertEquals(TransferException.Reason.INVALID_OPTION, e.getReason());
        assertEquals(List.of("separator"), e.getOffendingIds());
    }

    /**
     * What the JavaFX {@code CsvExportComponent} exports: time columns of the selection panel followed by the selected
     * criteria codes, {@code String.getBytes()} with the platform charset.
     */
    private static byte[] fxCommand(LinceFileProjectWrapper project, boolean comma, UUID research, List<Criteria> criteria) {
        LinceCsvExporter csv = new LinceCsvExporter();
        csv.setUseCsvComma(comma);
        csv.setResearchUUID(research);
        List<String> columns = Stream.concat(
                csv.getDefaultColumnDefinitions(project).stream().limit(LinceDataConstants.ColumnType.values().length),
                criteria.stream().map(Criteria::getCode)).toList();
        return csv.executeFormatConversion(project, columns).getBytes();
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
