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
package com.lince.observer.data.legacy.exportar;

import com.lince.observer.data.legacy.commands.exportar.ExportarRegistroSdisGseqEvento;
import com.lince.observer.data.legacy.utiles.SeleccionPanel;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Alberto Soto-Fernandez
 */
@Deprecated
public class ExportarSdisGseqEventoPanel extends JPanel {

    public ExportarSdisGseqEventoPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SeleccionPanel seleccionPanel = new SeleccionPanel(InstrumentoObservacional.getInstance().getCriterios(), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("TODOS LOS CRITERIOS"), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CRITERIOS SELECCIONADOS"));
        panelBotones.add(new JButton(new ExportarRegistroSdisGseqEvento(seleccionPanel)));
        add(seleccionPanel, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(400, 400));
    }
}
