package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Theme5ExporterTest {

    private final ExporterFixture fixture = new ExporterFixture();
    private final Theme5Exporter exporter = new Theme5Exporter(fixture.bridge);

    @Test
    void descriptor() {
        ExportTypeDescriptor descriptor = exporter.descriptor();
        assertEquals("theme5", descriptor.id());
        assertEquals("csv", descriptor.extension());
        assertTrue(descriptor.multiFile());
        assertTrue(descriptor.supportsCriteriaSelection());
    }

    @Test
    void registerAndInstrumentMatchTheLegacyThemeExports() {
        String csv = fixture.expected((registro, criterios) -> registro.exportToTheme5(criterios));
        String vvt = fixture.expected((registro, criterios) -> InstrumentoObservacional.getInstance().exportToTheme(criterios));

        List<ExportedFile> files = exporter.export(fixture.selection(), Map.of());

        assertEquals(2, files.size());
        assertEquals("theme5.csv", files.get(0).name());
        assertEquals("text/csv", files.get(0).mimeType());
        assertEquals(csv, new String(files.get(0).content(), Charset.defaultCharset()));
        assertEquals("theme5.vvt", files.get(1).name());
        assertEquals(vvt, new String(files.get(1).content(), Charset.defaultCharset()));
        assertTrue(csv.lines().count() > 3);
    }

    @Test
    void matchesTheGoldenFiles() {
        List<ExportedFile> files = exporter.export(fixture.selection(), Map.of());

        Golden.assertMatches("theme5.csv", files.get(0).content());
        Golden.assertMatches("theme5.vvt", files.get(1).content());
    }

    @Test
    void anEmptyRegisterExportsHeadersOnly() {
        List<ExportedFile> files = exporter.export(ExporterFixture.withoutObservations(fixture.project), Map.of());

        assertEquals("TIME;EVENT\r\n", new String(files.get(0).content(), Charset.defaultCharset()));
    }

    @Test
    void needsAtLeastOneCriterion() {
        ExportException ex = assertThrows(ExportException.class,
                () -> exporter.export(ExporterFixture.withoutCriteria(), Map.of()));

        assertEquals(TransferException.Reason.EMPTY_SELECTION, ex.getReason());
    }
}
