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

import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.List;

/**
 * GSEQ State format exporter.
 * <p>
 * State format is used for state sequences where each state has a duration.
 * Durations are calculated as the time difference until the next state.
 * <p>
 * Format structure:
 * <pre>
 * State
 *
 * ($Criterion1 = code1 code2 ...)
 * ($Criterion2 = code3 code4 ...);
 *
 *
 * code1=1000  code3=500
 * code2=1500  code4=2000
 * /
 * </pre>
 *
 * @see GseqExporter
 * @see <a href="file:../../../../../integration/GSEQ_FORMAT_SPECIFICATION.md">GSEQ Format Specification</a>
 */
public class StateExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "State";
    }

    @Override
    protected boolean useFormatA() {
        return true; // State format uses Format A variable declarations
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        FilaRegistro filaAnterior = null;
        int tiempoAnterior = -1;

        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = exportFila(criterios, filaRegistro, "+");

            // If previous row exists, calculate duration and replace placeholders
            if (filaAnterior != null) {
                int duracion = filaRegistro.getMilis() - tiempoAnterior;
                // Replace all + placeholders with =duration\t, then append =duration at end
                String contentStr = contenido.toString();
                contentStr = contentStr.replaceAll("\\+", "=" + duracion + "\t");
                contenido = new StringBuilder(contentStr);
                contenido.append("=").append(duracion);
            }

            contenido.append(LINE_SEPARATOR).append(cont);
            filaAnterior = filaRegistro;
            tiempoAnterior = filaRegistro.getMilis();
        }

        // Replace remaining placeholders with default duration of 1
        String contentStr = contenido.toString();
        contentStr = contentStr.replaceAll("\\+", "=1\t");
        contenido = new StringBuilder(contentStr);

        contenido.append(SEQUENCE_END).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "State";
    }
}
