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
package com.lince.observer.data.legacy.commands.registro;

import com.lince.observer.data.base.EmptyLinceApp;
import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.scene.control.Alert;
import com.lince.observer.legacy.Registro;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.io.File;

/**
 * @author Brais
 */
public class SaveRegistroAs extends Command {

    public SaveRegistroAs() {
        initAction();
    }

    private void initAction() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("GUARDAR REGISTRO COMO..."));
        putValue(Action.ACTION_COMMAND_KEY, "GuardarregistroComo");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("GUARDA EL REGISTRO COMO..."));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/iconos/x16/filesaveas.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/iconos/x32/filesaveas.png")));
    }

    @Override
    public void execute() {
        Registro registro = Registro.getInstance();
        ILinceApp linceApp = new EmptyLinceApp();
        String label = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("lince.register")
                + " (*.rlince)";
        File path = LinceDesktopFileHelper.openSaveFileDialogBasic(linceApp, new MutablePair<>(label, "*.rlince"));
        if (path != null) {
            registro.setPath(path);
            registro.save();
            JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION
                    , "Lince v1.4"
                    , "File saved ");
        }
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
