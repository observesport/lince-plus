package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportedFile;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SasExporterTest {

    private final ExporterFixture fixture = new ExporterFixture();
    private final SasExporter exporter = new SasExporter(fixture.bridge);

    @Test
    void descriptor() {
        assertEquals("sas", exporter.descriptor().id());
        assertEquals("sds", exporter.descriptor().extension());
        assertTrue(exporter.descriptor().supportsCriteriaSelection());
        assertFalse(exporter.descriptor().multiFile());
    }

    @Test
    void matchesTheLegacySasExport() {
        String expected = fixture.expected((registro, criterios) -> registro.exportToSas(new ArrayList<>(criterios)));

        List<ExportedFile> files = exporter.export(fixture.selection(), Map.of());

        assertEquals(1, files.size());
        assertEquals("sas.sds", files.get(0).name());
        assertEquals("text/plain; charset=" + Charset.defaultCharset().name(), files.get(0).mimeType());
        assertEquals(expected, new String(files.get(0).content(), Charset.defaultCharset()));
        assertFalse(expected.isEmpty());
    }

    @Test
    void matchesTheGoldenFile() {
        Golden.assertMatches("sas.sds", exporter.export(fixture.selection(), Map.of()).get(0).content());
    }
}
