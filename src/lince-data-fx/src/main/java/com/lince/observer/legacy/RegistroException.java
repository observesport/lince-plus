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
package com.lince.observer.legacy;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class RegistroException extends Exception {

    /**
     * Creates a new instance of <code>RegistroException</code> without detail message.
     */
    public RegistroException() {
    }

    /**
     * Constructs an instance of <code>RegistroException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RegistroException(String msg) {
        super(msg);
    }
}
