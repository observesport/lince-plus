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
package com.lince.observer.data.legacy.utiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brais
 */
public class SeleccionPanel extends JPanel { //Dialogpane es la nueva version

    private Object objetos[];
    private String name1 = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("DESCARTADOS");
    private String name2 = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("INCLUIDOS");
    private JList aceptados;
    private JList noAceptados;
    private JButton acept;
    private JButton aceptAll;
    private JButton refuse;
    private JButton refuseAll;
    private JButton up;
    private JButton down;

    public SeleccionPanel(Object[] objetos) {
        this.objetos = objetos;
        initComponents();
        lookAndFeel();
    }

    public SeleccionPanel(Object[] objetos, String name1, String name2) {
        this.objetos = objetos;
        this.name1 = name1;
        this.name2 = name2;
        initComponents();
        lookAndFeel();
    }

    private void initComponents() {
        aceptados = new JList(new DefaultComboBoxModel());
        noAceptados = new JList(new DefaultComboBoxModel(objetos));
        noAceptados.addMouseListener(new MouseAdapterDoubleList(noAceptados, aceptados));
        noAceptados.addKeyListener(new KeyAdapterDoubleList(noAceptados, aceptados));
        aceptados.addMouseListener(new MouseAdapterDoubleList(aceptados, noAceptados));
        aceptados.addKeyListener(new KeyAdapterDoubleList(aceptados, noAceptados));

        acept = new JButton(new ActionSingle(noAceptados, aceptados));
        aceptAll = new JButton(new ActionAll(noAceptados, aceptados));
        refuse = new JButton(new ActionSingle(aceptados, noAceptados));
        refuseAll = new JButton(new ActionAll(aceptados, noAceptados));
        up = new JButton(new MoveIntoJList(aceptados, 1));
        down = new JButton(new MoveIntoJList(aceptados, -1));
    }

    private void lookAndFeel() {
        setLayout(new GridBagLayout());

        lookAndFeelButtons();

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(new JScrollPane(noAceptados, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), constraints);

        constraints.gridx = 2;
        add(new JScrollPane(aceptados, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), constraints);

        JPanel auxPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        auxPanel.setMinimumSize(new Dimension(82, 120));
        auxPanel.setPreferredSize(new Dimension(82, 120));
        auxPanel.add(acept);
        auxPanel.add(aceptAll);
        auxPanel.add(refuse);
        auxPanel.add(refuseAll);

        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.gridx = 1;
        add(auxPanel, constraints);

        auxPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        auxPanel.setMinimumSize(new Dimension(82, 60));
        auxPanel.setPreferredSize(new Dimension(82, 60));
        auxPanel.add(up);
        auxPanel.add(down);

        constraints.gridx = 3;
        add(auxPanel, constraints);

        constraints.anchor = GridBagConstraints.FIRST_LINE_START;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(new JLabel(name1), constraints);

        constraints.gridx = 2;
        add(new JLabel(name2), constraints);
    }

    private void lookAndFeelButtons() {

        down.setText(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("BAJAR"));
        down.setToolTipText(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("BAJA UNA POSICIÓN EL ELEMENTO SELECCIONADO."));
        down.setIcon(new ImageIcon(getClass().getResource("/iconos/x16/go-down.png")));

        up.setText(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SUBIR"));
        up.setToolTipText(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SUBE UNA POSICIÓN EL ELEMENTO SELECCIONADO."));
        up.setIcon(new ImageIcon(getClass().getResource("/iconos/x16/go-up.png")));

        acept.setText(">");

        aceptAll.setText(">>");

        refuse.setText("<");

        refuseAll.setText("<<");
    }

    public List getElementosSeleccionados() {
        return allElementsOf(((DefaultComboBoxModel) aceptados.getModel()));
    }

    public List getElementosNoSeleccionados() {
        return allElementsOf(((DefaultComboBoxModel) noAceptados.getModel()));
    }

    private void cambiarDeLista(JList origen, JList destino) {
        Object objects[] = origen.getSelectedValues();
        DefaultComboBoxModel oModel = (DefaultComboBoxModel) origen.getModel();
        DefaultComboBoxModel dModel = (DefaultComboBoxModel) destino.getModel();
        for (Object object : objects) {
            oModel.removeElement(object);
            dModel.addElement(object);
        }
    }

    private void pasarTodoALaLista(JList origen, JList destino) {
        origen.setModel(new DefaultComboBoxModel());
        DefaultComboBoxModel mDestino = new DefaultComboBoxModel();
        for (Object object : objetos) {
            mDestino.addElement(object);
        }
        destino.setModel(mDestino);
    }

    private void moverDeLaLista(JList list, int move) {
        int i = list.getSelectedIndex();
        DefaultComboBoxModel defaultListModel = (DefaultComboBoxModel) list.getModel();
        int size = defaultListModel.getSize();
        int iDestino = i + move;
        if (iDestino >= 0 && iDestino < size) {
            Object aux = defaultListModel.getElementAt(i);
            defaultListModel.removeElementAt(i);
            defaultListModel.insertElementAt(aux, iDestino);
            list.setSelectedIndex(iDestino);
        }
    }

    private List allElementsOf(DefaultComboBoxModel defaultComboBoxModel) {
        int size = defaultComboBoxModel.getSize();
        List list = new ArrayList();
        for (int i = 0; i < size; i++) {
            list.add(defaultComboBoxModel.getElementAt(i));
        }
        return list;
    }

    private class MouseAdapterDoubleList extends MouseAdapter {

        private JList origen;
        private JList destino;

        public MouseAdapterDoubleList(JList origen, JList destino) {
            this.origen = origen;
            this.destino = destino;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                cambiarDeLista(origen, destino);
            }
        }
    }

    private class KeyAdapterDoubleList extends KeyAdapter {

        private JList origen;
        private JList destino;

        public KeyAdapterDoubleList(JList origen, JList destino) {
            this.origen = origen;
            this.destino = destino;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                cambiarDeLista(origen, destino);
            }
        }
    }

    private class ActionSingle extends AbstractAction {

        private JList origen;
        private JList destino;

        public ActionSingle(JList origen, JList destino) {
            this.origen = origen;
            this.destino = destino;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            cambiarDeLista(origen, destino);
        }
    }

    private class ActionAll extends AbstractAction {

        private JList origen;
        private JList destino;

        public ActionAll(JList origen, JList destino) {
            this.origen = origen;
            this.destino = destino;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pasarTodoALaLista(origen, destino);
        }
    }

    private class MoveIntoJList extends AbstractAction {

        private JList list;
        private int move;

        public MoveIntoJList(JList list, int move) {
            this.list = list;
            this.move = -move;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            moverDeLaLista(list, move);
        }
    }
}
