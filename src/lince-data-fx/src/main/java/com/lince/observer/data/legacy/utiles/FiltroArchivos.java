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

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class FiltroArchivos extends FileFilter {

    private List<String> extensiones;
    private String descripcion;

    public FiltroArchivos(String extension, String descripcion) {
        this.extensiones = new ArrayList<String>();
        this.extensiones.add(extension);
        this.descripcion = descripcion;
    }

    public FiltroArchivos(List<String> extensiones, String descripcion) {
        this.extensiones = extensiones;
        this.descripcion = descripcion;
    }

    @Override
    public boolean accept(File f) {
        if(f.isDirectory()){
            return true;
        }
        boolean aceptado = false;

        String ext = f.getName();
        int index = ext.lastIndexOf(".");
        if (index > 0) {
            ext = ext.substring(index + 1);
            for (String extension : extensiones) {
                if (ext.equalsIgnoreCase(extension)) {
                    aceptado = true;
                    break;
                }
            }
        }

        return aceptado;
    }

    @Override
    public String getDescription() {
        return descripcion;
    }
}
