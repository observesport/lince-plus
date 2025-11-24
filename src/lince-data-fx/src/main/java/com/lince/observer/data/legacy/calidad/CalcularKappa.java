/*
 *  LINCE PLUS - Automatizacion de datos observacionales. Inherited from legacy Lince 1.2.
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

import com.lince.observer.data.LegacyStatsHelper;
import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.legacy.commands.registro.LoadRegistro;
import com.lince.observer.data.legacy.utiles.FiltroArchivos;
import com.lince.observer.data.legacy.utiles.PathArchivos;
import com.lince.observer.data.legacy.utiles.SeleccionPanel;
import com.lince.observer.legacy.Registro;
import com.lince.observer.legacy.RegistroException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alberto Soto-Fernandez
 */
public class CalcularKappa extends Command {

    private SeleccionPanel seleccionPanel;

    public CalcularKappa(SeleccionPanel seleccionPanel) {
        this.seleccionPanel = seleccionPanel;
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CALCULAR KAPPA"));
        putValue(Action.ACTION_COMMAND_KEY, "CalcularKappa");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CALCULA KAPPA DE LOS PARAMETROS."));
    }

    @Override
    public void execute() {
        List<Object> datos = seleccionPanel.getElementosSeleccionados();
        if (datos.isEmpty()) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SELECCIONE AL MENOS UN CRITERIO."), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("LINCE"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<FileFilter> fileFilters = new ArrayList<FileFilter>();
        fileFilters.add(new FiltroArchivos("rlince", java.util.ResourceBundle.getBundle("i18n.Bundle").getString("REGISTRO DE LINCE")));
        if (Registro.getInstance().getRowCount() == 0) {
            new LoadRegistro().execute();
        }
        File otroRegistroFile = PathArchivos.getPathArchivoAbrir(fileFilters, null, null);

        if (otroRegistroFile != null) {
            try {
                Registro register = Registro.getInstance();
                List<Double> kappas = LegacyStatsHelper.calcularKappa(register, otroRegistroFile, datos);
                double media = LegacyStatsHelper.calcularMedia(kappas);
                mostrarValores(kappas, datos, media);
            } catch (RegistroException ex) {
                Logger.getLogger(CalcularKappa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void mostrarValores(List<Double> kappas, List<Object> datos, double media) {
        JDialog dialogExportarTheme = new JDialog(getWindow(), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CÁLCULO KAPPA"), JDialog.DEFAULT_MODALITY_TYPE);

        JPanel mainPanel = new ResultadosKappaPanel(kappas, datos, media);

        dialogExportarTheme.setMinimumSize(mainPanel.getMinimumSize());
        dialogExportarTheme.setMaximumSize(mainPanel.getMaximumSize());
        dialogExportarTheme.setPreferredSize(mainPanel.getPreferredSize());

        dialogExportarTheme.setContentPane(mainPanel);

        dialogExportarTheme.setVisible(true);
    }


}
