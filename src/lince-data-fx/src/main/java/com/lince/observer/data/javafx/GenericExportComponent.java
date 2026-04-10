package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Created by Alberto Soto. 7/11/24
 */
public abstract class GenericExportComponent extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(GenericExportComponent.class);
    ILinceProject linceProject = null;
    private final UUID researchUUID;

    protected GenericExportComponent(UUID observerId) {
        this.researchUUID = observerId;
        initComponents();
    }

    public UUID getResearchUUID() {
        return researchUUID;
    }

    public ILinceProject getLinceProject() {
        return linceProject;
    }

    public void setLinceProject(ILinceProject linceProject) {
        this.linceProject = linceProject;
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

    abstract List<Node> getActions(SelectionPanelComponent selectionPanelComponent);

    abstract List<Object> getSelectionItems();

    abstract String getFileExtension();

    abstract String getExportTitle();

    protected void executeExport(SelectionPanelComponent selectionPanelComponent,
                                 ExportFunction exportFunction,
                                 String fileExtension) {
        List<Criterio> selectedData = selectionPanelComponent.getSelectedElements().stream()
                .filter(Criterio.class::isInstance)
                .map(Criterio.class::cast)
                .toList();

        if (selectedData.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }

      /*  if (Registro.getInstance().getRowCount() == 0) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("CURRENT_REGISTER_HAS_NO_ROWS"));
            return;
        }*/

        Platform.runLater(() -> {
            File file = LinceDesktopFileHelper.openSaveFileDialog(fileExtension);
            if (file != null) {
                String content = exportFunction.apply(selectedData);
                boolean success = writeContentToFile(file, content);
                if (success) {
                    JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
                } else {
                    JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
                }
            }
        });
    }

    protected void executePairedExport(SelectionPanelComponent selectionPanelComponent,
                                       ExportFunction primaryExport, String primaryExtension,
                                       ExportFunction secondaryExport, String secondaryExtension) {
        List<Criterio> selectedData = selectionPanelComponent.getSelectedElements().stream()
                .filter(Criterio.class::isInstance)
                .map(Criterio.class::cast)
                .toList();

        if (selectedData.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }

        Platform.runLater(() -> {
            File primaryFile = LinceDesktopFileHelper.openSaveFileDialog(primaryExtension);
            if (primaryFile != null) {
                String primaryContent = primaryExport.apply(selectedData);
                boolean primaryOk = writeContentToFile(primaryFile, primaryContent);

                File secondaryFile = deriveSiblingFile(primaryFile, primaryExtension, secondaryExtension);
                String secondaryContent = secondaryExport.apply(selectedData);
                boolean secondaryOk = writeContentToFile(secondaryFile, secondaryContent);

                if (primaryOk && secondaryOk) {
                    JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
                } else {
                    JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
                }
            }
        });
    }

    /**
     * Save-As on the primary file; sibling file is written next to it with a
     * fixed name. If the sibling already exists, ask the user to confirm
     * overwrite — declining cancels the whole export (primary file is not
     * written either).
     */
    protected void executeFixedSiblingExport(String primaryExtension,
                                              Supplier<String> primaryContent,
                                              String fixedSiblingFileName,
                                              Supplier<String> siblingContent) {
        Platform.runLater(() -> {
            File primaryFile = LinceDesktopFileHelper.openSaveFileDialog(primaryExtension);
            if (primaryFile == null) {
                return;
            }
            File siblingFile = new File(primaryFile.getParentFile(), fixedSiblingFileName);
            if (siblingFile.exists()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        ResourceBundleHelper.getI18NLabel("THEME6_OVERWRITE_VVT_PROMPT")
                                + "\n" + siblingFile.getAbsolutePath(),
                        ButtonType.YES, ButtonType.NO);
                confirm.setTitle(ResourceBundleHelper.getI18NLabel(getExportTitle()));
                confirm.setHeaderText(null);
                java.util.Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.YES) {
                    return;
                }
            }
            boolean primaryOk = writeContentToFile(primaryFile, primaryContent.get());
            boolean siblingOk = writeContentToFile(siblingFile, siblingContent.get());
            if (primaryOk && siblingOk) {
                JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                        ResourceBundleHelper.getI18NLabel(getExportTitle()),
                        ResourceBundleHelper.getI18NLabel("THEME6_BOTH_FILES_SAVED")
                                + "\n" + primaryFile.getAbsolutePath()
                                + "\n" + siblingFile.getAbsolutePath());
            } else {
                JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                        ResourceBundleHelper.getI18NLabel(getExportTitle()),
                        ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
            }
        });
    }

    protected void executeModernExport(Supplier<String> contentSupplier, String fileExtension) {
        Platform.runLater(() -> {
            File file = LinceDesktopFileHelper.openSaveFileDialog(fileExtension);
            if (file != null) {
                String content = contentSupplier.get();
                boolean success = writeContentToFile(file, content);
                if (success) {
                    JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
                } else {
                    JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                            ResourceBundleHelper.getI18NLabel(getExportTitle()),
                            ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
                }
            }
        });
    }

    static File deriveSiblingFile(File primaryFile, String primaryExt, String siblingExt) {
        String cleanPrimary = primaryExt.replace("*", "");
        String cleanSibling = siblingExt.replace("*", "");
        String path = primaryFile.getAbsolutePath();
        if (path.endsWith(cleanPrimary)) {
            path = path.substring(0, path.length() - cleanPrimary.length());
        }
        return new File(path + cleanSibling);
    }

    protected boolean writeContentToFile(File file, String content) {
        try {
            java.nio.file.Files.write(file.toPath(), content.getBytes());
            return true;
        } catch (Exception e) {
            log.error("Error writing content to file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    @FunctionalInterface
    protected interface ExportFunction {
        String apply(List<Criterio> selectedData);
    }
}
