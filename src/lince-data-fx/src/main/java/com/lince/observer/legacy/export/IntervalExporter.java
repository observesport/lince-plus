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
 * GSEQ Interval format exporter.
 * <p>
 * Interval format is used for time-sampled observations with fixed or calculated intervals.
 * Timing offsets (*N) appear BEFORE each observation.
 * <p>
 * Format structure:
 * <pre>
 * Interval=10'
 *   fullname1 = code1
 *   fullname2 = code2
 * * Metadata;
 *
 * &lt;S01&gt;
 * *0,code1,*10,code2,*15,code1
 *
 * (Session)/
 * </pre>
 *
 * @see GseqExporter
 * @see <a href="file:../../../../../integration/GSEQ_FORMAT_SPECIFICATION.md">GSEQ Format Specification</a>
 */
public class IntervalExporter extends GseqExporter {

    private int intervalSeconds;

    /**
     * Create an IntervalExporter with specified interval duration.
     *
     * @param intervalSeconds The interval duration in seconds (appears in header as "Interval=N'")
     */
    public IntervalExporter(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    /**
     * Create an IntervalExporter with auto-calculated interval.
     * <p>
     * The interval will be calculated from the observation data.
     *
     * @param datosVariables List of observations to calculate interval from
     */
    public IntervalExporter(List<FilaRegistro> datosVariables) {
        this.intervalSeconds = calculateIntervalDuration(datosVariables);
    }

    @Override
    protected String getFormatHeader() {
        return "Interval=" + intervalSeconds + "'" + LINE_SEPARATOR;
    }

    @Override
    protected boolean useFormatA() {
        return false; // Interval format uses Format B variable declarations
    }

    @Override
    protected String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        if (datosVariables.isEmpty()) {
            return contenido.append(SEQUENCE_END).append(LINE_SEPARATOR).toString();
        }

        // Start with empty line and session marker
        contenido.append(LINE_SEPARATOR).append("<S01>").append(LINE_SEPARATOR);

        int tiempoAnterior = 0;
        boolean isFirst = true;

        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = exportFila(criterios, filaRegistro, " ");

            // Skip empty observations
            if (cont.isEmpty()) {
                continue;
            }

            // Calculate time interval from previous observation (or from start for first one)
            int tiempoActual = filaRegistro.getMilis();
            int intervalo = tiempoActual - tiempoAnterior;

            if (!isFirst) {
                contenido.append(",");
            }

            // Add timing before the data (critical for Interval format)
            contenido.append("*").append(intervalo).append(",").append(cont);

            isFirst = false;
            tiempoAnterior = tiempoActual;
        }

        // Add session end marker with placeholder metadata
        contenido.append(LINE_SEPARATOR).append(LINE_SEPARATOR)
                .append("(Session)").append(SEQUENCE_END).append(LINE_SEPARATOR);

        return contenido.toString();
    }

    @Override
    public String getFormatName() {
        return "Interval";
    }

    /**
     * Calculates the average interval duration between observations in seconds.
     *
     * @param datosVariables List of observations
     * @return Average interval in seconds (minimum 1, default 10 if insufficient data)
     */
    private int calculateIntervalDuration(List<FilaRegistro> datosVariables) {
        if (datosVariables == null || datosVariables.size() < 2) {
            return 10; // Default to 10 seconds if insufficient data
        }

        // Calculate average time difference between consecutive observations
        long totalDiff = 0;
        int count = 0;

        for (int i = 1; i < datosVariables.size(); i++) {
            int diff = datosVariables.get(i).getMilis() - datosVariables.get(i - 1).getMilis();
            if (diff > 0) { // Only count positive differences
                totalDiff += diff;
                count++;
            }
        }

        if (count == 0) {
            return 10; // Default to 10 seconds
        }

        // Convert milliseconds to seconds and round
        int avgIntervalMs = (int) (totalDiff / count);
        int avgIntervalSeconds = (avgIntervalMs + 500) / 1000; // Round to nearest second

        // Return at least 1 second
        return Math.max(1, avgIntervalSeconds);
    }

    /**
     * Get the interval duration in seconds.
     *
     * @return Interval duration
     */
    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    /**
     * Set the interval duration in seconds.
     *
     * @param intervalSeconds New interval duration
     */
    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
}
