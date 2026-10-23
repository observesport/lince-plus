package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.transfer.exporters.CsvExporter;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * CSV export (LO-349). The panel lists the time columns and the criteria codes of {@link LinceCsvExporter}; the
 * selection becomes the criteria ids and the {@code timeColumns} option of {@link CsvExporter}, called through
 * {@link ExportService} with the separator of the pressed button. At least one criteria is required. This is the
 * "Excel" menu entry, so it always exports with {@code excelSafe=true}: formula-leading cells get a single quote.
 * <p>
 * Created by Alberto Soto. 7/11/24
 */
public class CsvExportComponent extends GenericExportComponent {

    public CsvExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService) {
        super(linceProject, observerId, exportService, CsvExporter.TYPE_ID, null);
    }

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        return List.of(
                button("EXPORT_CSV_COMMA", CsvExporter.SEPARATOR_COMMA, selectionPanelComponent),
                button("EXPORT_CSV_SEMICOLON", CsvExporter.SEPARATOR_SEMICOLON, selectionPanelComponent));
    }

    private Button button(String labelKey, String separator, SelectionPanelComponent selectionPanelComponent) {
        Button button = new Button(ResourceBundleHelper.getI18NLabel(labelKey));
        button.setOnAction(e -> {
            List<String> columns = selectionPanelComponent.getSelectedElements().stream().map(Object::toString).toList();
            List<String> timeColumns = columns.stream().filter(CsvExporter::isTimeColumn).toList();
            List<String> criteriaIds = columns.stream()
                    .filter(column -> !CsvExporter.isTimeColumn(column))
                    .map(this::criteriaIdOfCode)
                    .filter(Objects::nonNull)
                    .toList();
            executeExport(criteriaIds, Map.of(CsvExporter.OPTION_SEPARATOR, separator,
                    CsvExporter.OPTION_TIME_COLUMNS, String.join(",", timeColumns),
                    CsvExporter.OPTION_EXCEL_SAFE, Boolean.TRUE.toString()));
        });
        return button;
    }

    /**
     * The panel lists criteria by code; the request identifies them by id so a code that looks like another
     * criteria's id cannot be mismatched.
     */
    private String criteriaIdOfCode(String code) {
        return getLinceProject().getObservationTool().stream()
                .filter(c -> code.equals(c.getCode()))
                .findFirst()
                .map(GenericExportComponent::criteriaId)
                .orElse(null);
    }

    @Override
    protected List<Object> getSelectionItems() {
        return new ArrayList<>(new LinceCsvExporter().getDefaultColumnDefinitions(getLinceProject()));
    }

    @Override
    String getFileExtension() {
        return "*.csv";
    }

    @Override
    String getExportTitle() {
        return "Csv file";
    }
}
