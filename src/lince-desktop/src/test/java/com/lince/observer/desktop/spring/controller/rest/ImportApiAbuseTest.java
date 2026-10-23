package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.transfer.Lince1Fixtures;
import com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.LiveState;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.Set;

import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.IMPORT;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.assertNoLeak;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.resource;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.newSpoolFiles;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.spoolFileNames;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.text;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.upload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code POST /api/import} and {@code GET /api/import/types/{id}} driven by a hostile client (LO-349), end to end
 * through the real controller, exception handler, service, importers and legacy bridge (see
 * {@link TransferApiHarness}). Every rejected upload must answer the documented status and {@code reason}, must not
 * reveal server paths or internals, must not leave spool files behind, and must leave the loaded project exactly as
 * it was.
 */
class ImportApiAbuseTest {

    private static final String PLUS_PROJECT = "/prj/template.xml";

    private TransferApiHarness api;
    private Set<String> spoolBefore;
    private LiveState before;

    @BeforeEach
    void setUp() {
        api = new TransferApiHarness().withLince1Project();
        spoolBefore = spoolFileNames();
        before = api.liveState();
    }

    @AfterEach
    void noSpoolFileLeft() {
        assertEquals(Set.of(), newSpoolFiles(spoolBefore), "an upload spool file was left in java.io.tmpdir");
    }

    private MvcResult rejected(MockMultipartFile file, int status, String reason, String... params) throws Exception {
        var request = multipart(IMPORT).file(file);
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        MvcResult result = api.mvc.perform(request)
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reason").value(reason))
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
        return result;
    }

    // ---- unreadable content ------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"bad.xml", "bad.rlince", "bad.ilince", "bad.mdb"})
    void emptyUploadIsUnreadable(String name) throws Exception {
        rejected(upload(name, new byte[0]), 400, "unreadable");
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad.xml", "bad.rlince", "bad.ilince", "bad.mdb"})
    void binaryGarbageIsUnreadable(String name) throws Exception {
        byte[] garbage = new byte[4096];
        new java.util.Random(349).nextBytes(garbage);
        rejected(upload(name, garbage), 400, "unreadable");
    }

    @Test
    void truncatedLince1RegisterIsUnreadable() throws Exception {
        byte[] full = resource(Lince1Fixtures.REGISTER);
        rejected(upload("cut.rlince", Arrays.copyOf(full, full.length / 2)), 400, "unreadable");
    }

    @Test
    void truncatedLince1ToolIsUnreadable() throws Exception {
        byte[] full = resource(Lince1Fixtures.TOOL);
        rejected(upload("cut.ilince", Arrays.copyOf(full, full.length / 3)), 400, "unreadable");
    }

    @Test
    void truncatedLincePlusProjectIsUnreadable() throws Exception {
        byte[] full = resource(PLUS_PROJECT);
        rejected(upload("cut.xml", Arrays.copyOf(full, full.length / 2)), 400, "unreadable");
    }

    @Test
    void wellFormedXmlThatIsNotAProjectIsUnreadable() throws Exception {
        rejected(upload("other.xml", "<?xml version=\"1.0\"?><html><body>hi</body></html>"), 400, "unreadable");
    }

    /**
     * An empty Lince PLUS root holds no instrument and no observations: a full import of it is refused instead of
     * emptying the loaded project (LO-349 FR-9), whether the scope is the default or explicit.
     */
    @Test
    void emptyLincePlusRootIsRefusedAndLeavesTheProjectUntouched() throws Exception {
        String empty = "<?xml version=\"1.0\"?><linceProject/>";
        MvcResult result = rejected(upload("empty.xml", empty), 400, "empty_project");
        assertTrue(result.getResponse().getContentAsString().contains("The file holds no instrument and no observations"),
                result.getResponse().getContentAsString());
        rejected(upload("empty.xml", empty), 400, "empty_project", "scope", "full");
        assertFalse(api.criteria.isEmpty());
    }

    @Test
    void mdbHeaderWithoutDatabaseIsUnreadable() throws Exception {
        byte[] head = new byte[2048];
        byte[] magic = "\0\1\0\0Standard Jet DB\0".getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(magic, 0, head, 0, magic.length);
        rejected(upload("fake.mdb", head), 400, "unreadable");
    }

    @Test
    void lince1ToolContentUploadedAsRegisterIsUnreadable() throws Exception {
        rejected(upload("swapped.rlince", resource(Lince1Fixtures.TOOL)), 400, "unreadable");
    }

    @Test
    void lince1RegisterContentUploadedAsToolIsUnreadable() throws Exception {
        rejected(upload("swapped.ilince", resource(Lince1Fixtures.REGISTER)), 400, "unreadable");
    }

    // ---- Lince 1 decoder guard, dummy documents from lince-data -------------------------------------------------

    /**
     * Harmless dummy documents written for the lince-data guard tests: each one trips a single allow-list rule
     * (an unlisted JDK class, an extra method, a DOCTYPE pointing at a nonexistent file, ...). None of them does
     * anything if decoded; they only prove the guard is on the API path.
     */
    private static final Path DUMMY_GUARD_FIXTURES = Path.of("../lince-data/src/test/resources/legacy-xml/dummy");

    static Stream<Arguments> dummyGuardRejections() throws IOException {
        List<Path> files;
        try (Stream<Path> listing = Files.list(DUMMY_GUARD_FIXTURES)) {
            files = listing.filter(f -> f.getFileName().toString().startsWith("reject-")).sorted().toList();
        }
        assertEquals(24, files.size(), "dummy guard fixtures missing from " + DUMMY_GUARD_FIXTURES.toAbsolutePath());
        return files.stream().flatMap(f -> {
            String base = f.getFileName().toString().replaceFirst("\\.ilince$", "");
            return Stream.of(Arguments.of(f, base + ".ilince", "lince1-tool"),
                    Arguments.of(f, base + ".rlince", "lince1-register"));
        });
    }

    @ParameterizedTest(name = "{1} as {2}")
    @MethodSource("dummyGuardRejections")
    void dummyDocumentRejectedByTheLegacyGuardIsUnreadable(Path fixture, String uploadName, String type) throws Exception {
        MvcResult result = rejected(upload(uploadName, Files.readAllBytes(fixture)), 400, "unreadable", "type", type);
        String body = text(result);
        assertFalse(body.contains("nonexistent"), () -> "entity target echoed: " + body);
    }

    // ---- type, scope and request shape ----------------------------------------------------------------------------

    @Test
    void explicitTypeContradictingTheExtensionIs415() throws Exception {
        rejected(upload("project.xml", resource(PLUS_PROJECT)), 415, "type_mismatch", "type", "lince1-register");
    }

    @ParameterizedTest
    @ValueSource(strings = {"../../lince-plus", "lince-plus\r\nX-Injected: 1", "<script>alert(1)</script>",
            "LINCE-PLUS;rm", "%00"})
    void hostileExplicitTypeIsUnknownType(String type) throws Exception {
        MvcResult result = rejected(upload("project.xml", resource(PLUS_PROJECT)), 400, "unknown_type", "type", type);
        assertFalse(result.getResponse().getHeaderNames().contains("X-Injected"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"project.exe", "project", ".", "project.xml.exe", "project.xml ", "project.xml\0.exe"})
    void undetectableFileNameIsUnknownType(String name) throws Exception {
        rejected(upload(name, resource(PLUS_PROJECT)), 400, "unknown_type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"full; drop", "observations,full", "*", "../full"})
    void hostileScopeIsInvalidScope(String scope) throws Exception {
        rejected(upload("project.xml", resource(PLUS_PROJECT)), 400, "invalid_scope", "scope", scope);
    }

    @Test
    void observationsScopeIntoAProjectWithoutInstrumentIsInstrumentRequired() throws Exception {
        api = new TransferApiHarness().withEmptyProject();
        before = api.liveState();
        rejected(upload("observer.xml", resource(PLUS_PROJECT)), 400, "instrument_required", "scope", "observations");
    }

    @Test
    void lince1RegisterIntoAProjectWithoutInstrumentIsInstrumentRequired() throws Exception {
        api = new TransferApiHarness().withEmptyProject();
        before = api.liveState();
        rejected(upload("observer.rlince", resource(Lince1Fixtures.REGISTER)), 400, "instrument_required");
    }

    @Test
    void missingFilePartIsInvalidRequest() throws Exception {
        MvcResult result = api.mvc.perform(multipart(IMPORT).param("type", "lince-plus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("invalid_request"))
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @Test
    void fileSentUnderAnotherPartNameIsInvalidRequest() throws Exception {
        MockMultipartFile wrongPart = new MockMultipartFile("upload", "project.xml", "text/xml", resource(PLUS_PROJECT));
        MvcResult result = api.mvc.perform(multipart(IMPORT).file(wrongPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("invalid_request"))
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @Test
    void jsonBodyInsteadOfMultipartIsRefusedWithoutDetails() throws Exception {
        MvcResult result = api.mvc.perform(post(IMPORT).contentType(MediaType.APPLICATION_JSON).content("{\"file\":\"x\"}"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    // ---- hostile file names on a valid upload ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"../../../../etc/project.xml", "..\\..\\windows\\project.xml", "/etc/passwd/project.xml",
            "C:\\temp\\project.xml", "pro\r\nject.xml", "<img src=x>.xml"})
    void pathLikeFileNamesOnlyDriveTypeDetection(String name) throws Exception {
        MvcResult result = api.mvc.perform(multipart(IMPORT).file(upload(name, resource(PLUS_PROJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("lince-plus"))
                .andReturn();
        assertFalse(text(result).contains(name), "the client file name must not be echoed on success");
        assertEquals(Set.of(), newSpoolFiles(spoolBefore));
    }

    @Test
    void dummyFourKilobyteFileNameIsUnknownTypeAndEchoedTruncated() throws Exception {
        String name = "dummy-" + "a".repeat(4096) + ".dat";
        MvcResult result = rejected(upload(name, resource(PLUS_PROJECT)), 400, "unknown_type");
        String message = JsonPath.read(text(result), "$.message");
        assertTrue(message.contains("dummy-aaa"), message);
        assertFalse(message.contains("a".repeat(81)), message);
        assertTrue(message.contains("\u2026"), message);
    }

    @Test
    void dummyFileNameWithControlCharactersIsEchoedWithoutThem() throws Exception {
        MvcResult result = rejected(upload("dummy\r\nX-Injected: 1\t\u0000\u007f.dat", "x"), 400, "unknown_type");
        String message = JsonPath.read(text(result), "$.message");
        assertTrue(message.contains("dummyX-Injected: 1"), message);
        assertTrue(message.chars().noneMatch(c -> c < 0x20 || c == 0x7f), message);
    }

    @Test
    void dummyFileNameWithControlCharactersIsEchoedWithoutThemOnTypeMismatch() throws Exception {
        MvcResult result = rejected(upload("dummy\r\n" + "b".repeat(200) + ".xml", resource(PLUS_PROJECT)),
                415, "type_mismatch", "type", "lince1-tool");
        String message = JsonPath.read(text(result), "$.message");
        assertTrue(message.chars().noneMatch(c -> c < 0x20 || c == 0x7f), message);
        assertFalse(message.contains("b".repeat(81)), message);
    }

    @Test
    void dummyFourKilobyteTypeWithControlCharactersIsEchoedCappedAndClean() throws Exception {
        String type = "dummy\r\nX-Injected: 1\t\u007f" + "c".repeat(4096);
        MvcResult result = rejected(upload("project.xml", resource(PLUS_PROJECT)), 400, "unknown_type", "type", type);
        String message = JsonPath.read(text(result), "$.message");
        assertTrue(message.contains("dummyX-Injected: 1"), message);
        assertTrue(message.chars().noneMatch(c -> c < 0x20 || c == 0x7f), message);
        assertFalse(message.contains("c".repeat(81)), message);
        assertTrue(message.contains("\u2026"), message);
    }

    @Test
    void dummyFourKilobyteFileNameWithAKnownExtensionImportsNormally() throws Exception {
        String name = "dummy-" + "a".repeat(4096) + ".xml";
        MvcResult result = api.mvc.perform(multipart(IMPORT).file(upload(name, resource(PLUS_PROJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("lince-plus"))
                .andReturn();
        assertFalse(text(result).contains("a".repeat(64)), "the long name was echoed");
    }

    @Test
    void dummyUnknownOptionKeysAreIgnoredAndNotEchoed() throws Exception {
        MvcResult result = api.mvc.perform(multipart(IMPORT).file(upload("project.xml", resource(PLUS_PROJECT)))
                        .param("options.dummyUnknownKey", "dummy-value-../\r\n")
                        .param("options.", "dummy-empty-key")
                        .param("dummyNotAnOption", "dummy-other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("lince-plus"))
                .andReturn();
        String body = text(result);
        assertFalse(body.contains("dummy"), body);
    }

    @Test
    void echoedFileNameInAnErrorIsJsonEscaped() throws Exception {
        MvcResult result = rejected(upload("\"},\"reason\":\"ok.exe", "x"), 400, "unknown_type");
        assertTrue(text(result).startsWith("{\"reason\":\"unknown_type\""));
    }

    // ---- a failure after a success keeps the successful state ---------------------------------------------------

    @Test
    void failedImportAfterASuccessfulOneKeepsTheImportedProject() throws Exception {
        api.mvc.perform(multipart(IMPORT).file(upload("project.xml", resource(PLUS_PROJECT))))
                .andExpect(status().isOk());
        api = rebase(api);
        before = api.liveState();

        rejected(upload("cut.ilince", Arrays.copyOf(resource(Lince1Fixtures.TOOL), 100)), 400, "unreadable");
    }

    /**
     * Same live lists, fresh mock interactions: the next {@code assertUntouched} only covers what follows.
     */
    private static TransferApiHarness rebase(TransferApiHarness used) {
        TransferApiHarness next = new TransferApiHarness();
        TransferApiHarness.replace(next.criteria, used.criteria);
        TransferApiHarness.replace(next.registers, used.registers);
        TransferApiHarness.replace(next.profiles, used.profiles);
        return next;
    }

    // ---- GET /types/{id} ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"..", "..%2F..%2Fetc", "LINCE-PLUS%0d%0aX-Injected:1", "%3Cscript%3E", "lince-plus.json"})
    void unknownTypeIdIs404WithoutBody(String id) throws Exception {
        MvcResult result = api.mvc.perform(get(IMPORT + "/types/" + id))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals("", text(result));
        assertFalse(result.getResponse().getHeaderNames().contains("X-Injected"));
    }
}
