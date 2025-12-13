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
package com.lince.observer.data.legacy.commands.registro;

import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.base.ILinceAppProvider;
import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.RegistroException;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.io.File;

/**
 * @author Alberto Soto-Fernandez
 */
public class LoadRegistro extends Command {

    public LoadRegistro() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CARGAR REGISTRO"));
        putValue(Action.ACTION_COMMAND_KEY, "CargarRegistro");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CARGA UN REGISTRO DESDE UN ARCHIVO"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/iconos/x16/fileopen.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/iconos/x32/fileopen.png")));
    }

    @Override
    public void execute() {
        ILinceApp linceApp = ILinceAppProvider.getInstance();
        String label = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("REGISTRO DE LINCE")
                + " (*.rlince)";
        File f = LinceDesktopFileHelper.openSingleFileDialogBasic(linceApp, new MutablePair<>(label, "*.rlince"));
        if (f != null) {
            if (f.canRead()) {
                try {
                    Registro.loadInstance(f);
                } catch (RegistroException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Lince", JOptionPane.INFORMATION_MESSAGE);
                    JavaFXLogHelper.addLogInfo("El instrumento o registro importado contenia los siguientes errores:\n "+ ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EL ARCHIVO ") + f.getName() + java.util.ResourceBundle.getBundle("i18n.Bundle").getString(" NO EXISTE."), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE"), JOptionPane.INFORMATION_MESSAGE);
                execute();
            }
        }
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
