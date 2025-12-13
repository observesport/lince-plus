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
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public abstract class PathArchivos {

    private static File lastPath = null;

    public static File getPathArchivoGuardar(List<FileFilter> fileFilters, File directorioPredeterminado, Component parent, String extension) {
        if (directorioPredeterminado == null) {
            directorioPredeterminado = lastPath;
        }
        JFileChooser fileChooser = getJFileChooser(fileFilters, directorioPredeterminado);
        int valor = fileChooser.showSaveDialog(parent);
        File f = null;
        if (valor == JFileChooser.APPROVE_OPTION) {
            f = fileChooser.getSelectedFile();
            f = setExtension(f, extension);
            if (f.exists()) {
                int respuesta = JOptionPane.showConfirmDialog(null
                        ,ResourceBundleHelper.getI18NLabel("i18n.legacy","confirm_overwrite")
                        ,ResourceBundleHelper.getI18NLabel("i18n.legacy","overwrite_file")
                        //, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("¿ESTA SEGURO QUE DESEA SOBREESCRIBIR EL ARCHIVO \"") + f.getName() + java.util.ResourceBundle.getBundle("i18n.Bundle").getString("\"?")
                        //, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SOBREESCRIBIR ARCHIVO")
                        , JOptionPane.YES_NO_OPTION);
                if (respuesta != JOptionPane.YES_OPTION) {
                    f = getPathArchivoGuardar(fileFilters, f, parent, extension);
                }
            }
        }
        if (f != null) {
            lastPath = f.getParentFile();
        }
        return f;
    }

    /**
     * Does not work on mac OS
     *
     * @param fileFilters
     * @param directorioPredeterminado
     * @param parent
     * @return
     */
    @Deprecated
    public static File getPathArchivoAbrir(List<FileFilter> fileFilters, File directorioPredeterminado, Component parent) {
        if (directorioPredeterminado == null) {
            directorioPredeterminado = lastPath;
        }
        JFileChooser fileChooser = getJFileChooser(fileFilters, directorioPredeterminado);
        int valor = fileChooser.showOpenDialog(parent);
        File f = (valor == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
        if (f != null) {
            lastPath = f.getParentFile();
        }
        return f;
    }

    /**
     * Does not work on mac OS
     *
     * @param fileFilters
     * @param directorioPredeterminado
     * @return
     */
    @Deprecated
    private static JFileChooser getJFileChooser(List<FileFilter> fileFilters, File directorioPredeterminado) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        if (directorioPredeterminado != null) {
            fileChooser.setCurrentDirectory(directorioPredeterminado);
        }
        if (fileFilters != null) {
            if (fileFilters.size() > 0) {
                for (FileFilter ff : fileFilters) {
                    fileChooser.addChoosableFileFilter(ff);
                }
                fileChooser.setFileFilter(fileFilters.get(0));
            }
        }
        return fileChooser;
    }

    private static File setExtension(File f, String extension) {
        File file;
        String name = f.getName();
        int index = name.lastIndexOf(".");
        if (index < 0) {
            file = new File(f.getAbsolutePath() + "." + extension);
        } else {
            if (extension.equalsIgnoreCase(name.substring(index + 1))) {
                file = f;
            } else {
                file = new File(f.getAbsolutePath() + "." + extension);
            }
        }

        return file;
    }
}
