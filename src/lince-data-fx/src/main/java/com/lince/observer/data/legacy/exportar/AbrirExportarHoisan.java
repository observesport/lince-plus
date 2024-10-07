
/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2105 Alberto Soto Fernández
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

import com.lince.observer.data.legacy.Command;
import com.lince.observer.data.legacy.plugins.HoisanTool;
import com.lince.observer.data.legacy.utiles.ResourceBundleHelper;
import com.lince.observer.data.system.operations.LinceDesktopFileHelper;
import com.lince.observer.data.util.JavaFXLogHelper;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

/**
 * @author Alberto Soto
 *
 * 2019: Java fx behaviour
 */
public class AbrirExportarHoisan extends Command {

    public static final String EXPORT_HOISAN_COMMAND_ID = "AbrirExportarHoisan";
    private Logger log = LoggerFactory.getLogger(AbrirExportarHoisan.class.getName());

    public AbrirExportarHoisan() {
        putValue(Action.NAME, ResourceBundleHelper.getI18NLabel("HOISAN"));
        putValue(Action.ACTION_COMMAND_KEY, EXPORT_HOISAN_COMMAND_ID);
        putValue(Action.SHORT_DESCRIPTION, ResourceBundleHelper.getI18NLabel("actions.export.Hoisan"));
    }

    @Override
    public void execute() {
        File file = LinceDesktopFileHelper.openSaveFileDialog("*.mdb");
        log.warn("Init export to Hoisan file: " + file.getName() + ".");
        HoisanTool hoisan = new HoisanTool();
        hoisan.exportFile(file);
        JavaFXLogHelper.showMessage(Alert.AlertType.INFORMATION
                , "Hoisan"
                , "File saved ");

    }
}
