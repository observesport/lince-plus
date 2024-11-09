package com.lince.observer.data.javafx;

import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.Arrays;
import java.util.List;

public class RegistroSdisGseqEstadoExport extends GenericExportComponent {

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExport = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_SDIS_GSEQ_ESTADO"));
        btnExport.setOnAction(e -> executeSdisGseqEstadoExport(selectionPanelComponent));
        return Arrays.asList(btnExport);
    }

    @Override
    protected List<Object> getSelectionItems() {
        return Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
    }

    @Override
    String getFileExtension() {
        return "*.sds";
    }

    @Override
    String getExportTitle() {
        return "GSEQ";
    }

    private void executeSdisGseqEstadoExport(SelectionPanelComponent selectionPanelComponent) {
        executeExport(selectionPanelComponent, Registro.getInstance()::exportToSdisGseqEstado, getFileExtension());
    }
}
