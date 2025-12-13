/*
 *  LINCE PLUS - Automatizacion de datos observacionales. Inherited from legacy Lince 1.2.
 *  Copyright (C) 2025  Alberto Soto-Fernandez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lince.observer.data.legacy.commands.exportar;

import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.legacy.datos.ControladorArchivos;
import com.lince.observer.data.legacy.utiles.SeleccionPanel;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 *
 * @author Alberto Soto-Fernandez
 */
@Deprecated
public class ExportarInstrumentoObservacionalTheme extends Command {

    private SeleccionPanel seleccionPanel;

    public ExportarInstrumentoObservacionalTheme(SeleccionPanel seleccionPanel) {
        this.seleccionPanel = seleccionPanel;
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR INSTRUMENTO OBSERVACIONAL"));
        putValue(Action.ACTION_COMMAND_KEY, "ExportarInstrumentoObservacionalTheme");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR INSTRUMENTO OBSERVACIONAL"));
    }

    @Override
    public void execute() {
        List<Criterio> criterios = seleccionPanel.getElementosSeleccionados();
        if (criterios.isEmpty()) {
            JOptionPane.showMessageDialog(null
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SELECCIONE AL MENOS UN CRITERIO.")
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE")
                    , JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        //File f = PathArchivos.getPathArchivoGuardar(null, null, null, "vvt");
        Platform.runLater(() -> {
            File f = LinceDesktopFileHelper.openSaveFileDialog("*.vvt");
            if (f != null) {
                String contenido = InstrumentoObservacional.getInstance().exportToTheme(criterios);
                ControladorArchivos.getInstance().crearArchivoDeTexto(f, contenido);
                JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION
                        , "Theme"
                        , "File saved ");
            }

        });

    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
