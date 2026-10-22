package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.ImportScope;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.transfer.importers.Lince1RegisterImporter;
import com.lince.observer.data.transfer.importers.Lince1ToolImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link Lince1RegisterExporter} and {@link Lince1ToolExporter}: same bytes as the legacy {@code save()} that
 * the former {@code SaveRegistroAs} / {@code SaveInstrumentoObservacionalAs} commands made, and readable by the Lince 1 importers.
 */
class Lince1ExportersTest {

    private final ExporterFixture fixture = new ExporterFixture();

    @TempDir
    Path dir;

    @Test
    void descriptors() {
        Lince1RegisterExporter register = new Lince1RegisterExporter(fixture.bridge);
        Lince1ToolExporter tool = new Lince1ToolExporter(fixture.bridge);
        assertEquals("lince1-register", register.descriptor().id());
        assertEquals("rlince", register.descriptor().extension());
        assertEquals("lince1-tool", tool.descriptor().id());
        assertEquals("ilince", tool.descriptor().extension());
        assertFalse(register.descriptor().supportsCriteriaSelection());
        assertFalse(tool.descriptor().supportsCriteriaSelection());
    }

    /**
     * {@code Criterio} has no {@code hashCode}, so the row maps of a .rlince serialise in identity hash order: two
     * conversions of the same project (in the menu too) give equivalent files with entries in a different order.
     * Bytes are therefore compared against {@code save()} on the same legacy state, and the exporter output by
     * re-importing it.
     */
    @Test
    void registerBytesAreWhatRegistroSaveWrites() throws Exception {
        File saved = dir.resolve("saved.rlince").toFile();
        fixture.bridge.export(fixture.selection(), legacy -> {
            legacy.register().setPath(saved);
            legacy.register().save();
            assertArrayEquals(readBytes(saved), Lince1RegisterExporter.content(legacy.register()));
            return null;
        });

        List<ExportedFile> files = new Lince1RegisterExporter(fixture.bridge).export(fixture.selection(), Map.of());

        assertEquals("register.rlince", files.get(0).name());
        LinceFileProjectWrapper back = new LinceFileProjectWrapper();
        back.setObservationTool(new ArrayList<>(fixture.project.getObservationTool()));
        new Lince1RegisterImporter(fixture.bridge).importInto(back, new ByteArrayInputStream(files.get(0).content()),
                ImportScope.FULL, Map.of());
        assertEquals(fixture.project.getRegister().get(0).getRegisterData().size(),
                back.getRegister().get(0).getRegisterData().size());
    }

    @Test
    void toolBytesAreWhatInstrumentoObservacionalSaveWrites() throws Exception {
        File saved = dir.resolve("saved.ilince").toFile();
        byte[] expected = fixture.bridge.export(fixture.selection(), legacy -> {
            legacy.instrument().setPath(saved);
            legacy.instrument().save();
            return readBytes(saved);
        });

        List<ExportedFile> files = new Lince1ToolExporter(fixture.bridge).export(fixture.selection(), Map.of());

        assertEquals("tool.ilince", files.get(0).name());
        assertArrayEquals(expected, files.get(0).content());
        LinceFileProjectWrapper back = new LinceFileProjectWrapper();
        new Lince1ToolImporter(fixture.bridge).importInto(back, new ByteArrayInputStream(files.get(0).content()),
                ImportScope.FULL, Map.of());
        assertEquals(fixture.project.getObservationTool().stream().map(Criteria::getName).toList(),
                back.getObservationTool().stream().map(Criteria::getName).toList());
    }

    /**
     * The .ilince has no identity-ordered maps, so its bytes are stable across runs apart from the JDK version
     * in the XMLEncoder header; the .rlince is only checked by re-import (see above).
     */
    @Test
    void toolMatchesTheGoldenFile() {
        Golden.assertXmlEncoderMatches("tool.ilince",
                new Lince1ToolExporter(fixture.bridge).export(fixture.selection(), Map.of()).get(0).content());
    }

    private static byte[] readBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
