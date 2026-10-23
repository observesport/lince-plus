/*
 *  LINCE PLUS - Automatizacion de datos observacionales
 *  Copyright (C) 2025  Alberto Soto
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
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;

import java.util.List;

/**
 * Abstract base class for GSEQ format exporters.
 * <p>
 * This class provides the template method pattern for GSEQ export operations.
 * All GSEQ formats follow a common structure:
 * <ol>
 *   <li>Format identifier header (e.g., "Event", "Multievent", "Interval=10'")</li>
 *   <li>Variable declarations (Format A or Format B, see GSEQ_FORMAT_SPECIFICATION.md)</li>
 *   <li>Data section with observations</li>
 *   <li>Sequence terminator (/)</li>
 * </ol>
 * <p>
 * Subclasses must implement format-specific logic for:
 * <ul>
 *   <li>Header generation ({@link #getFormatHeader()})</li>
 *   <li>Variable declaration format selection ({@link #useFormatA()})</li>
 *   <li>Data section export ({@link #exportDataSection(List, List)})</li>
 * </ul>
 * <p>
 * See GSEQ_FORMAT_SPECIFICATION.md for detailed format specifications.
 *
 * @author Alberto Soto
 * @version 4.0.0
 * @since 2025-01-24
 */
public abstract class GseqExporter {

    // Line ending constants (Windows-style CRLF required by GSEQ)
    protected static final String LINE_SEPARATOR = "\r\n";
    protected static final String SEQUENCE_END = "/";

    /**
     * Export data in GSEQ format.
     * <p>
     * This is the template method that defines the algorithm structure:
     * <pre>
     * 1. Generate format header
     * 2. Generate variable declarations
     * 3. Export data section
     * 4. Return complete export
     * </pre>
     *
     * @param criterios     List of criteria (columns) to export
     * @param datosVariables List of observation rows to export
     * @return Complete GSEQ format export as string
     */
    public final String export(List<Criterio> criterios, List<FilaRegistro> datosVariables) {
        StringBuilder contenido = new StringBuilder();

        // 1. Format header
        contenido.append(getFormatHeader());

        // 2. Variable declarations
        contenido.append(getVariableDeclarations(criterios));

        // 3. Data section
        contenido.append(exportDataSection(criterios, datosVariables));

        return contenido.toString();
    }

    /**
     * Get the format identifier header line(s).
     * <p>
     * Examples:
     * <ul>
     *   <li>Event format: "Event"</li>
     *   <li>Multievent format: "Multievent"</li>
     *   <li>Interval format: "Interval=10'\r\n"</li>
     * </ul>
     *
     * @return Format header string (may include line separator)
     */
    protected abstract String getFormatHeader();

    /**
     * Determine which variable declaration format to use.
     * <p>
     * Format A (Event, Multievent, Timed, State):
     * <pre>($CriterionName = code1 code2 ...)</pre>
     * <p>
     * Format B (Interval):
     * <pre>  fullname = code</pre>
     *
     * @return true for Format A (old format), false for Format B (interval format)
     */
    protected abstract boolean useFormatA();

    /**
     * Export the data section of the GSEQ format.
     * <p>
     * This is the format-specific part that varies significantly:
     * <ul>
     *   <li>Event: Simple sequential listing</li>
     *   <li>Multievent: Periods between observations, semicolon at end</li>
     *   <li>Timed: Time ranges with codes</li>
     *   <li>State: Duration calculations</li>
     *   <li>Interval: Timing offsets before data</li>
     * </ul>
     *
     * @param criterios     List of criteria to export
     * @param datosVariables List of observation rows
     * @return Data section string including sequence terminator (/)
     */
    protected abstract String exportDataSection(List<Criterio> criterios, List<FilaRegistro> datosVariables);

    /**
     * Get variable declarations in the appropriate format.
     *
     * @param criterios List of criteria
     * @return Variable declarations section
     */
    protected String getVariableDeclarations(List<Criterio> criterios) {
        InstrumentoObservacional instrumento = InstrumentoObservacional.getInstance();
        if (useFormatA()) {
            return instrumento.exportToSdisGseqOldFormat(criterios);
        } else {
            return instrumento.exportToSdisGseq(criterios);
        }
    }

    /**
     * Export a single observation row to string format.
     * <p>
     * Iterates through all criteria and concatenates the category codes
     * with the specified separator.
     *
     * @param criterios   List of criteria to export
     * @param filaRegistro The observation row to export
     * @param separator   Separator between category codes (e.g., " ", "+")
     * @return Formatted observation string (may be empty if no categories)
     */
    protected String exportFila(List<Criterio> criterios, FilaRegistro filaRegistro, String separator) {
        boolean isFirst = true;
        StringBuilder cont = new StringBuilder();
        for (Criterio criterio : criterios) {
            Categoria categoria = filaRegistro.getCategoria(criterio);
            if (categoria != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    cont.append(separator);
                }
                cont.append(categoria.getCodigo());
            }
        }
        return cont.toString();
    }

    /**
     * Get the format name for this exporter.
     * <p>
     * Used for debugging and logging purposes.
     *
     * @return Format name (e.g., "Event", "Multievent", "Interval")
     */
    public abstract String getFormatName();
}