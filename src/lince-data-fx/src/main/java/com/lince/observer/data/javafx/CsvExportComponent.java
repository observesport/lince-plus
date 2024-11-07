package com.lince.observer.data.javafx;

import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Alberto Soto. 7/11/24
 */
public class CsvExportComponent extends BorderPane {

    public CsvExportComponent() {
        initComponents();
    }

    private void initComponents() {
        HBox panelBotones = new HBox(10);
        panelBotones.setAlignment(Pos.CENTER_RIGHT);
        panelBotones.setPadding(new Insets(10));

        List<Object> criterios = Arrays.asList(InstrumentoObservacional.getInstance().getCriterios());
        List<String> otros = Arrays.asList(
                LinceDataConstants.COL_TFRAMES, LinceDataConstants.COL_DURACION_FR,
                LinceDataConstants.COL_TSEGUNDOS, LinceDataConstants.COL_DURACION_SEC,
                LinceDataConstants.COL_TMILISEGUNDOS, LinceDataConstants.COL_DURACION_MS
        );
        List<Object> datosMixtos = Arrays.asList(InstrumentoObservacional.getInstance().getDatosMixtos());
        List<Object> datosFijos = Arrays.asList(InstrumentoObservacional.getInstance().getDatosFijos());

        List<Object> lista = new ArrayList<>();
        lista.addAll(otros);
        lista.addAll(criterios);
        lista.addAll(datosMixtos);
        lista.addAll(datosFijos);

        SelectionPanelComponent selectionPanelComponent = new SelectionPanelComponent(
                lista.toArray(),
                ResourceBundleHelper.getI18NLabel("ALL_CRITERIA"),
                ResourceBundleHelper.getI18NLabel("SELECTED_CRITERIA")
        );
        setCenter(selectionPanelComponent);

        Button btnExportWithComma = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_CSV_COMMA"));
        btnExportWithComma.setOnAction(e -> new CsvExportRegisterCommand(selectionPanelComponent, true).execute());

        Button btnExportWithSemicolon = new Button(ResourceBundleHelper.getI18NLabel("EXPORT_CSV_SEMICOLON"));
        btnExportWithSemicolon.setOnAction(e -> new CsvExportRegisterCommand(selectionPanelComponent, false).execute());

        panelBotones.getChildren().addAll(btnExportWithComma, btnExportWithSemicolon);

        setBottom(panelBotones);

        setMinWidth(550);
        setMinHeight(400);
    }
}
