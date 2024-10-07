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

import com.lince.observer.data.legacy.Command;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.Registro;

import javax.swing.*;

/**
 *
 * @author Brais
 */
public class NewInstrumentoObservacional extends Command {

    public NewInstrumentoObservacional() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("NUEVO INSTR. OBSERVACIONAL"));
        putValue(Action.ACTION_COMMAND_KEY, "NuevoInstrumentObservacional");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CREAR UN NUEVO INSTRUMENTO OBSERVACIONAL"));
        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/iconos/x16/filenew.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/iconos/x32/filenew.png")));
    }

    @Override
    public void execute() {
        /*NecesarioGuardar necesarioGuardar = NecesarioGuardar.getInstance();
        boolean continuar = necesarioGuardar.saveInstrumentObservacional();
        if (continuar) {
            continuar = necesarioGuardar.saveRegistro();
            if (continuar) {
            */
                InstrumentoObservacional.loadNewInstance();
                Registro.loadNewInstance();
            //}
        //}
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
