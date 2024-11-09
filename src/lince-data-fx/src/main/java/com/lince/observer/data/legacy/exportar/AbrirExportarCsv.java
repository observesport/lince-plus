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
package com.lince.observer.data.legacy.exportar;

import com.lince.observer.data.legacy.Command;

import javax.swing.*;

/**
 *
 * @author Brais
 */
@Deprecated
public class AbrirExportarCsv extends Command {

    public AbrirExportarCsv() {
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXCEL"));
        putValue(Action.ACTION_COMMAND_KEY, "AbrirExportarCsv");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR A CSV, FORMATO COMPATIBLE CON EXCEL"));
    }

    @Override
    public void execute() {
        JDialog dialog = new JDialog(getWindow(), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("EXPORTAR EXCEL"), JDialog.DEFAULT_MODALITY_TYPE);
        JPanel mainPanel = new ExportarCsvPanel();
        dialog.setMinimumSize(mainPanel.getMinimumSize());
        dialog.setMaximumSize(mainPanel.getMaximumSize());
        dialog.setPreferredSize(mainPanel.getPreferredSize());
        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }
}
