package com.lince.observer.data.javafx;

import com.lince.observer.data.bean.RegisterItem;
import com.lince.observer.data.bean.categories.Criteria;
import com.lince.observer.data.export.Lince2ThemeExport;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Collections;
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
        Button btnExport = new Button(ResourceBundleHelper.getI18NLabel("EXPORTAR THEME 6"));
        btnExport.setOnAction(e -> {
            List<Criteria> criteria = linceProject.getObservationTool();
            List<RegisterItem> allItems = linceProject.getRegister().stream()
                    .flatMap(r -> r.getRegisterData().stream())
                    .toList();
            Lince2ThemeExport exporter = new Lince2ThemeExport(allItems);
            executeFixedSiblingExport(
                    "*.txt",
                    exporter::exportToString,
                    "vvt.vvt",
                    () -> Lince2ThemeExport.createVvtContent(criteria)
            );
        });
        return Collections.singletonList(btnExport);
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
