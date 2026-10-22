package com.lince.observer.data.bean.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void importScopeSerialisesLowercaseAndParsesIgnoringCase() throws Exception {
        assertEquals("\"full\"", mapper.writeValueAsString(ImportScope.FULL));
        assertEquals("\"observations\"", mapper.writeValueAsString(ImportScope.OBSERVATIONS));
        assertEquals(ImportScope.OBSERVATIONS, mapper.readValue("\"observations\"", ImportScope.class));
        assertEquals(ImportScope.FULL, mapper.readValue("\"FULL\"", ImportScope.class));
    }

    @Test
    void importScopeFromValueDefaultsBlankToFullAndRejectsUnknown() {
        assertEquals(ImportScope.FULL, ImportScope.fromValue(null));
        assertEquals(ImportScope.FULL, ImportScope.fromValue(" "));
        assertEquals(ImportScope.OBSERVATIONS, ImportScope.fromValue("Observations"));
        assertThrows(IllegalArgumentException.class, () -> ImportScope.fromValue("videos"));
    }

    @Test
    void transferOptionTypeIsLowercaseAndNullsAreOmitted() throws Exception {
        TransferOption withDefault = TransferOption.integer("interval", "Interval", 2);
        TransferOption withoutDefault = TransferOption.integer("interval", "Interval", null);

        String json = mapper.writeValueAsString(withDefault);
        String nullableJson = mapper.writeValueAsString(withoutDefault);

        assertTrue(json.contains("\"type\":\"integer\""), json);
        assertTrue(json.contains("\"defaultValue\":\"2\""), json);
        assertFalse(json.contains("values"), json);
        assertFalse(withDefault.nullable());
        assertTrue(withoutDefault.nullable());
        assertFalse(nullableJson.contains("defaultValue"), nullableJson);
        assertEquals(withDefault, mapper.readValue(json, TransferOption.class));
        assertEquals(withoutDefault, mapper.readValue(nullableJson, TransferOption.class));
    }

    @Test
    void enumOptionCarriesValues() throws Exception {
        TransferOption option = TransferOption.choice("separator", "Separator", "comma", List.of("comma", "semicolon"));

        String json = mapper.writeValueAsString(option);

        assertTrue(json.contains("\"type\":\"enum\""), json);
        assertTrue(json.contains("\"values\":[\"comma\",\"semicolon\"]"), json);
        assertEquals(option, mapper.readValue(json, TransferOption.class));
    }

    @Test
    void importDescriptorRoundTripsAndNormalisesNullLists() throws Exception {
        ImportTypeDescriptor descriptor = new ImportTypeDescriptor("lince-plus", "Lince PLUS project", List.of("xml"), null);

        String json = mapper.writeValueAsString(descriptor);

        assertEquals("{\"id\":\"lince-plus\",\"label\":\"Lince PLUS project\",\"extensions\":[\"xml\"],\"options\":[]}", json);
        assertEquals(descriptor, mapper.readValue(json, ImportTypeDescriptor.class));
    }

    @Test
    void exportDescriptorRoundTrips() throws Exception {
        ExportTypeDescriptor descriptor = new ExportTypeDescriptor("theme6", "Theme 6", "txt", "text/plain", true, true,
                List.of(TransferOption.integer("intervalSeconds", "Interval", 1)));

        String json = mapper.writeValueAsString(descriptor);

        assertTrue(json.contains("\"supportsCriteriaSelection\":true"), json);
        assertTrue(json.contains("\"multiFile\":true"), json);
        assertTrue(json.contains("\"type\":\"integer\""), json);
        assertEquals(descriptor, mapper.readValue(json, ExportTypeDescriptor.class));
    }

    @Test
    void exportRequestDefaultsCriteriaIdsAndOptionsToEmpty() throws Exception {
        ExportRequest request = mapper.readValue("{\"type\":\"csv\"}", ExportRequest.class);

        assertEquals("csv", request.type());
        assertNull(request.researchId());
        assertEquals(List.of(), request.criteriaIds());
        assertEquals(Map.of(), request.options());
    }

    @Test
    void exportRequestOmitsNullResearchAndRoundTrips() throws Exception {
        UUID research = UUID.fromString("0b6c7d9e-1f2a-4b3c-8d4e-5f6a7b8c9d0e");
        ExportRequest request = new ExportRequest("csv", research, List.of("1", "2"), Map.of("separator", "semicolon"));

        assertEquals(request, mapper.readValue(mapper.writeValueAsString(request), ExportRequest.class));
        assertFalse(mapper.writeValueAsString(new ExportRequest("csv", null, null, null)).contains("researchId"));
    }

    @Test
    void importResultOmitsNullScopeAndKeepsWarnings() throws Exception {
        ImportResult result = new ImportResult("hoisan", null, 3, 1, 20, 1, List.of("skipped row 4"));

        String json = mapper.writeValueAsString(result);

        assertFalse(json.contains("scope"), json);
        assertTrue(json.contains("\"warnings\":[\"skipped row 4\"]"), json);
        assertEquals(result, mapper.readValue(json, ImportResult.class));
        assertTrue(mapper.writeValueAsString(new ImportResult("lince-plus", ImportScope.OBSERVATIONS, 0, 1, 5, 0, null))
                .contains("\"scope\":\"observations\""));
    }

    @Test
    void exportedFileComparesContentByValue() {
        ExportedFile a = new ExportedFile("a.csv", "text/csv", new byte[]{1, 2});
        ExportedFile b = new ExportedFile("a.csv", "text/csv", new byte[]{1, 2});

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertArrayEquals(new byte[]{1, 2}, a.content());
    }

    @Test
    void textFileUsesThePlatformCharsetAndNamesItInTheMimeType() {
        String content = "Pase;Zona\n\u00f1and\u00fa\n";

        ExportedFile file = ExportedFile.text("a.csv", "text/csv", content);

        assertEquals("a.csv", file.name());
        assertEquals("text/csv; charset=" + Charset.defaultCharset().name(), file.mimeType());
        assertArrayEquals(content.getBytes(Charset.defaultCharset()), file.content());
    }
}
