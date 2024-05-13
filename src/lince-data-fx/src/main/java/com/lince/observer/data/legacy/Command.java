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
package com.lince.observer.data.legacy;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Brais
 */
public abstract class Command extends AbstractAction {

    private static final String keys[] = {Action.ACCELERATOR_KEY,
        Action.ACTION_COMMAND_KEY,
        Action.DEFAULT,
        Action.DISPLAYED_MNEMONIC_INDEX_KEY,
        Action.LARGE_ICON_KEY,
        Action.LARGE_ICON_KEY,
        Action.LONG_DESCRIPTION,
        Action.MNEMONIC_KEY,
        Action.MNEMONIC_KEY,
        Action.NAME,
        Action.SELECTED_KEY,
        Action.SHORT_DESCRIPTION,
        Action.SMALL_ICON
    };

    public abstract void execute();

    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final void actionPerformed(ActionEvent e) {
        execute();
    }

    @Override
    public Command clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void copiarValues(Command other) {
        for (String key : keys) {
            putValue(key, other.getValue(key));
        }
    }

    /**
     * TODO ASF: Fix this, Done to be able to compile
     * @return
     */
    protected JFrame getWindow(){
        return null;
    }
}
