package com.lince.observer.data.transfer.exporters;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportTypeDescriptor;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.bean.transfer.TransferOption;
import com.lince.observer.data.bean.wrapper.LinceFileProjectWrapper;
import com.lince.observer.data.bean.wrapper.LinceRegisterWrapper;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportException;
import com.lince.observer.data.transfer.ExportSelection;
import com.lince.observer.data.transfer.LinceExporter;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.transfer.TransferException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CSV export of one register (LO-349), wrapping {@link LinceCsvExporter} the way the JavaFX {@code CsvExportComponent}
 * did: the time columns followed by the code of each selected criteria (in selection order), the register chosen
 * by research id, and {@code String.getBytes()} with the platform charset, which the MIME type names. At least one
 * column is required: time columns alone are a valid export.
 * <p>
 * Options: {@code separator}, {@code comma} or {@code semicolon} (default); {@code timeColumns}, the time columns
 * to write before the criteria, comma separated labels of {@link LinceDataConstants.ColumnType} in output order
 * (default all of them in the selection panel order, empty for none). An unknown value fails with
 * {@code INVALID_OPTION}.
 * <p>
 * Created by Alberto Soto. 21/10/26
 */
@Component
public class CsvExporter implements LinceExporter {

    public static final String TYPE_ID = "csv";
    public static final String OPTION_SEPARATOR = "separator";
    public static final String SEPARATOR_COMMA = "comma";
    public static final String SEPARATOR_SEMICOLON = "semicolon";
    public static final String OPTION_TIME_COLUMNS = "timeColumns";
    static final String MIME_TYPE = "text/csv";
    static final String FILE_NAME = "lince-export.csv";

    private static final ExportTypeDescriptor DESCRIPTOR = new ExportTypeDescriptor(TYPE_ID, "CSV", "csv", MIME_TYPE,
            true, false, List.of(
            TransferOption.choice(OPTION_SEPARATOR, "Separator", SEPARATOR_SEMICOLON,
                    List.of(SEPARATOR_COMMA, SEPARATOR_SEMICOLON)),
            TransferOption.string(OPTION_TIME_COLUMNS, "Time columns", String.join(",", LinceCsvExporter.TIME_COLUMNS))));

    @Override
    public ExportTypeDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public List<ExportedFile> export(ExportSelection selection, Map<String, String> options) {
        boolean comma = parseSeparator(options.get(OPTION_SEPARATOR));
        List<String> columns = new ArrayList<>(timeColumns(options.get(OPTION_TIME_COLUMNS)));
        selection.getCriteria().stream().map(Criteria::getCode).forEach(columns::add);
        if (columns.isEmpty()) {
            throw new ExportException(TransferException.Reason.EMPTY_SELECTION, "Select at least one column");
        }
        LinceFileProjectWrapper view = registerView(selection);

        LinceCsvExporter csv = new LinceCsvExporter();
        csv.setUseCsvComma(comma);

        String content = csv.executeFormatConversion(view, columns);
        return List.of(new ExportedFile(FILE_NAME, MIME_TYPE + "; charset=" + Charset.defaultCharset().name(),
                content.getBytes()));
    }

    /**
     * A view of the project holding only the selected register, so {@link LinceCsvExporter} needs no research id and
     * a project without registers exports the header only instead of failing.
     */
    private static LinceFileProjectWrapper registerView(ExportSelection selection) {
        LinceFileProjectWrapper view = new LinceFileProjectWrapper();
        view.setObservationTool(selection.getProject().getObservationTool());
        view.setProfiles(selection.getProject().getProfiles());
        LinceRegisterWrapper register = selection.getRegister();
        view.setRegister(new ArrayList<>(List.of(register != null ? register : new LinceRegisterWrapper())));
        return view;
    }

    /**
     * @param label a column label
     * @return true when the label is one of the time columns, false for criteria codes
     */
    public static boolean isTimeColumn(String label) {
        return LinceCsvExporter.TIME_COLUMNS.contains(label);
    }

    private static List<String> timeColumns(String value) {
        if (value == null) {
            return LinceCsvExporter.TIME_COLUMNS;
        }
        List<String> columns = new ArrayList<>();
        for (String token : StringUtils.split(value, ',')) {
            String column = token.trim();
            if (column.isEmpty()) {
                continue;
            }
            if (!isTimeColumn(column)) {
                throw invalidOption(OPTION_TIME_COLUMNS, "Unknown CSV time column: " + column + " (expected "
                        + String.join(", ", LinceCsvExporter.TIME_COLUMNS) + ")");
            }
            if (!columns.contains(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    /**
     * @return true for a comma separator, false for semicolon (the default)
     */
    private static boolean parseSeparator(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String normalised = value.trim().toLowerCase(Locale.ROOT);
        if (SEPARATOR_COMMA.equals(normalised) || LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA.equals(normalised)) {
            return true;
        }
        if (SEPARATOR_SEMICOLON.equals(normalised) || LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON.equals(normalised)) {
            return false;
        }
        throw invalidOption(OPTION_SEPARATOR, "Unknown CSV separator: " + value + " (expected comma or semicolon)");
    }

    private static ExportException invalidOption(String key, String message) {
        return new ExportException(TransferException.Reason.INVALID_OPTION, message, List.of(key));
    }
}
