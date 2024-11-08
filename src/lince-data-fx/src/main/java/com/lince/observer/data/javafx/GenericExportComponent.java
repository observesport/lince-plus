package com.lince.observer.data.javafx;

import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alberto Soto. 7/11/24
 */
public abstract class GenericExportComponent extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(GenericExportComponent.class);
    public GenericExportComponent() {
        initComponents();
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
                                 ExportFunction exportFunction) {
        List<Criterio> selectedData = selectionPanelComponent.getSelectedElements().stream()
                .filter(obj -> obj instanceof Criterio)
                .map(obj -> (Criterio) obj)
                .collect(Collectors.toList());

        if (selectedData.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }

        if (Registro.getInstance().getRowCount() == 0) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("CURRENT_REGISTER_HAS_NO_ROWS"));
            return;
        }

        Platform.runLater(() -> {
            File file = LinceDesktopFileHelper.openSaveFileDialog(getFileExtension());
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
