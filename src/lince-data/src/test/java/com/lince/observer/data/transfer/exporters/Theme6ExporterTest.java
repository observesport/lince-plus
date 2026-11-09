package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.export.Lince2ThemeExport;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.TransferFixtures;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Theme6ExporterTest {

    private final Theme6Exporter exporter = new Theme6Exporter();

    @Test
    void descriptor() {
        ExportTypeDescriptor d = exporter.descriptor();
        assertEquals("theme6", d.id());
        assertEquals("txt", d.extension());
        assertEquals("text/plain", d.mimeType());
        assertTrue(d.supportsCriteriaSelection());
        assertTrue(d.multiFile());
        assertTrue(d.options().isEmpty());
    }

    @Test
    void allCriteriaIsByteIdenticalToTheFxComponent() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        assertEquals(1, project.getRegister().size(), "fixture has one observer");

        List<ExportedFile> files = exporter.export(new ExportSelection(project, null, List.of()), Map.of());

        assertEquals(2, files.size());
        ExportedFile txt = files.get(0);
        ExportedFile vvt = files.get(1);
        assertTrue(txt.name().endsWith(".txt"));
        assertEquals("vvt.vvt", vvt.name());
        // Theme6ExportComponent: all register rows and the full instrument, String.getBytes()
        List<RegisterItem> fxRows = project.getRegister().stream().flatMap(r -> r.getRegisterData().stream()).toList();
        assertArrayEquals(new Lince2ThemeExport(fxRows).exportToString().getBytes(), txt.content());
        assertArrayEquals(Lince2ThemeExport.createVvtContent(project.getObservationTool()).getBytes(), vvt.content());
    }

    @Test
    void criteriaSubsetFiltersTheInstrumentAndTheEvents() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        Criteria selected = project.getObservationTool().get(1);
        Set<String> allowed = selected.getInnerCategories().stream().map(Category::getCode).collect(Collectors.toSet());

        List<ExportedFile> files = exporter.export(
                new ExportSelection(project, null, List.of(selected.getCode())), Map.of());

        assertArrayEquals(Lince2ThemeExport.createVvtContent(List.of(selected)).getBytes(), files.get(1).content());
        List<String> lines = new String(files.get(0).content(), Charset.defaultCharset()).lines().toList();
        assertEquals("TIME\tEVENT", lines.get(0));
        assertFalse(lines.size() <= 3, "some rows use the selected criteria");
        for (String line : lines.subList(2, lines.size() - 1)) {
            for (String event : line.split("\t")[1].split(",")) {
                assertTrue(allowed.contains(event), event + " not in " + allowed);
            }
        }
        // the original project is untouched
        assertTrue(project.getRegister().get(0).getRegisterData().stream()
                .anyMatch(item -> item.getRegister().stream().anyMatch(c -> !allowed.contains(c.getCode()))));
    }

    @Test
    void projectWithoutObservationsExportsHeaderOnly() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.setRegister(new ArrayList<>());

        List<ExportedFile> files = exporter.export(new ExportSelection(project, null, List.of()), Map.of());

        assertEquals("TIME\tEVENT\r\n", new String(files.get(0).content(), Charset.defaultCharset()));
    }

    /**
     * Real project: both files are produced and carry its data (formerly {@code Lince2ThemeExport.createFile}).
     */
    @Test
    void realProjectProducesBothFilesWithData() {
        LinceFileProjectWrapper project = TransferFixtures.load("FCB_RMAD_0925.xml");
        int rows = project.getRegister().get(0).getRegisterData().size();

        List<ExportedFile> files = exporter.export(new ExportSelection(project, null, List.of()), Map.of());

        List<String> txt = new String(files.get(0).content(), Charset.defaultCharset()).lines().toList();
        assertEquals(rows + 3, txt.size(), "header + start marker + rows + end marker");
        assertTrue(txt.get(txt.size() - 1).endsWith("\t&"));
        String vvt = new String(files.get(1).content(), Charset.defaultCharset());
        assertTrue(vvt.contains("EQUIPO") && vvt.contains(" FCB") && vvt.contains(" RMAD"), vvt);
    }

    /**
     * The JavaFX menu always sends explicit ids: ticking every criteria must give the unfiltered export.
     */
    @Test
    void explicitSelectionOfEveryCriteriaIsTheUnfilteredExport() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.MULTIPLE);
        List<String> everyId = project.getObservationTool().stream().map(c -> String.valueOf(c.getId())).toList();

        List<ExportedFile> files = exporter.export(new ExportSelection(project, null, everyId), Map.of());

        List<RegisterItem> rows = project.getRegister().get(0).getRegisterData();
        assertArrayEquals(new Lince2ThemeExport(rows).exportToString().getBytes(), files.get(0).content());
        assertArrayEquals(Lince2ThemeExport.createVvtContent(project.getObservationTool()).getBytes(), files.get(1).content());
    }

    /**
     * Independent oracle: the Theme T-data layout written by hand, not produced by the code under test.
     */
    @Test
    void txtFollowsTheThemeTDataLayout() {
        Criteria criteria = new Criteria(1, "POST");
        criteria.setCode("POST");
        Category stand = new Category(10, "STAND", 1);
        stand.setCode("STAND");
        Category sit = new Category(11, "SIT", 1);
        sit.setCode("SIT");
        criteria.setInnerCategories(new java.util.LinkedList<>(List.of(stand, sit)));
        LinceFileProjectWrapper project = new LinceFileProjectWrapper();
        project.setObservationTool(new ArrayList<>(List.of(criteria)));
        com.lince.observer.data.bean.wrapper.LinceRegisterWrapper register = new com.lince.observer.data.bean.wrapper.LinceRegisterWrapper();
        register.getRegisterData().add(row(25, stand));
        register.getRegisterData().add(row(50, sit));
        project.setRegister(new ArrayList<>(List.of(register)));

        List<ExportedFile> files = exporter.export(new ExportSelection(project, null, List.of()), Map.of());

        assertEquals("TIME\tEVENT\r\n24\t:\r\n25\tSTAND\r\n50\tSIT\r\n51\t&\r\n",
                new String(files.get(0).content(), Charset.defaultCharset()));
        assertEquals("POST\r\n STAND\r\n SIT\r\n", new String(files.get(1).content(), Charset.defaultCharset()));
        assertTrue(files.get(0).mimeType().startsWith("text/plain; charset="), files.get(0).mimeType());
    }

    @Test
    void rowWithoutFrameNumberFailsInsteadOfTruncating() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.getRegister().get(0).getRegisterData().get(1).setFrames(null);

        ExportException e = assertThrows(ExportException.class,
                () -> exporter.export(new ExportSelection(project, null, List.of()), Map.of()));

        assertEquals(TransferException.Reason.IO, e.getReason());
    }

    @Test
    void rowWithoutCategoryListFailsAsIoInsteadOfTruncating() {
        LinceFileProjectWrapper project = TransferFixtures.load(TransferFixtures.SINGLE);
        project.getRegister().get(0).getRegisterData().get(1).setRegister(null);

        ExportException e = assertThrows(ExportException.class,
                () -> exporter.export(new ExportSelection(project, null, List.of()), Map.of()));

        assertEquals(TransferException.Reason.IO, e.getReason());
    }

    private static RegisterItem row(int frames, Category category) {
        RegisterItem item = new RegisterItem(frames / 25.0, category);
        item.setFrames(frames);
        return item;
    }
}
