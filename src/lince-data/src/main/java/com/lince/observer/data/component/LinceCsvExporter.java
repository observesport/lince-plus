package com.lince.observer.data.component;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Category;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.util.TimeCalculations;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exporter component for CSV with support for , and ; as a delimiter based on LinceProject settings.
 * <p>
 * Every field goes through {@link #csvField}: RFC 4180 quoting when the field holds the separator, a double quote, CR
 * or LF; other fields are written as they are. With {@link #setExcelSafe(boolean)} a field starting with {@code =},
 * {@code +}, {@code -}, {@code @}, tab or CR is first prefixed with a single quote, so spreadsheets do not evaluate it
 * as a formula (OWASP CSV injection).
 * Created by Alberto Soto. 7/11/24
 */
public class LinceCsvExporter implements ILinceFileExporter {

    private boolean isComma = true;
    private boolean excelSafe = false;
    TimeCalculations timeCalculations = new TimeCalculations();
    private static final String LINE_SEPARATOR = System.lineSeparator(); //= "\r\n"; //System.lineSeparator()

    /**
     * The time columns, in the order of {@link #getDefaultColumnDefinitions}.
     */
    public static final List<String> TIME_COLUMNS = List.of(
            LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString(),
            LinceDataConstants.ColumnType.EVENT_DURATION_FRAMES.toString(),
            LinceDataConstants.ColumnType.EVENT_TIME_SECONDS.toString(),
            LinceDataConstants.ColumnType.EVENT_DURATION_SECONDS.toString(),
            LinceDataConstants.ColumnType.EVENT_TIME_MS.toString(),
            LinceDataConstants.ColumnType.EVENT_DURATION_MS.toString());

    @Override
    public List<String> getDefaultColumnDefinitions(ILinceProject linceProject) {
        List<String> columns = new ArrayList<>(TIME_COLUMNS);
        linceProject.getObservationTool().stream().map(Criteria::getCode).forEach(columns::add);
        return columns;
    }

    @Override
    public String getFileFormat() {
        return "csv";
    }

    public void setUseCsvComma(boolean useComma) {
        this.isComma = useComma;
    }

    /**
     * @param excelSafe true to prefix formula-leading fields with a single quote (default false)
     */
    public void setExcelSafe(boolean excelSafe) {
        this.excelSafe = excelSafe;
    }

    /**
     * Writes one CSV field.
     *
     * @param value     raw value, null written as empty
     * @param separator active separator
     * @param excelSafe true to prefix a value starting with {@code =}, {@code +}, {@code -}, {@code @}, tab or CR with
     *                  a single quote, before quoting
     * @return the value, wrapped in double quotes with inner quotes doubled when it holds the separator, a double
     * quote, CR or LF
     */
    public static String csvField(String value, String separator, boolean excelSafe) {
        String field = value == null ? StringUtils.EMPTY : value;
        if (excelSafe && !field.isEmpty() && "=+-@\t\r".indexOf(field.charAt(0)) >= 0) {
            field = "'" + field;
        }
        if (field.contains(separator) || field.indexOf('"') >= 0 || field.indexOf('\r') >= 0 || field.indexOf('\n') >= 0) {
            return '"' + field.replace("\"", "\"\"") + '"';
        }
        return field;
    }

    private String joinFields(List<String> fields) {
        String separator = getSeparatorValue();
        return fields.stream()
                .map(field -> csvField(field, separator, excelSafe))
                .collect(Collectors.joining(separator));
    }

    private String getSeparatorValue() {
        return isComma ? LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA : LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON;
    }

    private String createCsvHeader(List<String> columns) {
        return joinFields(columns) + LINE_SEPARATOR;
    }

    @Override
    public String executeFormatConversion(ILinceProject linceProject, List<String> columnDefinitions) {
        Double fps = getFps(linceProject);
        StringBuilder content = new StringBuilder(createCsvHeader(columnDefinitions));
        List<RegisterItem> selectedRegisters = getSelectedRegisters(linceProject);

        if (!selectedRegisters.isEmpty()) {
            RegisterItem zeroRegister = new RegisterItem(0.0);
            String[] firstRow = generateRow(columnDefinitions, zeroRegister, selectedRegisters.get(0), fps, linceProject);
            content.append(joinFields(Arrays.asList(firstRow))).append(LINE_SEPARATOR);
        }

        for (int i = 0; i < selectedRegisters.size(); i++) {
            RegisterItem currentRegister = selectedRegisters.get(i);
            RegisterItem nextRegister = (i < selectedRegisters.size() - 1) ? selectedRegisters.get(i + 1) : null;
            String[] row = generateRow(columnDefinitions, currentRegister, nextRegister, fps, linceProject);
            content.append(joinFields(Arrays.asList(row))).append(LINE_SEPARATOR);
        }
        return content.toString();
    }

    private String[] generateRow(List<String> columnDefinitions, RegisterItem register, RegisterItem previousRegister, Double fps, ILinceProject linceProject) {
        return columnDefinitions.stream().map(column -> {
            if (Arrays.stream(LinceDataConstants.ColumnType.values())
                    .anyMatch(type -> type.toString().equals(column))) {
                return handleStringColumn(column, register, previousRegister, fps);
            } else {
                Criteria criteria = linceProject.getObservationTool().stream()
                        .filter(c -> c.getCode().equals(column))
                        .findFirst()
                        .orElse(null);
                return criteria != null ? handleCriteriaColumn(criteria, register) : StringUtils.EMPTY;
            }
        }).toArray(String[]::new);
    }

    private List<RegisterItem> getSelectedRegisters(ILinceProject linceProject) {
        List<RegisterItem> registerData = linceProject.getRegister().get(0).getRegisterData();
        // Sort RegisterItems by videoTime (RegisterItem implements Comparable based on videoTime)
        return registerData.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public String handleCriteriaColumn(Criteria criteria, RegisterItem registerItem) {
        List<Category> register = registerItem.getRegister();
        if (criteria.isInformationNode()) {
            return registerItem.getRegister().stream()
                    .filter(cat -> cat.getCode().equals(criteria.getCode()))
                    .findFirst()
                    .map(Category::getNodeInformation)
                    .orElse(StringUtils.EMPTY);
        } else {
            return criteria.getInnerCategories().stream()
                    .filter(register::contains)
                    .map(Category::getCode)
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
        }
    }

    public String handleStringColumn(String column, RegisterItem register, RegisterItem nextRegister, Double fps) {
        try {
            LinceDataConstants.ColumnType columnType = LinceDataConstants.ColumnType.fromValue(column);
            long eventDurationMs = (nextRegister != null ? (long) nextRegister.getVideoTimeMillis() : register.getVideoTimeMillis()) - register.getVideoTimeMillis();
            return switch (columnType) {
                case EVENT_TIME_FRAMES -> String.valueOf(timeCalculations.convertMsToFPS(register.getVideoTimeMillis(), fps));
                case EVENT_TIME_SECONDS -> timeCalculations.formatTimeWithAllComponents(register.getVideoTimeMillis());
                case EVENT_TIME_MS -> String.valueOf(register.getVideoTimeMillis());
                case EVENT_DURATION_FRAMES -> String.valueOf(timeCalculations.convertMsToFPS(eventDurationMs, fps));
                case EVENT_DURATION_SECONDS -> timeCalculations.formatTimeWithAllComponents(eventDurationMs);
                case EVENT_DURATION_MS -> String.valueOf(eventDurationMs);
            };
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized column type: {}", column);
            return StringUtils.EMPTY;
        } catch (Exception e) {
            log.error("Error processing column {}: {}", column, e.getMessage());
            return StringUtils.EMPTY;
        }
    }

}
