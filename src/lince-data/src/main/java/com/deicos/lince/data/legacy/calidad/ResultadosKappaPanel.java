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
package com.deicos.lince.data.legacy.calidad;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Brais
 */
public class ResultadosKappaPanel extends JPanel {

    private String datos[][];

    public ResultadosKappaPanel(List<Double> kappas, List<Object> datos, double media) {
        this.datos = getContenidoTextArea(datos, kappas, media);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        TableModel tm = new DefaultTableModel(datos, new String[]{java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CRITERIO"), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("KAPPA")});
        JTable table = new JTable(tm);

        add(new JScrollPane(table), BorderLayout.CENTER);

        setMinimumSize(new Dimension(300, 300));
    }

    private String[][] getContenidoTextArea(List<Object> datos, List<Double> kappas, double media) {
        String text[][] = new String[datos.size() + 1][2];
        int i = 0;
        int tam = datos.size();
        for (i = 0; i < tam; i++) {
            text[i][0] = datos.get(i).toString();
            text[i][1] = kappas.get(i).toString();
        }
        text[i][0] = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("MEDIA");
        text[i][1] = media + "";
        return text;
    }
}
