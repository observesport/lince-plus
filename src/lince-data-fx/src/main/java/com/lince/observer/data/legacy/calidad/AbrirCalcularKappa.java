/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2011  Brais Gabin Moreira
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
package com.lince.observer.data.legacy.calidad;

import com.lince.observer.data.legacy.Command;

import javax.swing.*;


/**
 *
 * @author Brais
 */
public class AbrirCalcularKappa extends Command {

    public AbrirCalcularKappa() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("KAPPA"));
        putValue(Action.ACTION_COMMAND_KEY, "AbrirExportarSdisGseqEventoConTiempo");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR A SDIS-GSEQ EVENTO CON TIEMPO"));
    }

    @Override
    public void execute() {
        JDialog calidadDelDato = new JDialog(getWindow(), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CALIDAD DEL DATO"), JDialog.DEFAULT_MODALITY_TYPE);
        JPanel mainPanel = new CalidadDelDatoMainPanel();
        calidadDelDato.setMinimumSize(mainPanel.getMinimumSize());
        calidadDelDato.setMaximumSize(mainPanel.getMaximumSize());
        calidadDelDato.setPreferredSize(mainPanel.getPreferredSize());
        calidadDelDato.setContentPane(mainPanel);
        calidadDelDato.setVisible(true);
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
