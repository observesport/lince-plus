package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.TransferFixtures;
import com.lince.observer.data.util.LinceFileHelper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LinceProjectExporterTest {

    private final LinceProjectExporter exporter = new LinceProjectExporter();

    @Test
    void descriptor() {
        ExportTypeDescriptor d = exporter.descriptor();
        assertEquals("lince-plus", d.id());
        assertEquals("xml", d.extension());
        assertEquals("application/xml", d.mimeType());
        assertFalse(d.supportsCriteriaSelection());
        assertFalse(d.multiFile());
    }

    @Test
    void roundTripKeepsTheWholeProject() throws Exception {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);

        // criteria selection is ignored: the whole project is written
        List<ExportedFile> files = exporter.export(new ExportSelection(project, null,
                List.of(project.getObservationTool().get(0).getCode())), Map.of());

        assertEquals(1, files.size());
        ExportedFile file = files.get(0);
        assertEquals("application/xml", file.mimeType());
        LinceFileProjectWrapper read = new LinceFileHelper().readProject(new ByteArrayInputStream(file.content()));
        assertEquals(project.getObservationTool().stream().map(Criteria::getCode).toList(),
                read.getObservationTool().stream().map(Criteria::getCode).toList());
        assertEquals(project.getRegister().stream().map(LinceRegisterWrapper::getId).toList(),
                read.getRegister().stream().map(LinceRegisterWrapper::getId).toList());
        assertEquals(project.getRegister().stream().map(r -> r.getRegisterData().size()).toList(),
                read.getRegister().stream().map(r -> r.getRegisterData().size()).toList());
        assertEquals(project.getProfiles().size(), read.getProfiles().size());
        assertEquals(project.getYoutubeVideoPlayList(), read.getYoutubeVideoPlayList());
    }

    @Test
    void contentIsByteIdenticalToWriteProjectFile() throws Exception {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        File file = File.createTempFile("lince-export-", ".xml");
        try {
            new LinceFileHelper().writeProjectFile(file, project);

            ExportedFile exported = exporter.export(new ExportSelection(project, null, null), Map.of()).get(0);

            assertArrayEquals(Files.readAllBytes(file.toPath()), exported.content());
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }
}
