package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.export.Lince2ThemeExport;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 8/11/24
 *
 * Theme 6 export using modern API: produces .txt (register) and .vvt (instrument) files
 * directly from ILinceProject data, without legacy Registro dependency.
 */
public class Theme6ExportComponent extends GenericExportComponent {
    public Theme6ExportComponent(UUID observerId) {
        super(observerId);
    }

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExportInstrumento = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR INSTRUMENTO OBSERVACIONAL"));
        btnExportInstrumento.setOnAction(e -> {
            List<Criteria> criteria = linceProject.getObservationTool();
            executeModernExport(() -> Lince2ThemeExport.createVvtContent(criteria), "*.vvt");
        });

        Button btnExportRegistro = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR REGISTRO"));
        btnExportRegistro.setOnAction(e -> {
            List<RegisterItem> allItems = linceProject.getRegister().stream()
                    .flatMap(r -> r.getRegisterData().stream())
                    .toList();
            Lince2ThemeExport exporter = new Lince2ThemeExport(allItems);
            executeModernExport(exporter::exportToString, "*.txt");
        });

        return Arrays.asList(btnExportInstrumento, btnExportRegistro);
    }

    @Override
    protected List<Object> getSelectionItems() {
        if (linceProject == null) {
            throw new IllegalStateException("LinceProject is null");
        }
        return new ArrayList<>(linceProject.getObservationTool());
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
