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
package com.deicos.lince.data.legacy.utiles;

/**
 *
 * @author Brais
 */
public class Tiempo {

    public static String formatSimpleMiliseconds(long milis) {
        String formatOther = String.format("%%0%dd", 2);
        long milisec = milis % 1000;
        String sMilisec;
        if (milisec < 10) {
            sMilisec = "00" + milisec;
        } else if (milisec < 100) {
            sMilisec = "0" + milisec;
        } else {
            sMilisec = "" + milisec;
        }
        milis /= 1000;
        long seconds = milis % 60;
        milis /= 60;
        long minutes = milis % 60;
        long hours = milis / 60;
        String ret;
        if (hours == 0) {
            ret = minutes + ":" + String.format(formatOther, seconds) + "." + sMilisec;
        } else {
            ret = hours + ":" + String.format(formatOther, minutes) + ":" + String.format(formatOther, seconds) + "." + sMilisec;
        }
        return ret;
    }

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

    public static String formatCompletMiliseconds(long milis) {
        long milisec = milis % 1000;
        String sMilisec;
        if (milisec < 10) {
            sMilisec = "00" + milisec;
        } else if (milisec < 100) {
            sMilisec = "0" + milisec;
        } else {
            sMilisec = "" + milisec;
        }
        milis /= 1000;
        long seconds = milis % 60;
        milis /= 60;
        long minutes = milis % 60;
        long hours = milis / 60;
        return hours + ":" + minutes + ":" + seconds + "," + sMilisec;
    }
}
