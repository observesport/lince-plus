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

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class Tiempo {

    public static String formatSimpleSeconds(long milis) {
        String formatOther = String.format("%%0%dd", 2);
        milis = milis / 1000;
        long seconds = milis % 60;
        milis /= 60;
        long minutes = milis % 60;
        long hours = milis / 60;
        String ret;
        if (hours == 0) {
            ret = minutes + ":" + String.format(formatOther, seconds);
        } else {
            ret = hours + ":" + String.format(formatOther, minutes) + ":" + String.format(formatOther, seconds);
        }
        return ret;
    }

}
