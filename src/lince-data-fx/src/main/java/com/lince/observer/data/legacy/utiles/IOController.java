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
package com.lince.observer.data.legacy.utiles;

import java.io.File;

/**
 *
 * @author Brais
 */
public class IOController {

    public static String pathAppdata() {
        File f = new File(System.getenv("APPDATA"));
        if (f.exists()) {
            f = new File(f.getAbsolutePath() + "\\Lince");
            if (!f.exists()) {
                f.mkdir();
            }
            return f.getAbsolutePath() + "\\";
        }
        return "";
    }
}
