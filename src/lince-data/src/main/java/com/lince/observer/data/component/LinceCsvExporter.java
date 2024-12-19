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
 * Created by Alberto Soto. 7/11/24
 */
public class LinceCsvExporter implements ILinceFileExporter {

    private boolean isComma = true;
    TimeCalculations timeCalculations = new TimeCalculations();
    private static final String LINE_SEPARATOR = System.lineSeparator(); //= "\r\n"; //System.lineSeparator()

    @Override
    public List<String> getDefaultColumnDefinitions(ILinceProject linceProject) {
        List<String> columns = new ArrayList<>(Arrays.asList(
                LinceDataConstants.ColumnType.EVENT_TIME_FRAMES.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_FRAMES.toString(),
                LinceDataConstants.ColumnType.EVENT_TIME_SECONDS.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_SECONDS.toString(),
                LinceDataConstants.ColumnType.EVENT_TIME_MS.toString(),
                LinceDataConstants.ColumnType.EVENT_DURATION_MS.toString()
        ));
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

    private String getSeparatorValue() {
        return isComma ? LinceDataConstants.CSV_CHAR_SEPARATOR_COMMA : LinceDataConstants.CSV_CHAR_SEPARATOR_SEMICOLON;
    }

    private String createCsvHeader(List<String> columns) {
        return columns.stream()
                .map(Object::toString)
                .collect(Collectors.joining(getSeparatorValue())) + LINE_SEPARATOR;
    }

    @Override
    public String executeFormatConversion(ILinceProject linceProject, List<String> columnDefinitions) {
        Double fps = getFps(linceProject);
        StringBuilder content = new StringBuilder(createCsvHeader(columnDefinitions));
        // TODO 2025: Introduce support for multiple observers here
        List<RegisterItem> registers = linceProject.getRegister().get(0).getRegisterData();
        RegisterItem[] previousRegister = {null}; // Use an array to hold the previous register
        registers.forEach(register -> {
            String[] row = columnDefinitions.stream().map(column -> {
                if (Arrays.stream(LinceDataConstants.ColumnType.values())
                        .anyMatch(type -> type.toString().equals(column))) {
                    return handleStringColumn(column, register, previousRegister[0], fps);
                } else {
                    Criteria criteria = linceProject.getObservationTool().stream()
                            .filter(c -> c.getCode().equals(column))
                            .findFirst()
                            .orElse(null);
                    return criteria != null ? handleCriteriaColumn(criteria, register) : StringUtils.EMPTY;
                }
            }).toArray(String[]::new);

            content.append(String.join(getSeparatorValue(), row)).append(LINE_SEPARATOR);
            previousRegister[0] = register; // Update the previous register
        });
        return content.toString();
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

    public String handleStringColumn(String column, RegisterItem register, RegisterItem previousRegister, Double fps) {
        try {
            LinceDataConstants.ColumnType columnType = LinceDataConstants.ColumnType.fromValue(column);
            long eventDurationMs = register.getVideoTimeMilis() - (previousRegister != null ? (long) previousRegister.getVideoTimeMilis() : 0L);
            return switch (columnType) {
                case EVENT_TIME_FRAMES -> String.valueOf(register.getFrames());
                case EVENT_TIME_SECONDS -> timeCalculations.formatTimeWithAllComponents(register.getVideoTimeMilis());
                case EVENT_TIME_MS -> String.valueOf(register.getVideoTimeMilis());
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
