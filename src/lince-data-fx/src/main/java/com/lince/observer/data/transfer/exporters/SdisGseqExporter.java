package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.TransferOption;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.legacy.LegacyBridge;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.export.GseqExporterFactory;
import com.lince.observer.legacy.export.GseqExporterFactory.GseqFormatType;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * SDIS-GSEQ export (LO-349), one bean per {@link GseqFormatType} (see {@link SdisGseqExporterConfiguration}):
 * {@code sdis-gseq-event}, {@code -state}, {@code -interval}, {@code -multievent}, {@code -timed}, each producing
 * the {@code .sds} the former {@code RegistroSdisGseq*Export} panel wrote (now {@code SdisGseqExportComponent}). Needs at least one criterion.
 * <p>
 * The interval type takes an optional {@value #OPTION_INTERVAL_SECONDS} integer option; omitted, the interval is
 * computed from the observations, as the panel does.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
public class SdisGseqExporter implements LinceExporter {

    public static final String TYPE_PREFIX = "sdis-gseq-";
    public static final String OPTION_INTERVAL_SECONDS = "intervalSeconds";

    private final LegacyBridge bridge;
    private final GseqFormatType format;

    public SdisGseqExporter(LegacyBridge bridge, GseqFormatType format) {
        this.bridge = Objects.requireNonNull(bridge, "bridge");
        this.format = Objects.requireNonNull(format, "format");
    }

    public GseqFormatType getFormat() {
        return format;
    }

    /**
     * @return {@code sdis-gseq-event}, {@code sdis-gseq-state}, {@code sdis-gseq-interval},
     * {@code sdis-gseq-multievent} or {@code sdis-gseq-timed}
     */
    public static String typeId(GseqFormatType format) {
        return TYPE_PREFIX + format.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public ExportTypeDescriptor descriptor() {
        List<TransferOption> options = format == GseqFormatType.INTERVAL
                ? List.of(new TransferOption(OPTION_INTERVAL_SECONDS, ResourceBundleHelper.getI18NLabel("INTERVALO"),
                TransferOption.Type.INTEGER, null, null, true))
                : List.of();
        return new ExportTypeDescriptor(typeId(format), ResourceBundleHelper.getI18NLabel(labelKey(format)), "sds",
                LegacyExportSupport.TEXT_PLAIN, true, false, options);
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        selection.requireCriteria();
        Integer intervalSeconds = format == GseqFormatType.INTERVAL ? intervalSeconds(options) : null;
        return bridge.export(selection, legacy -> {
            LegacyExportSupport.logWarnings(typeId(format), legacy);
            return List.of(LegacyExportSupport.text("gseq-" + format.name().toLowerCase(Locale.ROOT) + ".sds",
                    LegacyExportSupport.TEXT_PLAIN,
                    content(format, legacy.register(), legacy.selectedCriterios(), intervalSeconds)));
        });
    }

    /**
     * The SDIS-GSEQ content of one format, what the matching {@code Registro.exportToSdisGseq*} call returns.
     *
     * @param intervalSeconds only for {@link GseqFormatType#INTERVAL}: null computes it from the observations
     */
    public static String content(GseqFormatType format, Registro registro, List<Criterio> criterios, Integer intervalSeconds) {
        return switch (format) {
            case EVENT -> registro.exportToSdisGseqEvento(criterios);
            case STATE -> registro.exportToSdisGseqEstado(criterios);
            case MULTIEVENT -> registro.exportToSdisGseqMultievento(criterios);
            case TIMED -> registro.exportToSdisGseqEventoConTiempo(criterios);
            case INTERVAL -> intervalSeconds == null
                    ? registro.exportToSdisGseqIntervalo(criterios)
                    : GseqExporterFactory.createIntervalExporter(intervalSeconds).export(criterios, registro.datosVariables);
        };
    }

    private static Integer intervalSeconds(Map<String, String> options) {
        String value = options == null ? null : options.get(OPTION_INTERVAL_SECONDS);
        if (StringUtils.isBlank(value)) {
            return null;
        }
        int seconds = NumberUtils.toInt(value.trim(), 0);
        if (seconds <= 0) {
            throw new ExportException(TransferException.Reason.INVALID_OPTION,
                    OPTION_INTERVAL_SECONDS + " must be a positive integer: " + value, List.of(OPTION_INTERVAL_SECONDS));
        }
        return seconds;
    }

    /**
     * @return i18n key of the format label, also the button of the JavaFX panel
     */
    public static String labelKey(GseqFormatType format) {
        return switch (format) {
            case EVENT -> "EXPORT_SDIS_GSEQ_EVENT";
            case STATE -> "EXPORT_SDIS_GSEQ_ESTADO";
            case INTERVAL -> "EXPORT_SDIS_GSEQ_INTERVAL";
            case MULTIEVENT -> "EXPORT_SDIS_GSEQ_MULTIEVENT";
            case TIMED -> "EXPORT_SDIS_GSEQ_TIMED_EVENT";
        };
    }
}
