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
package com.lince.observer.data.legacy.commands.instrumento;

import com.lince.observer.data.legacy.Command;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class RemoveCriterio extends Command {

    private DefaultMutableTreeNode node;

    public RemoveCriterio(DefaultMutableTreeNode node) {
        this.node = node;
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINAR CRITERIO"));
        putValue(Action.ACTION_COMMAND_KEY, "EliminarCriterio");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINA EL CRITERIO SELECCIONADO."));
    }

    @Override
    public void execute() {
        int respuesta = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("¿ESTA SEGURO QUE DESEA BORRAR ESTE CRITERIO Y TODAS LAS CATEGORIAS QUE CUELGAN DE ÉL?"), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINAR CRITERIO"), JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        InstrumentoObservacional.getInstance().removeNodo(node);
    }

    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
