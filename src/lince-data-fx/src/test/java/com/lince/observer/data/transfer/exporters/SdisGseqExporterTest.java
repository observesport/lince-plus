package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.TransferOption;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.export.GseqExporterFactory;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SdisGseqExporterTest {

    private final ExporterFixture fixture = new ExporterFixture();

    @Test
    void eachFormatMatchesTheLegacyRegisterExport() {
        for (GseqFormatType format : GseqFormatType.values()) {
            String expected = fixture.expected(legacyCall(format));

            List<ExportedFile> files = new SdisGseqExporter(fixture.bridge, format).export(fixture.selection(), Map.of());

            assertEquals(1, files.size(), format.name());
            assertEquals("gseq-" + format.name().toLowerCase() + ".sds", files.get(0).name());
            assertEquals(expected, new String(files.get(0).content(), Charset.defaultCharset()), format.name());
        }
    }

    @Test
    void intervalSecondsOptionOverridesTheComputedInterval() {
        String expected = fixture.expected((registro, criterios) ->
                GseqExporterFactory.createIntervalExporter(7).export(criterios, registro.datosVariables));

        List<ExportedFile> files = new SdisGseqExporter(fixture.bridge, GseqFormatType.INTERVAL)
                .export(fixture.selection(), Map.of(SdisGseqExporter.OPTION_INTERVAL_SECONDS, " 7 "));

        String content = new String(files.get(0).content(), Charset.defaultCharset());
        assertEquals(expected, content);
        assertTrue(content.startsWith("Interval=7'"), content);
    }

    @Test
    void invalidIntervalSecondsIsRejected() {
        SdisGseqExporter exporter = new SdisGseqExporter(fixture.bridge, GseqFormatType.INTERVAL);

        for (String value : List.of("abc", "0", "-3")) {
            ExportException ex = assertThrows(ExportException.class, () -> exporter.export(fixture.selection(),
                    Map.of(SdisGseqExporter.OPTION_INTERVAL_SECONDS, value)));
            assertEquals(TransferException.Reason.INVALID_OPTION, ex.getReason());
            assertEquals(List.of(SdisGseqExporter.OPTION_INTERVAL_SECONDS), ex.getOffendingIds());
        }
    }

    @Test
    void needsAtLeastOneCriterion() {
        ExportException ex = assertThrows(ExportException.class, () -> new SdisGseqExporter(fixture.bridge,
                GseqFormatType.EVENT).export(ExporterFixture.withoutCriteria(), Map.of()));

        assertEquals(TransferException.Reason.EMPTY_SELECTION, ex.getReason());
    }

    @Test
    void configurationRegistersOneBeanPerFormat() {
        SdisGseqExporterConfiguration configuration = new SdisGseqExporterConfiguration();
        LegacyBridge bridge = new LegacyBridge();

        List<ExportTypeDescriptor> descriptors = Stream.of(
                        configuration.sdisGseqEventExporter(bridge),
                        configuration.sdisGseqStateExporter(bridge),
                        configuration.sdisGseqIntervalExporter(bridge),
                        configuration.sdisGseqMultieventExporter(bridge),
                        configuration.sdisGseqTimedExporter(bridge))
                .map(SdisGseqExporter::descriptor).toList();

        assertEquals(List.of("sdis-gseq-event", "sdis-gseq-state", "sdis-gseq-interval", "sdis-gseq-multievent",
                "sdis-gseq-timed"), descriptors.stream().map(ExportTypeDescriptor::id).toList());
        for (ExportTypeDescriptor descriptor : descriptors) {
            assertEquals("sds", descriptor.extension());
            assertTrue(descriptor.supportsCriteriaSelection());
            boolean interval = descriptor.id().endsWith("interval");
            assertEquals(interval ? 1 : 0, descriptor.options().size(), descriptor.id());
        }
        TransferOption option = descriptors.get(2).options().get(0);
        assertEquals(SdisGseqExporter.OPTION_INTERVAL_SECONDS, option.key());
        assertEquals(TransferOption.Type.INTEGER, option.type());
        assertTrue(option.nullable());
    }

    private static BiFunction<Registro, List<Criterio>, String> legacyCall(GseqFormatType format) {
        return switch (format) {
            case EVENT -> Registro::exportToSdisGseqEvento;
            case STATE -> Registro::exportToSdisGseqEstado;
            case INTERVAL -> Registro::exportToSdisGseqIntervalo;
            case MULTIEVENT -> Registro::exportToSdisGseqMultievento;
            case TIMED -> Registro::exportToSdisGseqEventoConTiempo;
        };
    }
}
