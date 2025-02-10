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
package com.lince.observer.data.legacy.commands.instrumento;

import com.lince.observer.data.base.EmptyLinceApp;
import com.lince.observer.data.base.ILinceApp;
import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.Registro;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Brais
 */
public class LoadInstrumentoObservacional extends Command {

    public LoadInstrumentoObservacional() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CARGAR INSTR. OBSERVACIONAL"));
        putValue(Action.ACTION_COMMAND_KEY, "CargarInstrumentObservacional");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CARGA UN INSTRUMENTO OBSERVACIONAL DESDE UN ARCHIVO"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/iconos/x16/fileopen.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/iconos/x32/fileopen.png")));
    }

    @Override
    public void execute() {
        ILinceApp linceApp = new EmptyLinceApp();
        String label = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("INSTRUMENTO OBSERVACIONAL DE LINCE")
                + " (*.ilince)";
        File f = LinceDesktopFileHelper.openSingleFileDialogBasic(linceApp, new MutablePair<>(label, "*.ilince"));
        if (f != null) {
            if (f.canRead()) {
                try {
                    InstrumentoObservacional.loadInstance(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Registro.loadNewInstance();
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
