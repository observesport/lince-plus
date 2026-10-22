package com.lince.observer.desktop.spring.controller.rest;

import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.LiveState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.EXPORT;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.assertNoLeak;
import static com.lince.observer.desktop.spring.controller.rest.TransferApiHarness.text;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code POST /api/export} and {@code GET /api/export/types/{id}} driven by a hostile client (LO-349), end to end
 * through the real controller, exception handler, service, exporters and legacy bridge (see
 * {@link TransferApiHarness}). A rejected request must answer the documented status and {@code reason} without
 * revealing internals; no export, accepted or not, may change the loaded project; downloads must carry safe file
 * names.
 */
class ExportApiAbuseTest {

    private TransferApiHarness api;
    private LiveState before;

    @BeforeEach
    void setUp() {
        api = new TransferApiHarness().withLince1Project();
        before = api.liveState();
    }

    private MvcResult rejected(String json, int status, String reason) throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reason").value(reason))
                .andReturn();
        assertNoLeak(result);
        assertTrue(result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION) == null,
                "an error must not be offered as a download");
        api.assertUntouched(before);
        return result;
    }

    private MvcResult exported(String json) throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andReturn();
        api.assertUntouched(before);
        return result;
    }

    private String criterionId() {
        return String.valueOf(api.criteria.get(0).getId());
    }

    // ---- type --------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"../../etc/passwd", "csv\\r\\nX-Injected: 1", "<script>alert(1)</script>", "csv;theme6",
            "", " ", "csv.zip", "CSV%00"})
    void hostileTypeIsUnknownType(String type) throws Exception {
        MvcResult result = rejected("{\"type\":\"" + type + "\"}", 400, "unknown_type");
        assertFalse(result.getResponse().getHeaderNames().contains("X-Injected"));
    }

    @Test
    void missingTypeIsUnknownType() throws Exception {
        rejected("{}", 400, "unknown_type");
    }

    @Test
    void typeOfTheWrongJsonKindIsInvalidRequest() throws Exception {
        rejected("{\"type\":{\"id\":\"csv\"}}", 400, "invalid_request");
    }

    // ---- researchId ----------------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"\"../../x\"", "\"1 OR 1=1\"", "123", "true", "[]", "\"00000000-0000-0000-0000-00000000000g\""})
    void nonUuidResearchIdIsInvalidRequest(String researchId) throws Exception {
        rejected("{\"type\":\"csv\",\"researchId\":" + researchId + "}", 400, "invalid_request");
    }

    @Test
    void unknownResearchIdIsUnknownResearchAndNamesIt() throws Exception {
        String id = UUID.randomUUID().toString();
        rejected("{\"type\":\"csv\",\"researchId\":\"" + id + "\"}", 400, "unknown_research");
        MvcResult result = rejected("{\"type\":\"sas\",\"researchId\":\"" + id + "\"}", 400, "unknown_research");
        assertTrue(text(result).contains(id));
    }

    // ---- criteriaIds ------------------------------------------------------------------------------------------

    @Test
    void hostileCriteriaIdsAreUnknownCriteriaAndListedVerbatim() throws Exception {
        api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"csv\",\"criteriaIds\":[\"" + criterionId() + "\",\"1 OR 1=1\",\"../x\",\"<b>\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("unknown_criteria"))
                .andExpect(jsonPath("$.ids", contains("1 OR 1=1", "../x", "<b>")));
        api.assertUntouched(before);
    }

    @Test
    void criteriaNameIsNotAnIdentity() throws Exception {
        Criteria first = api.criteria.get(0);
        rejected("{\"type\":\"csv\",\"criteriaIds\":[\"" + first.getName().replace("\"", "\\\"") + "\"]}",
                400, "unknown_criteria");
    }

    @Test
    void nullCriteriaIdIsAClientError() throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"csv\",\"criteriaIds\":[null]}"))
                .andReturn();
        String body = text(result);
        assertEquals(400, result.getResponse().getStatus(), () -> "got " + result.getResponse().getStatus() + " " + body);
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @Test
    void criteriaIdsOfTheWrongJsonKindIsInvalidRequest() throws Exception {
        rejected("{\"type\":\"csv\",\"criteriaIds\":{\"a\":1}}", 400, "invalid_request");
    }

    @Test
    void manyUnknownCriteriaIdsAreRejectedQuickly() throws Exception {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            ids.append(i == 0 ? "" : ",").append("\"x").append(i).append('"');
        }
        rejected("{\"type\":\"csv\",\"criteriaIds\":[" + ids + "]}", 400, "unknown_criteria");
    }

    // ---- options ---------------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"tab", "|", "\\u0000", "comma;semicolon", "=cmd"})
    void hostileCsvSeparatorIsInvalidOption(String separator) throws Exception {
        api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"csv\",\"options\":{\"separator\":\"" + separator + "\"}}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("invalid_option"))
                .andExpect(jsonPath("$.ids", contains("separator")));
        api.assertUntouched(before);
    }

    @Test
    void unknownCsvTimeColumnIsInvalidOption() throws Exception {
        rejected("{\"type\":\"csv\",\"options\":{\"timeColumns\":\"../x,<script>\"}}", 400, "invalid_option");
    }

    @Test
    void nullOptionValueIsAClientError() throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"csv\",\"options\":{\"separator\":null}}"))
                .andReturn();
        String body = text(result);
        assertEquals(400, result.getResponse().getStatus(), () -> "got " + result.getResponse().getStatus() + " " + body);
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "abc", "1e9", "NaN", "99999999999999999999", "2147483648"})
    @Timeout(20)
    void hostileGseqIntervalIsRejectedOrBounded(String seconds) throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"sdis-gseq-interval\",\"options\":{\"intervalSeconds\":\"" + seconds + "\"}}"))
                .andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 400, () -> seconds + " answered " + status);
        if (status == 400) {
            assertNoLeak(result);
            String body = text(result);
            assertTrue(body.contains("\"reason\":\"invalid_option\""), body);
        }
        api.assertUntouched(before);
    }

    // ---- request shape ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"type\":\"csv\"", "[]", "null", "\"csv\""})
    void malformedJsonIsInvalidRequest(String json) throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @Test
    void deeplyNestedJsonIsRejectedWithoutAStackOverflow() throws Exception {
        String nested = "[".repeat(5_000) + "]".repeat(5_000);
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"csv\",\"options\":" + nested + "}"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertNoLeak(result);
        api.assertUntouched(before);
    }

    @Test
    void formEncodedBodyIsUnsupportedMediaType() throws Exception {
        MvcResult result = api.mvc.perform(post(EXPORT).contentType(MediaType.APPLICATION_FORM_URLENCODED).content("type=csv"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn();
        assertNoLeak(result);
    }

    @Test
    void unknownJsonFieldsAreIgnoredNotExecuted() throws Exception {
        exported("{\"type\":\"csv\",\"@class\":\"java.lang.Object\",\"fileName\":\"../../evil.sh\"}");
    }

    // ---- empty project ---------------------------------------------------------------------------------------

    @Test
    void exportOfAnEmptyProjectIsEmptySelection() throws Exception {
        api = new TransferApiHarness().withEmptyProject();
        before = api.liveState();
        rejected("{\"type\":\"sas\"}", 400, "empty_selection");
    }

    // ---- downloads: every type, safe names, project untouched --------------------------------------------------

    static Stream<String> exportTypes() {
        return new TransferApiHarness().exporters.stream().map(e -> e.descriptor().id());
    }

    @ParameterizedTest
    @MethodSource("exportTypes")
    void everyTypeDownloadsUnderASafeNameWithoutTouchingTheProject(String type) throws Exception {
        ExportTypeDescriptor descriptor = api.exportService.type(type).orElseThrow();
        MvcResult result = exported("{\"type\":\"  " + type.toUpperCase() + " \"}");

        String disposition = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(disposition);
        assertTrue(disposition.startsWith("attachment;"), disposition);
        assertFalse(disposition.contains("/") || disposition.contains("\\") || disposition.contains(".."), disposition);
        assertFalse(disposition.contains("\r") || disposition.contains("\n"), disposition);
        byte[] body = result.getResponse().getContentAsByteArray();
        assertTrue(body.length > 0);
        if (descriptor.multiFile()) {
            assertTrue(disposition.contains(type + "-export.zip"), disposition);
            List<String> entries = zipEntries(body);
            assertTrue(entries.size() > 1, entries::toString);
            for (String entry : entries) {
                assertFalse(entry.contains("/") || entry.contains("\\") || entry.contains(".."), entry);
            }
        }
    }

    private static List<String> zipEntries(byte[] zip) throws Exception {
        List<String> names = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        return names;
    }

    /**
     * The file names each exporter declares as constants; a multi-file type lists its zip entries.
     */
    private static final java.util.Map<String, List<String>> CONSTANT_NAMES = java.util.Map.ofEntries(
            java.util.Map.entry("lince-plus", List.of("lince-project.xml")),
            java.util.Map.entry("csv", List.of("lince-export.csv")),
            java.util.Map.entry("theme6", List.of("theme6.txt", "vvt.vvt")),
            java.util.Map.entry("theme5", List.of("theme5.csv", "theme5.vvt")),
            java.util.Map.entry("sas", List.of("sas.sds")),
            java.util.Map.entry("sdis-gseq-event", List.of("gseq-event.sds")),
            java.util.Map.entry("sdis-gseq-state", List.of("gseq-state.sds")),
            java.util.Map.entry("sdis-gseq-interval", List.of("gseq-interval.sds")),
            java.util.Map.entry("sdis-gseq-multievent", List.of("gseq-multievent.sds")),
            java.util.Map.entry("sdis-gseq-timed", List.of("gseq-timed.sds")),
            java.util.Map.entry("hoisan", List.of("hoisan.mdb")),
            java.util.Map.entry("lince1-register", List.of("register.rlince")),
            java.util.Map.entry("lince1-tool", List.of("tool.ilince")));

    @ParameterizedTest
    @MethodSource("exportTypes")
    void dummyOddNamesNeverReachTheDownloadNames(String type) throws Exception {
        api = new TransferApiHarness().withLince1Project().withOddNames();
        before = api.liveState();
        ExportTypeDescriptor descriptor = api.exportService.type(type).orElseThrow();
        List<String> expected = CONSTANT_NAMES.get(type);
        assertNotNull(expected, "no constant names listed for " + type);

        MvcResult result = exported("{\"type\":\"" + type + "\"}");

        String disposition = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        MediaType contentType = MediaType.parseMediaType(result.getResponse().getContentType());
        if (descriptor.multiFile()) {
            assertEquals("attachment; filename=\"" + type + "-export.zip\"", disposition);
            assertTrue(contentType.equalsTypeAndSubtype(MediaType.parseMediaType("application/zip")), contentType::toString);
            assertEquals(expected, zipEntries(result.getResponse().getContentAsByteArray()));
        } else {
            assertEquals("attachment; filename=\"" + expected.get(0) + "\"", disposition);
            assertTrue(contentType.equalsTypeAndSubtype(MediaType.parseMediaType(descriptor.mimeType())),
                    () -> contentType + " vs " + descriptor.mimeType());
        }
    }

    // ---- GET /types/{id} ---------------------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"..", "..%2F..%2Fetc", "csv%0d%0aX-Injected:1", "%3Cscript%3E", "csv.json"})
    void unknownTypeIdIs404WithoutBody(String id) throws Exception {
        MvcResult result = api.mvc.perform(get(EXPORT + "/types/" + id))
                .andExpect(status().isNotFound())
                .andReturn();
        assertEquals("", text(result));
        assertFalse(result.getResponse().getHeaderNames().contains("X-Injected"));
    }
}
