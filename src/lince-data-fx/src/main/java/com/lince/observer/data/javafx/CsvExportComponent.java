package com.lince.observer.data.javafx;

import com.lince.observer.data.component.ILinceFileExporter;
import com.lince.observer.data.component.LinceCsvExporter;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Alberto Soto. 7/11/24
 *
 * @see LinceCsvExporter
 */
public class CsvExportComponent extends GenericExportComponent {

    protected CsvExportComponent(UUID observerId) {
        super(observerId);
    }

    @Override
    protected List<Node> getActions(SelectionPanelComponent selectionPanelComponent) {
        Button btnExportWithComma = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_CSV_COMMA"));
        btnExportWithComma.setOnAction(e -> new CsvExportRegisterCommand(selectionPanelComponent, true, getLinceProject(), getResearchUUID()).execute());
        Button btnExportWithSemicolon = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_CSV_SEMICOLON"));
        btnExportWithSemicolon.setOnAction(e -> new CsvExportRegisterCommand(selectionPanelComponent, false, getLinceProject(), getResearchUUID()).execute());
        return Arrays.asList(btnExportWithComma, btnExportWithSemicolon);
    }

    @Override
    protected List<Object> getSelectionItems() {
        if (linceProject == null) {
            throw new IllegalStateException("LinceProject is null");
        }
        ILinceFileExporter linceCsvExporter = new LinceCsvExporter();
        return new ArrayList<>(linceCsvExporter.getDefaultColumnDefinitions(linceProject));
    }

    @Override
    String getFileExtension() {
        return "csv";
    }

    @Override
    String getExportTitle() {
        return "Csv file";
    }
}
