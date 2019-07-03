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
package com.deicos.lince.data.legacy.commands.registro;

import com.deicos.lince.data.base.EmptyLinceApp;
import com.deicos.lince.data.base.ILinceApp;
import com.deicos.lince.data.legacy.Command;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import lince.modelo.Registro;
import lince.modelo.RegistroException;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.io.File;

/**
 * @author Brais
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
        ILinceApp linceApp = new EmptyLinceApp();
        String label = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("REGISTRO DE LINCE")
                + " (*.rlince)";
        File f = LinceFileHelper.openSingleFileDialogBasic(linceApp, new MutablePair<>(label, "*.rlince"));
        if (f != null) {
            if (f.canRead()) {
                try {
                    Registro.loadInstance(f);
                } catch (RegistroException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Lince", JOptionPane.INFORMATION_MESSAGE);
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
