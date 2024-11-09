package com.lince.observer.data.javafx;

import com.lince.observer.data.legacy.commands.exportar.ExportarInstrumentoObservacionalTheme;
import com.lince.observer.data.legacy.commands.exportar.ExportarRegistroTheme6;
import com.lince.observer.data.legacy.datos.ControladorArchivos;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alberto Soto. 8/11/24
 */
public class Theme6ExportComponent extends GenericExportComponent {

    /*
    File f = LinceDesktopFileHelper.openSaveFileDialog("*.csv");
    String contenido = Registro.getInstance().exportToTheme5(criterios);
    //
    File f = LinceDesktopFileHelper.openSaveFileDialog("*.txt");
    String contenido = Registro.getInstance().exportToTheme6(criterios);
    //
    File f = LinceDesktopFileHelper.openSaveFileDialog("*.vvt");
    String contenido = InstrumentoObservacional.getInstance().exportToTheme(criterios);
     */

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExportInstrumento = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR INSTRUMENTO OBSERVACIONAL"));
        btnExportInstrumento.setOnAction(e -> {
            executeExport(selectionPanelComponent, InstrumentoObservacional.getInstance()::exportToTheme, ".vvt");
        });

        Button btnExportRegistro = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR REGISTRO"));
        btnExportRegistro.setOnAction(e -> {
            executeExport(selectionPanelComponent, Registro.getInstance()::exportToTheme6, "*.txt");
        });

        return Arrays.asList(btnExportInstrumento, btnExportRegistro);
    }

    @Override
    protected List<Object> getSelectionItems() {
        return Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
    }

    @Override
    String getFileExtension() {
        return ".vvt";
    }

    @Override
    String getExportTitle() {
        return "Theme 6 Export";
    }

}
