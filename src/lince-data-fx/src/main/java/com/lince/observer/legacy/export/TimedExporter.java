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
 * GSEQ Timed Event format exporter.
 * <p>
 * Produces multi-stream output: one stream per criterion, separated by {@code &}.
 * Within each stream, code transitions are written as {@code code,MM:SS-MM:SS}
 * (with end time from next transition in same criterion) or {@code code,MM:SS-}
 * (open-ended, when no further transition).
 * <p>
 * Lince PLUS has no native session/episode concept, so all observations form
 * a single session {@code <S01>} ending with {@code /}.
 *
 * @see GseqExporter
 */
public class TimedExporter extends GseqExporter {

    @Override
    protected String getFormatHeader() {
        return "Timed";
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

        // Session marker
        contenido.append(LINE_SEPARATOR).append("<S01>").append(LINE_SEPARATOR);
        contenido.append(LINE_SEPARATOR);

        // Build one stream per criterion, separated by &
        boolean isFirstCriterion = true;
        for (Criterio criterio : criterios) {
            if (!isFirstCriterion) {
                contenido.append(LINE_SEPARATOR).append("&").append(LINE_SEPARATOR);
            }
            isFirstCriterion = false;

            // Collect transitions for this criterion: (code, startTime) pairs
            List<String> codes = new ArrayList<>();
            List<Long> startTimes = new ArrayList<>();

            for (FilaRegistro fila : datosVariables) {
                Categoria cat = fila.getCategoria(criterio);
                if (cat != null) {
                    String code = cat.getCodigo();
                    long time = fila.getMilis();
                    // Only add if different from previous code (transition)
                    if (codes.isEmpty() || !codes.get(codes.size() - 1).equals(code)) {
                        codes.add(code);
                        startTimes.add(time);
                    }
                }
            }

            // Write transitions: code,MM:SS-MM:SS or code,MM:SS-
            StringBuilder stream = new StringBuilder();
            for (int i = 0; i < codes.size(); i++) {
                if (stream.length() > 0) {
                    stream.append(" ");
                }
                stream.append(codes.get(i)).append(",").append(Tiempo.formatSimpleSeconds(startTimes.get(i)));
                if (i + 1 < codes.size()) {
                    // End time is next transition's start time
                    stream.append("-").append(Tiempo.formatSimpleSeconds(startTimes.get(i + 1)));
                } else {
                    // Open-ended (last transition in stream)
                    stream.append("-");
                }
            }
            contenido.append(stream);
        }

        // End the single session with /
        contenido.append(SEQUENCE_END).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Timed";
    }
}
