package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.user.ResearchProfile;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.service.ProfileService;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.LinceImporter;
import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.data.transfer.exporters.CsvExporter;
import com.lince.observer.data.transfer.exporters.HoisanExporter;
import com.lince.observer.data.transfer.exporters.Lince1RegisterExporter;
import com.lince.observer.data.transfer.exporters.Lince1ToolExporter;
import com.lince.observer.data.transfer.exporters.LinceProjectExporter;
import com.lince.observer.data.transfer.exporters.SasExporter;
import com.lince.observer.data.transfer.exporters.SdisGseqExporter;
import com.lince.observer.data.transfer.exporters.Theme5Exporter;
import com.lince.observer.data.transfer.exporters.Theme6Exporter;
import com.lince.observer.data.transfer.importers.HoisanImporter;
import com.lince.observer.data.transfer.importers.Lince1RegisterImporter;
import com.lince.observer.data.transfer.importers.Lince1ToolImporter;
import com.lince.observer.data.transfer.importers.LinceProjectImporter;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.desktop.spring.service.CurrentProjectProvider;
import com.lince.observer.desktop.spring.service.DesktopExportService;
import com.lince.observer.desktop.spring.service.DesktopImportService;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;
import com.lince.observer.math.service.DataHubService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Headless end-to-end wiring of {@code /api/import} and {@code /api/export} for the abuse tests (LO-349): the real
 * controllers, {@link TransferExceptionHandler}, the desktop services, every real importer and exporter around one
 * real {@link LegacyBridge}, and a real {@link CurrentProjectProvider}. Only the storage behind the provider is
 * faked: {@link DataHubService} is a mock whose getters return the {@link #criteria}, {@link #registers} and
 * {@link #profiles} lists below and whose setters replace their content, so a test can tell exactly what an
 * import wrote to the "live" project. No Spring context and no JavaFX toolkit.
 */
final class TransferApiHarness {

    static final String IMPORT = "/api/import";
    static final String EXPORT = "/api/export";
    static final String SPOOL_PREFIX = "lince-import-";
    static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    final LegacyBridge bridge = new LegacyBridge();
    final ObservableList<Criteria> criteria = FXCollections.observableArrayList();
    final List<LinceRegisterWrapper> registers = new ArrayList<>();
    final ObservableList<ResearchProfile> profiles = FXCollections.observableArrayList();
    final DataHubService dataHub = mock(DataHubService.class);
    final ProfileService profileService = mock(ProfileService.class);
    final CurrentProjectProvider provider = new CurrentProjectProvider(dataHub, profileService, bridge);
    final List<LinceImporter> importers = List.of(new LinceProjectImporter(), new Lince1RegisterImporter(bridge),
            new Lince1ToolImporter(bridge), new HoisanImporter(bridge));
    final List<LinceExporter> exporters = List.of(new LinceProjectExporter(), new CsvExporter(), new Theme6Exporter(),
            new Theme5Exporter(bridge), new SasExporter(bridge),
            new SdisGseqExporter(bridge, GseqFormatType.EVENT), new SdisGseqExporter(bridge, GseqFormatType.STATE),
            new SdisGseqExporter(bridge, GseqFormatType.INTERVAL), new SdisGseqExporter(bridge, GseqFormatType.MULTIEVENT),
            new SdisGseqExporter(bridge, GseqFormatType.TIMED), new HoisanExporter(bridge),
            new Lince1RegisterExporter(bridge), new Lince1ToolExporter(bridge));
    final DesktopImportService importService = new DesktopImportService(importers, provider, dataHub);
    final DesktopExportService exportService = new DesktopExportService(exporters, provider);
    final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ImportControllerImpl(importService), new ExportControllerImpl(exportService))
            .setControllerAdvice(new TransferExceptionHandler())
            .build();

    TransferApiHarness() {
        assertTrue(GraphicsEnvironment.isHeadless(), "tests must run with -Djava.awt.headless=true (see the pom)");
        when(dataHub.getCriteria()).thenAnswer(i -> criteria);
        when(dataHub.getDataRegister()).thenAnswer(i -> registers);
        when(dataHub.getUserData()).thenAnswer(i -> profiles);
        when(dataHub.getVideoPlayList()).thenAnswer(i -> FXCollections.observableArrayList());
        when(dataHub.getYoutubeVideoPlayList()).thenAnswer(i -> FXCollections.observableArrayList());
        doAnswer(i -> replace(criteria, i.getArgument(0))).when(dataHub).setCriteria(anyList());
        doAnswer(i -> replace(registers, i.getArgument(0))).when(dataHub).setAllDataRegister(anyList());
        doAnswer(i -> replace(profiles, i.getArgument(0))).when(dataHub).setUserData(anyList());
        when(profileService.getAllResearchInfo()).thenAnswer(i -> profiles);
    }

    /**
     * Loads the Lince 1 fixture pair (instrument + one observer with observations) as the live project.
     */
    TransferApiHarness withLince1Project() {
        ILinceProject project = Lince1Fixtures.project(bridge);
        replace(criteria, project.getObservationTool());
        replace(registers, project.getRegister());
        replace(profiles, project.getProfiles());
        return this;
    }

    /**
     * Dummy odd names on the loaded project: every criteria, category, observer and research name gets quotes, a
     * semicolon and a line break (kept unique by index). Codes are left alone because they are the identities.
     */
    TransferApiHarness withOddNames() {
        String odd = "dummy \"quoted\"; semi\r\nline ";
        int n = 0;
        for (Criteria c : criteria) {
            c.setName(odd + "criteria " + n);
            c.setDescription(odd + n);
            for (var category : c.getInnerCategories() == null ? List.<com.lince.observer.data.bean.categories.Category>of()
                    : c.getInnerCategories()) {
                category.setName(odd + "category " + n++);
            }
            n++;
        }
        for (LinceRegisterWrapper register : registers) {
            if (register.getUserProfile() != null) {
                register.getUserProfile().setUserName(odd + "observer " + n++);
            }
        }
        for (ResearchProfile research : profiles) {
            research.setResearchName(odd + "research");
        }
        return this;
    }

    /**
     * An empty project with one research profile, as after "New project".
     */
    TransferApiHarness withEmptyProject() {
        replace(profiles, List.of(new ResearchProfile()));
        return this;
    }

    // ---- requests -------------------------------------------------------------------------------------------

    static MockMultipartFile upload(String fileName, byte[] content) {
        return new MockMultipartFile("file", fileName, "application/octet-stream", content);
    }

    static MockMultipartFile upload(String fileName, String content) {
        return upload(fileName, content.getBytes(StandardCharsets.UTF_8));
    }

    static byte[] resource(String path) {
        try (InputStream in = TransferApiHarness.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("missing test resource " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---- live project state -----------------------------------------------------------------------------------

    /**
     * Everything a failed request must leave as it was: the live list instances, their elements (by identity), and
     * the full project serialised as a Lince PLUS file, which also covers in-place mutation of those elements.
     */
    LiveState liveState() {
        return new LiveState(List.copyOf(criteria), List.copyOf(registers), List.copyOf(profiles),
                profiles.stream().map(p -> p.getUserProfiles() == null ? List.of() : List.copyOf(p.getUserProfiles())).toList(),
                serialise());
    }

    /**
     * Asserts the live project is exactly {@code before} and that the import never wrote to the data hub.
     */
    void assertUntouched(LiveState before) {
        verify(dataHub, never()).setCriteria(anyList());
        verify(dataHub, never()).setAllDataRegister(anyList());
        verify(dataHub, never()).setUserData(anyList());
        verify(dataHub, never()).reloadResearchProfileDataFromRegister();
        assertSameElements(before.criteria(), criteria);
        assertSameElements(before.registers(), registers);
        assertSameElements(before.profiles(), profiles);
        for (int i = 0; i < profiles.size(); i++) {
            List<?> users = profiles.get(i).getUserProfiles() == null ? List.of() : profiles.get(i).getUserProfiles();
            assertSameElements(before.users().get(i), users);
        }
        assertArrayEquals(before.xml(), serialise(), "the live project changed (serialised form differs)");
    }

    private byte[] serialise() {
        return new LinceProjectExporter()
                .export(new ExportSelection(provider.current(), null, List.of()), Map.of())
                .get(0).content();
    }

    private static void assertSameElements(List<?> expected, List<?> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertSame(expected.get(i), actual.get(i), "element " + i + " was replaced");
        }
    }

    record LiveState(List<Criteria> criteria, List<LinceRegisterWrapper> registers, List<ResearchProfile> profiles,
                     List<List<?>> users, byte[] xml) {
    }

    // ---- response hygiene ---------------------------------------------------------------------------------------

    /**
     * A 4xx/5xx body must not reveal the server: no temp folder path (spool files), no user home, no stack frame or
     * exception class name.
     */
    static void assertNoLeak(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertFalse(body.contains(TMP_DIR), () -> "body leaks java.io.tmpdir: " + body);
        String trimmedTmp = TMP_DIR.endsWith("/") || TMP_DIR.endsWith("\\")
                ? TMP_DIR.substring(0, TMP_DIR.length() - 1) : TMP_DIR;
        assertFalse(body.contains(trimmedTmp), () -> "body leaks java.io.tmpdir: " + body);
        assertFalse(body.contains(SPOOL_PREFIX), () -> "body names a spool file: " + body);
        assertFalse(body.contains(System.getProperty("user.home")), () -> "body leaks user.home: " + body);
        assertFalse(body.matches("(?s).*\\bat [a-z]+(\\.[A-Za-z0-9_$]+)+\\(.*"), () -> "body has a stack frame: " + body);
        assertFalse(body.matches("(?s).*\\b(java|javax|jakarta|org|com)\\.[a-z.]+[A-Z][A-Za-z]*(Exception|Error)\\b.*"),
                () -> "body names an exception class: " + body);
    }

    /**
     * @return number of upload spool files currently in the temp folder
     */
    static long spoolFiles() {
        try (Stream<Path> files = Files.list(Path.of(TMP_DIR))) {
            return files.filter(p -> p.getFileName().toString().startsWith(SPOOL_PREFIX)).count();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String text(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    static <T> Void replace(List<T> target, List<T> source) {
        List<T> copy = source == null ? List.of() : new ArrayList<>(source);
        target.clear();
        target.addAll(copy);
        return null;
    }
}
