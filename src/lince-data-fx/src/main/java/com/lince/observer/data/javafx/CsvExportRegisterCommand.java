package com.lince.observer.data.javafx;

import com.lince.observer.data.ILinceProject;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.Registro;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alberto Soto. 7/11/24
 */
public class CsvExportRegisterCommand {
    private static final Logger log = LoggerFactory.getLogger(CsvExportRegisterCommand.class);
    private final SelectionPanelComponent selectionPanelComponent;
    private final boolean isWithComma;
    private final ILinceProject linceProject;
    private final UUID researchUUID;

    public CsvExportRegisterCommand(SelectionPanelComponent selectionPanelComponent, boolean isWithComma, ILinceProject linceProject, UUID researchUUID) {
        this.selectionPanelComponent = selectionPanelComponent;
        this.isWithComma = isWithComma;
        this.linceProject = linceProject;
        this.researchUUID = researchUUID;
    }

    public void execute() {
        List<Object> selectedData = selectionPanelComponent.getSelectedElements();
        if (selectedData.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }

        Platform.runLater(() -> {
            File file = LinceDesktopFileHelper.openSaveFileDialog("*.csv");
            if (file != null) {
                LinceCsvExporter csvExporter = new LinceCsvExporter();
                csvExporter.setUseCsvComma(isWithComma);
                csvExporter.setResearchUUID(researchUUID);
                String content = csvExporter.executeFormatConversion(linceProject, selectedData.stream().map(Object::toString).toList());
                boolean success = writeContentToFile(file, content);
                if (success) {
                    JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                            ResourceBundleHelper.getI18NLabel("CSV"),
                            ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
                } else {
                    JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                            ResourceBundleHelper.getI18NLabel("CSV"),
                            ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
                }
            }
        });
    }

    private boolean writeContentToFile(File file, String content) {
        try {
            java.nio.file.Files.write(file.toPath(), content.getBytes());
            return true;
        } catch (Exception e) {
            log.error("Error writing content to file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }
}
