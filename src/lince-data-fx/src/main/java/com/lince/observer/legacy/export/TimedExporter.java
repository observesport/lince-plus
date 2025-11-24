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
package com.lince.observer.legacy.export;

import com.lince.observer.data.legacy.utiles.Tiempo;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.List;

/**
 * GSEQ Timed Event format exporter.
 * <p>
 * Timed format is used for events with time ranges (start-end times).
 * Each event includes time information in MM:SS format.
 * <p>
 * Format structure:
 * <pre>
 * Timed
 *
 * ($Criterion1 = code1 code2 ...)
 * ($Criterion2 = code3 code4 ...);
 *
 *
 * code1+code3,0:00-0:10
 * code2+code4,0:10-0:20
 * /
 * </pre>
 *
 * @see GseqExporter
 * @see <a href="file:../../../../../integration/GSEQ_FORMAT_SPECIFICATION.md">GSEQ Format Specification</a>
 */
public class TimedExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "Timed";
    }

    @Override
    protected boolean useFormatA() {
        return true; // Timed format uses Format A variable declarations
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        /*
         * TODO: Verify if time format should always be m:ss
         *  - Check if milliseconds are supported
         *  - Check if hours are supported
         */

        StringBuilder contenido = new StringBuilder();

        FilaRegistro filaAnterior = null;
        String tiempo = "";

        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = exportFila(criterios, filaRegistro, "+");

            // If previous row exists, complete the previous line with end time
            if (filaAnterior != null) {
                contenido.append("-").append(Tiempo.formatSimpleSeconds(filaRegistro.getMilis()));
            }

            tiempo = Tiempo.formatSimpleSeconds(filaRegistro.getMilis());
            contenido.append(LINE_SEPARATOR).append(cont).append(",").append(tiempo);

            filaAnterior = filaRegistro;
        }

        // Complete the last time range
        if (tiempo != null) {
            contenido.append("-").append(tiempo);
        }

        contenido.append(SEQUENCE_END).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Timed";
    }
}
