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
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.ArrayList;
import java.util.List;

/**
 * GSEQ Event format exporter.
 * <p>
 * Event format is used for sequential observations without timing information.
 * Each observation row becomes its own sequence. Within each sequence,
 * each criterion's code appears on its own line.
 * <p>
 * Format structure:
 * <pre>
 * Event
 *
 * ($CriterionName = code1 code2 ...);
 *
 *
 * code1
 * code2;
 *
 * code3
 * code4/
 * </pre>
 * <p>
 * Sequences end with {@code ;} except the last which ends with {@code /}.
 * Blank lines separate sequences.
 *
 * @see GseqExporter
 */
public class EventExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "Event";
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

        // Collect non-empty observations as sequences
        // Each observation row is one sequence; within it, each code is on its own line
        List<List<String>> sequences = new ArrayList<>();
        for (FilaRegistro filaRegistro : datosVariables) {
            List<String> codes = new ArrayList<>();
            for (Criterio criterio : criterios) {
                Categoria categoria = filaRegistro.getCategoria(criterio);
                if (categoria != null) {
                    codes.add(categoria.getCodigo());
                }
            }
            if (!codes.isEmpty()) {
                sequences.add(codes);
            }
        }

        int lastSeqIndex = sequences.size() - 1;
        for (int s = 0; s < sequences.size(); s++) {
            List<String> codes = sequences.get(s);
            int lastCodeIndex = codes.size() - 1;

            for (int c = 0; c < codes.size(); c++) {
                contenido.append(LINE_SEPARATOR).append(codes.get(c));
                // Append sequence terminator on the last code of this sequence
                if (c == lastCodeIndex) {
                    if (s == lastSeqIndex) {
                        contenido.append(SEQUENCE_END);
                    } else {
                        contenido.append(";");
                    }
                }
            }

            // Blank line between sequences (not after the last)
            if (s < lastSeqIndex) {
                contenido.append(LINE_SEPARATOR);
            }
        }

        contenido.append(LINE_SEPARATOR);
        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Event";
    }
}
