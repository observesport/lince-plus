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

import java.util.ArrayList;
import java.util.List;

/**
 * GSEQ Multievent format exporter.
 * <p>
 * Multievent format is used for observations where multiple events can occur simultaneously.
 * Observations are separated by periods (.) except the last one which ends with semicolon (;).
 * <p>
 * Format structure:
 * <pre>
 * Multievent
 *
 * ($Criterion1 = code1 code2 ...)
 * ($Criterion2 = code3 code4 ...);
 *
 *
 * code1 code3.
 * code2 code4.
 * code1 code4;
 * /
 * </pre>
 *
 * @see GseqExporter
 * @see <a href="file:../../../../../integration/GSEQ_FORMAT_SPECIFICATION.md">GSEQ Format Specification</a>
 */
public class MultieventExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "Multievent";
    }

    @Override
    protected boolean useFormatA() {
        return true; // Multievent format uses Format A variable declarations
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        if (datosVariables.isEmpty()) {
            return contenido.append(SEQUENCE_END).append(LINE_SEPARATOR).toString();
        }

        // First pass: collect all non-empty observations
        List<String> observations = new ArrayList<>();
        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = exportFila(criterios, filaRegistro, " ");
            if (!cont.isEmpty()) {
                observations.add(cont);
            }
        }

        // Second pass: output with correct separators
        int lastObservationIndex = observations.size() - 1;
        for (int i = 0; i < observations.size(); i++) {
            contenido.append(observations.get(i));

            // Add appropriate ending: period for non-last, semicolon for last
            if (i == lastObservationIndex) {
                contenido.append(";");
            } else {
                contenido.append(".").append(LINE_SEPARATOR);
            }
        }

        // End with forward slash
        contenido.append(SEQUENCE_END).append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Multievent";
    }
}
