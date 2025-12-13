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

import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.legacy.utiles.PathArchivos;
import com.lince.observer.legacy.Registro;

import javax.swing.*;
import java.io.File;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class SaveRegistro extends Command {

    //private InstrumentoObservacional instrumentoObservacional = null;
    public SaveRegistro() {
        initAction();
    }

    private void initAction() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("GUARDAR REGISTRO"));
        putValue(Action.ACTION_COMMAND_KEY, "GuardarRegistro");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("GUARDA EL REGISTRO"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/iconos/x16/filesave.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/iconos/x32/filesave.png")));
    }

    @Override
    public void execute() {
        Registro registro = Registro.getInstance();

        File path = registro.getPath();
        if (path == null) {
            path = PathArchivos.getPathArchivoGuardar(null, null, null, "rlince");
            if (path != null) {
                registro.setPath(path);
            } else {
                return;
            }
        }

        registro.save();
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
