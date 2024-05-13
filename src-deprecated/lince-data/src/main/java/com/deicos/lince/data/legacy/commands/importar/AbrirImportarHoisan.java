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
package com.deicos.lince.data.legacy.commands.importar;

import com.deicos.lince.data.base.EmptyLinceApp;
import com.deicos.lince.data.base.ILinceApp;
import com.deicos.lince.data.legacy.Command;
import com.deicos.lince.data.legacy.plugins.HoisanTool;
import com.deicos.lince.data.legacy.utiles.ResourceBundleHelper;
import com.deicos.lince.data.system.operations.LinceFileHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.swing.*;
import java.io.File;

/**
 * @author Brais
 */
public class AbrirImportarHoisan extends Command {
    private static final String IMPORT_HOISAN_COMMAND_ID = "AbrirImportarHoisan";

    public AbrirImportarHoisan() {
        putValue(Action.NAME, ResourceBundleHelper.getI18NLabel("HOISAN"));
        putValue(Action.ACTION_COMMAND_KEY, IMPORT_HOISAN_COMMAND_ID);
        putValue(Action.SHORT_DESCRIPTION, ResourceBundleHelper.getI18NLabel("actions.import.Hoisan"));
    }

    @Override
    public void execute() {
        ILinceApp linceApp = new EmptyLinceApp();
        String label = java.util.ResourceBundle.getBundle("i18n.Bundle").getString("HOISAN")
                + " (*.mdb)";
        File f = LinceFileHelper.openSingleFileDialogBasic(linceApp, new MutablePair<>(label, "*.mdb"));

        HoisanTool hoisanConnector = new HoisanTool();
        hoisanConnector.importFile(f);
    }


    @Override
    public void unExecute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
