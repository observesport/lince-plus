package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.bean.transfer.ExportRequest;
import com.lince.observer.data.bean.transfer.ExportedFile;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.service.ExportService;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.transfer.TransferException;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Criteria selection dialog of the JavaFX export menu (LO-349). The panel lists the project's criteria; the export
 * itself goes through {@link ExportService}, the same path {@code POST /api/export} takes, with the selected
 * criteria and the observer chosen in the menu. This class only asks for the target file and writes the returned
 * bytes ({@link ExportTargets}).
 * <p>
 * Created by Alberto Soto. 7/11/24
 */
public abstract class GenericExportComponent extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(GenericExportComponent.class);
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

    public String getExportType() {
        return exportType;
    }

    public UUID getResearchUUID() {
        return researchUUID;
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
     * @return the panel buttons, by default one exporting the selected criteria as {@link #getExportType()}
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
        ExportAttempt.Result result = ExportAttempt.run(
                () -> exportService.export(new ExportRequest(exportType, researchUUID, criteriaIds, options)));
        if (result.failed()) {
            if (result.unexpected()) {
                JavaFXLogHelper.addLogError(exportType + " export failed", result.error());
            } else {
                TransferException e = (TransferException) result.error();
                log.warn("{} export: {} {}", exportType, e.getReason(), e.getMessage());
            }
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR, ResourceBundleHelper.getI18NLabel(getExportTitle()),
                    result.message());
            return;
        }
        List<ExportedFile> files = result.files();
        Platform.runLater(() -> saveFiles(files));
    }

    private void saveFiles(List<ExportedFile> files) {
        File chosen = LinceDesktopFileHelper.openSaveFileDialog(getFileExtension());
        if (chosen == null) {
            return;
        }
        List<ExportTargets.Target> targets = ExportTargets.resolve(chosen, files);
        if (!confirmOverwrite(ExportTargets.toConfirm(chosen, targets))) {
            return;
        }
        String title = ResourceBundleHelper.getI18NLabel(getExportTitle());
        try {
            ExportTargets.write(targets);
        } catch (Exception e) {
            log.error("Error writing the {} export", getExportTitle(), e);
            JavaFXLogHelper.showMessage(Alert.AlertType.ERROR, title, ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
            return;
        }
        if (targets.size() == 1) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION, title, ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
        } else {
            JavaFXLogHelper.showMessageDialog(Alert.AlertType.INFORMATION, title,
                    ResourceBundleHelper.getI18NLabel("FILES_SAVED") + "\n\n" + targets.stream()
                            .map(t -> "  • " + t.file().getName() + "\n    " + t.file().getAbsolutePath())
                            .collect(Collectors.joining("\n\n")));
        }
    }

    /**
     * Any existing target other than the file the save dialog returned (a sibling, or the primary with its
     * extension appended) needs confirmation; declining cancels the whole export.
     */
    private boolean confirmOverwrite(List<File> existing) {
        if (existing.isEmpty()) {
            return true;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                ResourceBundleHelper.getI18NLabel("OVERWRITE_FILE_PROMPT") + "\n"
                        + existing.stream().map(File::getAbsolutePath).collect(Collectors.joining("\n")),
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle(ResourceBundleHelper.getI18NLabel(getExportTitle()));
        confirm.setHeaderText(null);
        return confirm.showAndWait().filter(ButtonType.YES::equals).isPresent();
    }
}
