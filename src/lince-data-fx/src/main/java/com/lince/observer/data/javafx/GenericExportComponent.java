package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Criteria selection dialog of the JavaFX export menu (LO-349). The panel lists the project's criteria; the export
 * itself goes through {@link ExportService}, the same path {@code POST /api/export} takes, with the selected
 * criteria and the observer chosen in the menu. This class only asks for the target file and writes the returned
 * bytes ({@link ExportSaveFlow}).
 * <p>
 * Created by Alberto Soto. 7/11/24
 */
public abstract class GenericExportComponent extends BorderPane {
    private final ILinceProject linceProject;
    private final UUID researchUUID;
    private final ExportService exportService;
    private final String exportType;
    private final String buttonLabelKey;

    /**
     * @param linceProject   the loaded project, source of the selectable criteria
     * @param observerId     register to export, null for the default one
     * @param exportService  export dispatcher
     * @param exportType     export type id of the default button
     * @param buttonLabelKey i18n key of the default button
     */
    protected GenericExportComponent(ILinceProject linceProject, UUID observerId, ExportService exportService,
                                     String exportType, String buttonLabelKey) {
        this.linceProject = Objects.requireNonNull(linceProject, "linceProject");
        this.researchUUID = observerId;
        this.exportService = Objects.requireNonNull(exportService, "exportService");
        this.exportType = Objects.requireNonNull(exportType, "exportType");
        this.buttonLabelKey = buttonLabelKey;
        initComponents();
    }

    public ILinceProject getLinceProject() {
        return linceProject;
    }

    void initComponents() {
        HBox panelBotones = new HBox(10);
        panelBotones.setAlignment(Pos.CENTER_RIGHT);
        panelBotones.setPadding(new Insets(10));
        List<Object> lista = getSelectionItems();
        SelectionPanelComponent selectionPanelComponent = new SelectionPanelComponent(
                lista.toArray(),
                ResourceBundleHelper.getI18NLabel("ALL_CRITERIA"),
                ResourceBundleHelper.getI18NLabel("SELECTED_CRITERIA")
        );
        setCenter(selectionPanelComponent);
        panelBotones.getChildren().addAll(getActions(selectionPanelComponent));
        setBottom(panelBotones);
        setMinWidth(550);
        setMinHeight(400);
    }

    /**
     * @return the panel buttons, by default one exporting the selected criteria as the panel export type
     */
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button button = new Button(ResourceBundleHelper.getI18NLabel(buttonLabelKey));
        button.setOnAction(e -> executeExport(selectedCriteriaIds(selectionPanelComponent), Map.of()));
        return List.of(button);
    }

    /**
     * @return the items of the selection panel, by default the project's criteria
     */
    protected List<Object> getSelectionItems() {
        return new ArrayList<>(Optional.ofNullable(linceProject.getObservationTool()).orElse(List.of()));
    }

    /**
     * @return save dialog filter, e.g. {@code *.sds}
     */
    abstract String getFileExtension();

    abstract String getExportTitle();

    /**
     * @return the export id of each selected criteria, in selection order
     */
    protected static List<String> selectedCriteriaIds(SelectionPanelComponent selectionPanelComponent) {
        return selectionPanelComponent.getSelectedElements().stream()
                .filter(Criteria.class::isInstance)
                .map(Criteria.class::cast)
                .map(GenericExportComponent::criteriaId)
                .toList();
    }

    /**
     * @return the id an {@link ExportRequest} uses for the criteria: its numeric id, or its code when it has none
     */
    protected static String criteriaId(Criteria criteria) {
        return criteria.getId() != null ? String.valueOf(criteria.getId()) : criteria.getCode();
    }

    /**
     * Exports the criteria through {@link ExportService}, then asks for the file and writes the result. Errors are
     * shown before the save dialog opens.
     *
     * @param criteriaIds selected criteria, at least one
     * @param options     per-type options
     */
    protected void executeExport(List<String> criteriaIds, Map<String, String> options) {
        if (criteriaIds.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }
        String title = ResourceBundleHelper.getI18NLabel(getExportTitle());
        ExportSaveFlow flow = ExportSaveFlow.javaFx(getScene() == null ? null : getScene().getWindow());
        List<ExportedFile> files = flow.export(title,
                () -> exportService.export(new ExportRequest(exportType, researchUUID, criteriaIds, options)));
        if (files != null) {
            Platform.runLater(() -> flow.save(title, LinceDesktopFileHelper.openSaveFileDialog(getFileExtension()), files));
        }
    }
}
