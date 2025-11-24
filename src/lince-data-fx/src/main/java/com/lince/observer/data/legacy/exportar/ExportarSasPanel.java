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
package com.lince.observer.data.legacy.exportar;

import com.lince.observer.data.legacy.commands.exportar.ExportarRegistroSas;
import com.lince.observer.data.legacy.utiles.SeleccionPanel;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.Registro;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Alberto Soto-Fernandez
 */
@Deprecated
public class ExportarSasPanel extends JPanel {

    public ExportarSasPanel() {
        initComponents();
    }

    private void initComponents() {
        Registro.getInstance();
        InstrumentoObservacional.getInstance();
        setLayout(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Object datosFijos[] = InstrumentoObservacional.getInstance().getDatosFijos();
        Object datosMixtos[] = InstrumentoObservacional.getInstance().getDatosMixtos();
        Object criterios[] = InstrumentoObservacional.getInstance().getCriterios();
        Object lista[] = new Object[datosFijos.length + datosMixtos.length + criterios.length];
        System.arraycopy(datosFijos, 0, lista, 0, datosFijos.length);
        System.arraycopy(datosMixtos, 0, lista, datosFijos.length, datosMixtos.length);
        System.arraycopy(criterios, 0, lista, datosFijos.length + datosMixtos.length, criterios.length);
        SeleccionPanel seleccionPanel = new SeleccionPanel(lista
                , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("TODOS LOS CRITERIOS")
                , java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CRITERIOS SELECCIONADOS"));
        panelBotones.add(new JButton(new ExportarRegistroSas(seleccionPanel)));
        add(seleccionPanel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(400, 400));
    }
}
