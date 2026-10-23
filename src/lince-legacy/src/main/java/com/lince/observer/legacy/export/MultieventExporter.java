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
 * Each observation row produces one line with space-separated codes from all criteria.
 * Lines end with {@code .} except the last in a sequence which ends with {@code ;} or {@code /}.
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
 * code1 code4/
 * </pre>
 * <p>
 * All observations form a single sequence ending with {@code /}.
 *
 * @see GseqExporter
 */
public class MultieventExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "Multievent";
    }

    @Override
    protected boolean useFormatA() {
        return true;
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        if (datosVariables.isEmpty()) {
            return contenido.append(SEQUENCE_END).append(LINE_SEPARATOR).toString();
        }

        // Collect all non-empty observations
        List<String> observations = new ArrayList<>();
        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = exportFila(criterios, filaRegistro, " ");
            if (!cont.isEmpty()) {
                observations.add(cont);
            }
        }

        // Output as single sequence: lines end with "." except last which ends with "/"
        int lastIndex = observations.size() - 1;
        for (int i = 0; i < observations.size(); i++) {
            contenido.append(observations.get(i));

            if (i == lastIndex) {
                contenido.append(SEQUENCE_END);
            } else {
                contenido.append(".");
            }
            contenido.append(LINE_SEPARATOR);
        }

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Multievent";
    }
}
