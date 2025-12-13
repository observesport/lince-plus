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
package com.lince.observer.data.legacy.utiles;

import javax.swing.*;
import java.util.Enumeration;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class ButtonGroupNoExclusive extends ButtonGroup {

    /**
     * The current selection.
     */
    ButtonModel selection = null;

    @Override
    public void setSelected(ButtonModel m, boolean b) {
        if (b && m != null && m != selection) {
            ButtonModel oldSelection = selection;
            selection = m;
            if (oldSelection != null) {
                oldSelection.setSelected(false);
            }
            m.setSelected(true);
        }
        if (!b && selection == m) {
            selection = null;
        }
    }

    /*
     * Desde esta funcion hasta la ultima es codigo que he tenido que replicar
     * de la implementacion de sun por culpa de que no tenia acceso desde la API
     * de ButtonGroup para modificar el valor de selection. Por lo que para
     * poder usar la potencia de ButtonGroups he tenido que hacer esta chapuza.
     */
    public void add(AbstractButton b) {
        if (b == null) {
            return;
        }
        buttons.addElement(b);

        if (b.isSelected()) {
            if (selection == null) {
                selection = b.getModel();
            } else {
                b.setSelected(false);
            }
        }

        b.getModel().setGroup(this);
    }

    public void remove(AbstractButton b) {
        if (b == null) {
            return;
        }
        buttons.removeElement(b);
        if (b.getModel() == selection) {
            selection = null;
        }
        b.getModel().setGroup(null);
    }

    public void clearSelection() {
        if (selection != null) {
            ButtonModel oldSelection = selection;
            selection = null;
            oldSelection.setSelected(false);
        }
    }

    public Enumeration<AbstractButton> getElements() {
        return buttons.elements();
    }

    public ButtonModel getSelection() {
        return selection;
    }

    public boolean isSelected(ButtonModel m) {
        return (m == selection);
    }
}
