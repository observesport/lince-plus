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
package com.lince.observer.legacy;

import com.lince.observer.data.LegacyToolException;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.instrumentoObservacional.NodoInformacion;
import com.lince.observer.data.util.TimeCalculations;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Moved from lince.modelo.FilaRegistro
 *
 * @author Alberto Soto-Fernandez
 * @author berto
 */
public class FilaRegistro implements Comparable<FilaRegistro>, Serializable {

    private int milis;
    private Map<Criterio, Categoria> registro;
    private Map<NodoInformacion, String> datosMixtos;
    private double fps;
    TimeCalculations timeCalculations = new TimeCalculations();

    public FilaRegistro(int milis, Map<Criterio, Categoria> categoriasSeleccionadas, Map<NodoInformacion, String> datosMixtos, Double fps) {
        this.milis = milis;
        this.registro = categoriasSeleccionadas;
        this.datosMixtos = datosMixtos;
        this.fps = fps != null? fps : TimeCalculations.DEFAULT_FPS;
    }

    public FilaRegistro(Integer milis, HashMap<Criterio, Categoria> categoriasSeleccionadas, HashMap<NodoInformacion, String> datosMixtos) {
        this(milis, categoriasSeleccionadas, datosMixtos, 0.0);
    }

    public FilaRegistro getFilaRegistroCorrecta(List<LegacyToolException> exceptions) {
        Criterio[] criteriosActuales = InstrumentoObservacional.getInstance().getCriterios();
        NodoInformacion[] mixtosActuales = InstrumentoObservacional.getInstance().getDatosMixtos();
        Set<Criterio> setDeKeys = this.registro.keySet();
        Set<NodoInformacion> setDeKeysDatosMixtos = this.datosMixtos.keySet();
        Map<Criterio, Categoria> registroActual = new HashMap<>(this.registro.size());
        Map<NodoInformacion, String> datosMixtosActuales = new HashMap<>();
        for (Criterio criterio : setDeKeys) {
            Categoria categoria = this.registro.get(criterio);
            if (categoria != null) {
                for (Criterio criterioActual : criteriosActuales) {
                    if (criterio.getNombre().equalsIgnoreCase(criterioActual.getNombre())) {
                        Categoria categoriaActual = criterioActual.getCategoriaByCodigo(categoria.getCodigo());
                        if (categoriaActual != null) {
                            registroActual.put(criterioActual, categoriaActual);
                        } else {
                            addExceptionIfNotExists(exceptions, categoria);
                        }
                        break;
                    }
                }
            }
        }

        for (NodoInformacion nodoInformacion : setDeKeysDatosMixtos) {
            String string = this.datosMixtos.get(nodoInformacion);
            if (string != null) {
                for (NodoInformacion nodoInformacionActual : mixtosActuales) {
                    if (nodoInformacion.getNombre().equalsIgnoreCase(nodoInformacionActual.getNombre())) {
                        String stringActual = this.datosMixtos.get(nodoInformacion);
                        if (stringActual != null) {
                            datosMixtosActuales.put(nodoInformacionActual, stringActual);
                        }
                        break;
                    }
                }
            }
        }

        return new FilaRegistro(this.milis, registroActual, datosMixtosActuales, this.fps);
    }

    private void addExceptionIfNotExists(List<LegacyToolException> exceptions, Categoria categoria) {
        String message = ResourceBundle.getBundle("i18n.Bundle").getString("CATEGORIA ")
                + categoria.getNombre()
                + ResourceBundle.getBundle("i18n.Bundle").getString(" CON CODIGO ")
                + categoria.getCodigo()
                + ResourceBundle.getBundle("i18n.Bundle").getString(" NO ENCONTRADA.");

        boolean found = exceptions.stream()
                .anyMatch(e -> StringUtils.contains(e.getMessage(), message));

        if (!found) {
            LegacyToolException e = new LegacyToolException(message);
            e.setCategoria(categoria);
            exceptions.add(e);
        }
    }

    public int getMilis() {
        return milis;
    }

    /**
     * asoto 2019
     * Needs update for letting search by name between diferent register
     *
     * @param criterio Criteria
     * @return Selected Category for this criteria inside the register line
     */
    public Categoria getCategoria(Criterio criterio) {
        try {
            Categoria aux = registro.get(criterio);
            if (aux == null) {
                for (Iterator<Map.Entry<Criterio, Categoria>> it = registro.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Criterio, Categoria> item = it.next();
                    if (item.getKey().equals(criterio)) {
                        return item.getValue();
                    }
                }
            }
            return aux;
        } catch (Exception e) {
            return null;
        }
    }

    public String getDatoMixto(NodoInformacion nodoInformacion) {
        return datosMixtos.get(nodoInformacion);
    }

    public void setCategoria(Criterio criterio, Categoria categoria) {
        registro.put(criterio, categoria);
    }

    void setCategoria(NodoInformacion nodoInformacion, String string) {
        datosMixtos.put(nodoInformacion, string);
    }

    public int getRegisterFrameValue() {
       return (int) timeCalculations.convertMsToFPS(milis, this.fps);
    }

    /**
     * @return
     */
    public Map<Criterio, Categoria> getRegistro() {
        return registro;
    }

    @Override
    public int compareTo(FilaRegistro o) {
        return this.milis - o.milis;
    }

    public double getFps() {
        return fps;
    }
}
