package com.lince.observer.data.javafx;


import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Alberto Soto. 7/11/24
 */
public class CsvExportOpenCommand {
    public void execute() {
        Stage dialog = new Stage();
        dialog.setTitle(ResourceBundleHelper.getI18NLabel("EXPORT_EXCEL"));
        dialog.initModality(Modality.APPLICATION_MODAL);

        CsvExportComponent csvExportComponent = new CsvExportComponent();
        Scene scene = new Scene(csvExportComponent);
        dialog.setScene(scene);

        dialog.setMinWidth(550);
        dialog.setMinHeight(400);

        dialog.showAndWait();
    }
}
