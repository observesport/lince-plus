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
package com.deicos.lince.data.legacy.exportar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.deicos.lince.data.LinceDataConstants;
import com.deicos.lince.data.legacy.commands.exportar.ExportarRegistroCsv;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import com.deicos.lince.data.legacy.utiles.SeleccionPanel;

/**
 *
 * @author Brais
 */
public class ExportarCsvPanel extends JPanel {

    public ExportarCsvPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Object criterios[] = InstrumentoObservacional.getInstance().getCriterios();
        String otros[] = {LinceDataConstants.COL_TFRAMES, LinceDataConstants.COL_DURACION_FR, LinceDataConstants.COL_TSEGUNDOS, LinceDataConstants.COL_DURACION_SEC, LinceDataConstants.COL_TMILISEGUNDOS, LinceDataConstants.COL_DURACION_MS};
        //String otros[] = {"TFrames", "DuracionFr", "TSegundos", "DuracionSeg", "TMilisegundos", "DuracionMiliseg"};
        Object datosMixtos[] = InstrumentoObservacional.getInstance().getDatosMixtos();
        Object datosFijos[] = InstrumentoObservacional.getInstance().getDatosFijos();
        Object lista[] = new Object[criterios.length + otros.length + datosMixtos.length + datosFijos.length];
        System.arraycopy(otros, 0, lista, 0, otros.length);
        System.arraycopy(criterios, 0, lista, otros.length, criterios.length);
        System.arraycopy(datosMixtos, 0, lista, otros.length + criterios.length, datosMixtos.length);
        System.arraycopy(datosFijos, 0, lista, otros.length + criterios.length + datosMixtos.length, datosFijos.length);
        SeleccionPanel seleccionPanel = new SeleccionPanel(lista
                , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("TODOS LOS CRITERIOS")
                , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CRITERIOS SELECCIONADOS"));
        panelBotones.add(new JButton(new ExportarRegistroCsv(seleccionPanel,true)));
        panelBotones.add(new JButton(new ExportarRegistroCsv(seleccionPanel,false)));
        add(seleccionPanel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(550, 400));
    }
}
