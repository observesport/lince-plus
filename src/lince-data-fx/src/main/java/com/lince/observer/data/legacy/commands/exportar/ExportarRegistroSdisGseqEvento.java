/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2010  Brais Gabin Moreira
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
import com.lince.observer.legacy.Registro;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * @author Brais
 */
public class ExportarRegistroSdisGseqEvento extends Command {

    private SeleccionPanel seleccionPanel;

    public ExportarRegistroSdisGseqEvento(SeleccionPanel seleccionPanel) {
        this.seleccionPanel = seleccionPanel;
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR REGISTRO"));
        putValue(Action.ACTION_COMMAND_KEY, "ExportarRegistroSdisGseqEvento");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR REGISTRO"));
    }

    @Override
    public void execute() {
        List<Criterio> criterios = seleccionPanel.getElementosSeleccionados();
        if (criterios.isEmpty()) {
            JOptionPane.showMessageDialog(null
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SELECCIONE UN CRITERIO.")
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE")
                    , JOptionPane.INFORMATION_MESSAGE);
            return;
        } else if (criterios.size() > 1) {
            JOptionPane.showMessageDialog(null
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SOLO PUEDE SELECCIONAR UN CRITERIO.")
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE")
                    , JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Registro.getInstance().getRowCount() == 0) {
            JOptionPane.showMessageDialog(null
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EL REGISTRO ACTUALMENTE ABIERTO NO TIENE NINGUNA FILA.")
                    , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE")
                    , JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //File f = PathArchivos.getPathArchivoGuardar(null, null, null, "sds");
        Platform.runLater(() -> {
                    File f = LinceDesktopFileHelper.openSaveFileDialog("*.sds");
                    if (f != null) {
                        String contenido = Registro.getInstance().exportToSdisGseqEvento(criterios);
                        ControladorArchivos.getInstance().crearArchivoDeTexto(f, contenido);
                        JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION
                                , "Gseq"
                                , "File saved ");
                    }

                }
        );
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
