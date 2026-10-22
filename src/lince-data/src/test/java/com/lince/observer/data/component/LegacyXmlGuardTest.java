package com.lince.observer.data.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LO-349: Lince 1 files are decoded with {@link java.beans.XMLDecoder}, which runs whatever the XML describes. The guard
 * must accept every real Lince 1 file and reject anything that is not a plain bean graph of the legacy classes.
 */
class LegacyXmlGuardTest {

    private static final Path FX_RES = Path.of("../lince-data-fx/src/test/resources");
    private static final Path MATH_RES = Path.of("../lince-math/src/test/resources");

    static Stream<Path> realFixtures() {
        return Stream.of(
                FX_RES.resolve("lince1/RandomLinceLegacy.ilince"),
                FX_RES.resolve("lince1/RandomLinceLegacy.rlince"),
                FX_RES.resolve("lince1/import/Instrumento_prueba.ilince"),
                FX_RES.resolve("lince1/import/Registro_prueba.rlince"),
                FX_RES.resolve("lince1/instrumentoObservacional-adv.ilince"),
                FX_RES.resolve("lince1/registroObservacional-adv.rlince"),
                FX_RES.resolve("prj/template.ilince"),
                FX_RES.resolve("prj/template.rlince"),
                FX_RES.resolve("transfer/golden/tool.ilince"),
                MATH_RES.resolve("template.ilince"),
                MATH_RES.resolve("template.rlince"));
    }

    @ParameterizedTest
    @MethodSource("realFixtures")
    void acceptsEveryRealLince1File(Path fixture) throws IOException {
        assertTrue(Files.isRegularFile(fixture), "missing fixture " + fixture.toAbsolutePath());
        byte[] xml = Files.readAllBytes(fixture);
        assertDoesNotThrow(() -> LegacyXmlGuard.check(xml));
        assertDoesNotThrow(() -> new PackageAwareXMLSerializer(new ByteArrayInputStream(xml)).close());
    }

    @Test
    void registerFixtureDecodesToAMap() throws IOException {
        // the legacy bean classes live in lince-data-fx, so only the outer map is checked here
        byte[] xml = Files.readAllBytes(MATH_RES.resolve("template.rlince"));
        try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(new ByteArrayInputStream(xml))) {
            Object decoded = decoder.readObject();
            assertInstanceOf(Map.class, decoded);
        }
    }

    @Test
    void acceptsWhatTheEncoderWritesAndRoundTrips() throws IOException {
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add(7);
        list.add(8L);
        list.add(1.5d);
        list.add(Boolean.TRUE);
        list.add(null);
        list.add("ctrl\u0001char <&>");
        map.put("list", list);
        map.put("name", "tool");
        String xml = PackageAwareXMLSerializer.encodeToXml(map);
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);

        assertDoesNotThrow(() -> LegacyXmlGuard.check(bytes));
        try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(new ByteArrayInputStream(bytes))) {
            assertEquals(map, decoder.readObject());
        }
    }

    // ---- rejections: DUMMY documents only, one fixture per rule, each harmless and merely outside the grammar ----

    static final Path DUMMIES = Path.of("src/test/resources/legacy-xml/dummy");

    @ParameterizedTest(name = "dummy {0} -> {1}")
    @CsvSource({
            "reject-unknown-class.ilince,                 CLASS,                  java.util.Date",
            "reject-unknown-class-stringbuilder.ilince,   CLASS,                  java.lang.StringBuilder",
            "reject-class-case-variant.ilince,            CLASS,                  java.util.hashmap",
            "reject-class-whitespace-variant.ilince,      CLASS,                  ' java.util.HashMap'",
            "reject-array-unknown-class.ilince,           CLASS,                  java.util.Date",
            "reject-root-decoder-class.ilince,            CLASS,                  java.util.Date",
            "reject-method-on-object.ilince,              ATTRIBUTE,              method",
            "reject-field-on-object.ilince,               ATTRIBUTE,              field",
            "reject-field-on-void.ilince,                 ATTRIBUTE,              field",
            "reject-class-on-void.ilince,                 ATTRIBUTE,              <void>",
            "reject-method-clear.ilince,                  METHOD,                 clear",
            "reject-method-tostring.ilince,               METHOD,                 toString",
            "reject-property-class.ilince,                PROPERTY,               property=\"class\"",
            "reject-property-classloader.ilince,          PROPERTY,               classLoader",
            "reject-unknown-element.ilince,               ELEMENT,                <dummy>",
            "reject-class-element.ilince,                 ELEMENT,                <class>",
            "reject-doctype-external-entity.ilince,       DTD,                    DOCTYPE",
            "reject-processing-instruction.ilince,        PROCESSING_INSTRUCTION, dummy-pi",
            "reject-root-not-java.ilince,                 STRUCTURE,              <java>",
            "reject-void-under-java.ilince,               STRUCTURE,              <void>",
            "reject-namespace.ilince,                     NAMESPACE,              namespace",
            "reject-negative-index.ilince,                VALUE,                  -1",
            "reject-malformed.ilince,                     MALFORMED,              malformed",
            "reject-too-deep.ilince,                      TOO_DEEP,               deep",
    })
    void rejectsDummyDocumentForEachRule(String file, LegacyXmlException.Reason reason, String named) throws IOException {
        byte[] xml = Files.readAllBytes(DUMMIES.resolve(file));
        LegacyXmlException e = assertThrows(LegacyXmlException.class, () -> LegacyXmlGuard.check(xml));
        assertEquals(reason, e.getReason(), e.getMessage());
        assertTrue(e.getMessage().contains(named), "message '" + e.getMessage() + "' should name " + named);
        assertTrue(e.getMessage().contains("line"), "message should carry a line: " + e.getMessage());
    }

    @Test
    void everyDummyFixtureIsListed() throws Exception {
        CsvSource csv = getClass().getDeclaredMethod("rejectsDummyDocumentForEachRule", String.class,
                LegacyXmlException.Reason.class, String.class).getAnnotation(CsvSource.class);
        Set<String> listed = Stream.of(csv.value()).map(row -> row.split(",")[0].trim()).collect(Collectors.toSet());
        try (Stream<Path> files = Files.list(DUMMIES)) {
            Set<String> present = files.map(f -> f.getFileName().toString()).collect(Collectors.toSet());
            assertEquals(present, listed);
        }
    }

    @Test
    void rejectsDummyDocumentBeforeDecoding_dummyStringBuilder() throws IOException {
        byte[] xml = Files.readAllBytes(DUMMIES.resolve("reject-unknown-class-stringbuilder.ilince"));
        LegacyXmlException e = assertThrows(LegacyXmlException.class,
                () -> new PackageAwareXMLSerializer(new ByteArrayInputStream(xml)));
        assertEquals(LegacyXmlException.Reason.CLASS, e.getReason());
    }

    /** Marker document: decoding it would only create an empty file in a JUnit temp dir. */
    private static byte[] emptyTempFileMarker(Path marker) {
        return ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<java version=\"17\" class=\"java.beans.XMLDecoder\">\n"
                + "<object class=\"java.io.File\"><string>" + marker.toAbsolutePath() + "</string>"
                + "<void method=\"createNewFile\"/></object>\n</java>\n").getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void guardRunsBeforeDecodingOnTheRealPath_dummyTempFileMarker(@TempDir Path tmp) {
        Path marker = tmp.resolve("marker");
        byte[] xml = emptyTempFileMarker(marker);
        LegacyXmlException e = assertThrows(LegacyXmlException.class, () -> {
            try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(new ByteArrayInputStream(xml))) {
                decoder.readObject();
            }
        });
        assertEquals(LegacyXmlException.Reason.CLASS, e.getReason());
        assertFalse(Files.exists(marker), "the marker document was decoded");
    }

    @Test
    void unguardedDecoderWouldCreateTheMarker_control(@TempDir Path tmp) {
        // proves the marker above is observable: plain XMLDecoder does create the empty temp file
        Path marker = tmp.resolve("marker");
        try (java.beans.XMLDecoder raw = new java.beans.XMLDecoder(new ByteArrayInputStream(emptyTempFileMarker(marker)))) {
            raw.readObject();
        }
        assertTrue(Files.exists(marker));
    }
}
