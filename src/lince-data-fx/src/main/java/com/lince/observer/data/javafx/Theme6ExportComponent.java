package com.lince.observer.data.javafx;

import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 8/11/24
 *
 * Theme 6 paired export: produces both .txt (register) and .vvt (instrument) files
 * from a single export action, as required by Theme 6 software.
 */
public class Theme6ExportComponent extends GenericExportComponent {
    public Theme6ExportComponent(UUID observerId) {
        super(observerId);
    }

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExport = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR THEME 6"));
        btnExport.setOnAction(e -> {
            executePairedExport(
                    selectionPanelComponent,
                    Registro.getInstance()::exportToTheme6, "*.txt",
                    InstrumentoObservacional.getInstance()::exportToTheme, ".vvt"
            );
        });

        return List.of(btnExport);
    }

    @Override
    protected List<Object> getSelectionItems() {
        return List.of(InstrumentoObservacional.getInstance().getCriterios());
    }

    @Override
    String getFileExtension() {
        return ".txt";
    }

    @Override
    String getExportTitle() {
        return "Theme 6 Export";
    }

}
