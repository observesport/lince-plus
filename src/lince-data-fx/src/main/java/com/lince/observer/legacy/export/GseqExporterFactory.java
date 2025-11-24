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

import java.util.List;

/**
 * Factory for creating GSEQ format exporters.
 * <p>
 * This factory provides convenient methods to create the appropriate exporter
 * for each GSEQ format type.
 * <p>
 * Example usage:
 * <pre>
 * // Create an Event exporter
 * GseqExporter exporter = GseqExporterFactory.createEventExporter();
 * String output = exporter.export(criterios, datosVariables);
 *
 * // Create an Interval exporter with auto-calculated interval
 * GseqExporter intervalExporter = GseqExporterFactory.createIntervalExporter(datosVariables);
 * String intervalOutput = intervalExporter.export(criterios, datosVariables);
 * </pre>
 *
 * @author Claude Code
 * @version 4.0.0
 * @since 2025-01-24
 */
public class GseqExporterFactory {

    /**
     * Enum defining all supported GSEQ format types.
     */
    public enum GseqFormatType {
        EVENT,
        MULTIEVENT,
        TIMED,
        STATE,
        INTERVAL
    }

    /**
     * Create an exporter for the specified format type.
     *
     * @param formatType The GSEQ format type
     * @return Appropriate exporter instance
     * @throws IllegalArgumentException if formatType is null or INTERVAL (use createIntervalExporter instead)
     */
    public static GseqExporter createExporter(GseqFormatType formatType) {
        if (formatType == null) {
            throw new IllegalArgumentException("Format type cannot be null");
        }

        switch (formatType) {
            case EVENT:
                return createEventExporter();
            case MULTIEVENT:
                return createMultieventExporter();
            case TIMED:
                return createTimedExporter();
            case STATE:
                return createStateExporter();
            case INTERVAL:
                throw new IllegalArgumentException(
                    "Use createIntervalExporter() for INTERVAL format (requires interval duration)");
            default:
                throw new IllegalArgumentException("Unknown format type: " + formatType);
        }
    }

    /**
     * Create an Event format exporter.
     *
     * @return EventExporter instance
     */
    public static EventExporter createEventExporter() {
        return new EventExporter();
    }

    /**
     * Create a Multievent format exporter.
     *
     * @return MultieventExporter instance
     */
    public static MultieventExporter createMultieventExporter() {
        return new MultieventExporter();
    }

    /**
     * Create a Timed Event format exporter.
     *
     * @return TimedExporter instance
     */
    public static TimedExporter createTimedExporter() {
        return new TimedExporter();
    }

    /**
     * Create a State format exporter.
     *
     * @return StateExporter instance
     */
    public static StateExporter createStateExporter() {
        return new StateExporter();
    }

    /**
     * Create an Interval format exporter with specified interval duration.
     *
     * @param intervalSeconds Interval duration in seconds
     * @return IntervalExporter instance
     */
    public static IntervalExporter createIntervalExporter(int intervalSeconds) {
        return new IntervalExporter(intervalSeconds);
    }

    /**
     * Create an Interval format exporter with auto-calculated interval.
     * <p>
     * The interval will be calculated from the observation data.
     *
     * @param datosVariables List of observations to calculate interval from
     * @return IntervalExporter instance
     */
    public static IntervalExporter createIntervalExporter(List<FilaRegistro> datosVariables) {
        return new IntervalExporter(datosVariables);
    }
}
