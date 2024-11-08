package com.lince.observer.data.javafx;

import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * Created by Alberto Soto. 7/11/24
 */
public class SasExportComponent extends GenericExportComponent {
    private static final Logger log = LoggerFactory.getLogger(SasExportComponent.class);

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExport = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_SAS"));
        btnExport.setOnAction(e -> executeSasExport(selectionPanelComponent));
        return Arrays.asList(btnExport);
    }

    @Override
    protected List<Object> getSelectionItems() {
        InstrumentoObservacional instrumento = InstrumentoObservacional.getInstance();
        return Stream.of(
                        instrumento.getCriterios(),
                        instrumento.getDatosMixtos(),
                        instrumento.getDatosFijos()
                )
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    String getFileExtension() {
        return "*.sds";
    }

    @Override
    String getExportTitle() {
        return "EXPORT_SAS";
    }

    private void executeSasExport(SelectionPanelComponent selectionPanelComponent) {
        List<Object> datos = selectionPanelComponent.getSelectedElements();
        if (datos.isEmpty()) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("SELECT_AT_LEAST_ONE_CRITERIA"));
            return;
        }

        Registro registro = Registro.getInstance();
        if (registro.getRowCount() == 0) {
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                    ResourceBundleHelper.getI18NLabel("LINCE"),
                    ResourceBundleHelper.getI18NLabel("CURRENT_REGISTER_HAS_NO_ROWS"));
            return;
        }

        File f = LinceDesktopFileHelper.openSaveFileDialog("*.sds");
        if (f != null) {
            String contenido = registro.exportToSas(datos);
            boolean success = writeContentToFile(f, contenido);
            if (success) {
                JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION,
                        "SAS",
                        ResourceBundleHelper.getI18NLabel("FILE_SAVED"));
            } else {
                JavaFXLogHelper.showMessage(Alert.AlertType.ERROR,
                        "SAS",
                        ResourceBundleHelper.getI18NLabel("ERROR_SAVING_FILE"));
            }
        }
    }

}
