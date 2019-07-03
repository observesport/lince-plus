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

package com.deicos.lince.data.legacy.commands.instrumento;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import com.deicos.lince.data.legacy.Command;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;

/**
 *
 * @author Brais
 */
public class RemoveCategoria extends Command {

    private DefaultMutableTreeNode node;

    public RemoveCategoria(DefaultMutableTreeNode node) {
        this.node = node;
        putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINAR CATEGORIA"));
        putValue(Action.ACTION_COMMAND_KEY, "EliminarCategoria");
        putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINA LA CATEGORIA SELECCIONADA."));
    }

    @Override
    public void execute() {
        int respuesta = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("¿ESTA SEGURO QUE DESEA BORRAR ESTA CATEGORIA Y TODAS LAS CATEGORIAS QUE CUELGAN DE ELLA?"), java.util.ResourceBundle.getBundle("i18n.Bundle").getString("ELIMINAR CATEGORIA"), JOptionPane.YES_NO_OPTION);
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
