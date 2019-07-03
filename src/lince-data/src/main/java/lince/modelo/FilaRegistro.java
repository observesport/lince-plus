/*
 *  Lince - Automatizacion de datos observacionales
 *  Copyright (C) 2010  Brais Gabin Moreira
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
package lince.modelo;

import lince.modelo.InstrumentoObservacional.Categoria;
import lince.modelo.InstrumentoObservacional.Criterio;
import lince.modelo.InstrumentoObservacional.InstrumentoObservacional;
import lince.modelo.InstrumentoObservacional.NodoInformacion;

import java.io.Serializable;
import java.util.*;

/**
 * @author Brais
 */
public class FilaRegistro implements Comparable<FilaRegistro>, Serializable {

    private int milis;
    private Map<Criterio, Categoria> registro;
    private Map<NodoInformacion, String> datosMixtos;

    public static FilaRegistro getFilaRegistroCorrecta(FilaRegistro filaRegistroAntigua, List<String> exceptions) {
        Criterio criteriosActuales[] = InstrumentoObservacional.getInstance().getCriterios();
        NodoInformacion mixtosActuales[] = InstrumentoObservacional.getInstance().getDatosMixtos();
        Map<Criterio, Categoria> mapRegistro = filaRegistroAntigua.registro;
        Set<Criterio> setDeKeys = mapRegistro.keySet();
        Map<NodoInformacion, String> mapDatosMixtos = filaRegistroAntigua.datosMixtos;
        Set<NodoInformacion> SetDeKeysDatosMixtos = mapDatosMixtos.keySet();

        Map<Criterio, Categoria> registroActual = new HashMap<Criterio, Categoria>(mapRegistro.size());
        Map<NodoInformacion, String> datosMixtosAcutuales = new HashMap<NodoInformacion, String>();

        for (Criterio criterio : setDeKeys) {
            Categoria categoria = mapRegistro.get(criterio);
            if (categoria != null) {
                for (Criterio criterioActual : criteriosActuales) {
                    if (criterio.getNombre().equalsIgnoreCase(criterioActual.getNombre())) {
                        Categoria categoriaActual = criterioActual.getCategoriaByCodigo(categoria.getCodigo());
                        if (categoriaActual != null) {
                            registroActual.put(criterioActual, categoriaActual);
                        } else {
                            exceptions.add(java.util.ResourceBundle.getBundle("i18n.Bundle").getString("CATEGORIA ") + categoria.getNombre() + java.util.ResourceBundle.getBundle("i18n.Bundle").getString(" CON CODIGO ") + categoria.getCodigo() + java.util.ResourceBundle.getBundle("i18n.Bundle").getString(" NO ENCONTRADA."));
                        }
                        break;
                    }
                }
            }
        }

        for (NodoInformacion nodoInformacion : SetDeKeysDatosMixtos) {
            String string = mapDatosMixtos.get(nodoInformacion);
            if (string != null) {
                for (NodoInformacion nodoInformacionActual : mixtosActuales) {
                    if (nodoInformacion.getNombre().equalsIgnoreCase(nodoInformacionActual.getNombre())) {
                        String stringActual = mapDatosMixtos.get(nodoInformacion);
                        if (stringActual != null) {
                            datosMixtosAcutuales.put(nodoInformacionActual, stringActual);
                        }
                        break;
                    }
                }
            }
        }

        FilaRegistro filaRegistroActual = new FilaRegistro(filaRegistroAntigua.milis, registroActual, datosMixtosAcutuales);
        return filaRegistroActual;
    }

    /**
     * @param milis
     * @param registro
     * @deprecated Existe solo por compatibilidad con archivos de registro guardados
     * con versiones anteriores: XMLEncoder
     */
    @Deprecated
    public FilaRegistro(int milis, Map<Criterio, Categoria> registro) {
        this.milis = milis;
        this.registro = registro;
        this.datosMixtos = new HashMap<NodoInformacion, String>();
    }

    public FilaRegistro(int milis, Map<Criterio, Categoria> registro, Map<NodoInformacion, String> datosMixtos) {
        this.milis = milis;
        this.registro = registro;
        this.datosMixtos = datosMixtos;
    }

    public int getMilis() {
        return milis;
    }

    public void setMilis(int milis) {
        this.milis = milis;
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

    public int getFrames() {
        return milis / 40;
    }

    /**
     * @return
     */
    public Map<Criterio, Categoria> getRegistro() {
        return registro;
    }

    /**
     * @return
     */
    public Map<NodoInformacion, String> getDatosMixtos() {
        return datosMixtos;
    }

    @Override
    public int compareTo(FilaRegistro o) {
        return this.milis - o.milis;
    }
}
