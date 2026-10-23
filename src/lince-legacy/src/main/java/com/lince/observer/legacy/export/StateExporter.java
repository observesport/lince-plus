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
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;

import java.util.ArrayList;
import java.util.List;

/**
 * GSEQ State Sequential Data format exporter.
 * <p>
 * Produces multi-stream output: one stream per criterion, separated by {@code &}.
 * Within each stream, state transitions are written as {@code code,M:SS} pairs.
 * <p>
 * Lince PLUS has no native session/episode concept, so all observations form
 * a single session {@code <S01>} ending with {@code /}.
 *
 * @see GseqExporter
 */
public class StateExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "State";
    }

    @Override
    protected boolean useFormatA() {
        return true;
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        if (datosVariables.isEmpty() || criterios.isEmpty()) {
            contenido.append(LINE_SEPARATOR).append("<S01>").append(LINE_SEPARATOR);
            contenido.append(LINE_SEPARATOR).append(SEQUENCE_END).append(LINE_SEPARATOR);
            return contenido.toString();
        }

        // Session marker with start time
        String startTime = Tiempo.formatSimpleSeconds(datosVariables.get(0).getMilis());
        contenido.append(LINE_SEPARATOR).append("<S01>,").append(startTime);

        // Build one stream per criterion, separated by &
        boolean isFirstCriterion = true;
        for (Criterio criterio : criterios) {
            if (!isFirstCriterion) {
                contenido.append(" &").append(LINE_SEPARATOR);
            }

            // Collect transitions for this criterion
            List<String> codes = new ArrayList<>();
            List<Long> times = new ArrayList<>();

            for (FilaRegistro fila : datosVariables) {
                Categoria cat = fila.getCategoria(criterio);
                if (cat != null) {
                    String code = cat.getCodigo();
                    long time = fila.getMilis();
                    // Only add if different from previous code (state transition)
                    if (codes.isEmpty() || !codes.get(codes.size() - 1).equals(code)) {
                        codes.add(code);
                        times.add(time);
                    }
                }
            }

            // Write transitions: code,M:SS
            StringBuilder stream = new StringBuilder();
            for (int i = 0; i < codes.size(); i++) {
                if (stream.length() > 0) {
                    stream.append(" ");
                }
                stream.append(codes.get(i)).append(",").append(Tiempo.formatSimpleSeconds(times.get(i)));
            }
            if (stream.length() > 0) {
                if (isFirstCriterion) {
                    // First stream follows session marker on same line, separated by space
                    contenido.append(" ").append(stream);
                } else {
                    contenido.append(stream);
                }
            }
            isFirstCriterion = false;
        }

        // End the single session with /
        contenido.append(SEQUENCE_END).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "State";
    }
}
