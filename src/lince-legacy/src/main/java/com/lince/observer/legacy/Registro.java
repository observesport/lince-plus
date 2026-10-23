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
import com.lince.observer.data.LinceDataConstants;
import com.lince.observer.data.legacy.datos.ControladorArchivos;
import com.lince.observer.legacy.export.GseqExporter;
import com.lince.observer.legacy.export.GseqExporterFactory;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.instrumentoObservacional.NodoInformacion;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Alberto Soto-Fernandez
 */
@Deprecated
public class Registro implements Observer {

    static final String NOMBRE_INSTRUMENTO_OBSERVACIONAL = "nombreInstrumentoObservacional";
    static final String CRITERIOS = "criterios";
    static final String DATOS_FIJOS = "datosEstaticos";
    static final String DATOS_VARIABLES = "datosVariables";


    private static Registro instance = new Registro(); //asd 2017: to avoid null pointer exception
    private static double fps = LinceDataConstants.DEFAULT_FPS;
    public List<FilaRegistro> datosVariables;
    protected NodoInformacion[] datosMixtos;
    protected Criterio[] datos;
    private Map<NodoInformacion, String> datosFijos;

    Registro() {
        initRegistro(new ArrayList<>(), new HashMap<>());
    }

    Registro(List<FilaRegistro> datosVariables, Map<NodoInformacion, String> datosFijos) {
        initRegistro(datosVariables, datosFijos);
    }

    private void initRegistro(List<FilaRegistro> datosVariables, Map<NodoInformacion, String> datosFijos) {
        InstrumentoObservacional.addObservador(this);
        InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
        this.datos = instrumentoObservacional.getCriterios();
        this.datosMixtos = instrumentoObservacional.getDatosMixtos();
        this.datosVariables = datosVariables;
        this.datosFijos = datosFijos;
    }

    public static void loadNewInstance() {
        cambiarInstancia(new Registro());
    }
    /**
     * Makes an already loaded register the current one
     * (LO-349: lets importers validate the load before replacing the singleton).
     *
     * @param registro register returned by {@link #cargarRegistro(File, List)}
     */
    public static void useInstance(Registro registro) {
        cambiarInstancia(registro);
    }

    private static void cambiarInstancia(Registro newInstance) {
        if (instance == null) {
            instance = new Registro();
        }
        InstrumentoObservacional.removeObservador(instance);
        instance = newInstance;
    }

    public static synchronized Registro getInstance() {
        if (instance == null) {
            instance = new Registro();
        }
        return instance;
    }

    public static Registro cargarRegistro(File f, List<LegacyToolException> exceptions) {
        try {
            Map<String, Object> datosGuardados = (Map<String, Object>) ControladorArchivos.getInstance().cargar(f);
            Criterio[] criterios = (Criterio[]) datosGuardados.get(CRITERIOS);
            Map<NodoInformacion, String> datosFijosGuardados = (Map<NodoInformacion, String>) datosGuardados.get(DATOS_FIJOS);
            Map<NodoInformacion, String> datosFijos = cargarDatosFijos(datosFijosGuardados, exceptions);
            comprobarCriterios(criterios, exceptions);
            List<FilaRegistro> datosVariablesGuardados = (List<FilaRegistro>) datosGuardados.get(DATOS_VARIABLES);
            List<FilaRegistro> datosVariables = cargarDatos(datosVariablesGuardados, exceptions, false);
            return new Registro(datosVariables, datosFijos);
        } catch (IOException ex) {
            LegacyToolException exception = new LegacyToolException("Error loading file: " + f.getPath(), ex);
            exceptions.add(exception);
            return null;
        }
    }

    private static Map<NodoInformacion, String> cargarDatosFijos(Map<NodoInformacion, String> map, List<LegacyToolException> exceptions) {
        InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
        Map<NodoInformacion, String> datosFijos = new HashMap<NodoInformacion, String>(map.size());
        Set<NodoInformacion> setDeKeys = map.keySet();
        NodoInformacion nodosInformacion[] = instrumentoObservacional.getDatosFijos();
        for (NodoInformacion nodoInformacionProvisional : setDeKeys) {
            boolean encontrado = false;
            for (NodoInformacion nodoInformacionBienReferenciado : nodosInformacion) {
                if (nodoInformacionBienReferenciado.getNombre().equals(nodoInformacionProvisional.getNombre())) {
                    encontrado = true;
                    datosFijos.put(nodoInformacionBienReferenciado, map.get(nodoInformacionProvisional));
                    break;
                }
            }
            if (!encontrado) {
                LegacyToolException exception = new LegacyToolException(ResourceBundle.getBundle("i18n.Bundle").getString("NO SE HA ENCONTRADO REFERENCIA PARA EL DATO ESTÁTICO: ")
                        + nodoInformacionProvisional
                        + ResourceBundle.getBundle("i18n.Bundle").getString(" EN EL INSTRUMENTO OBSERVACIONAL ACTUAL."));
                exceptions.add(exception);
            }
        }
        return datosFijos;
    }

    /**
     * Comprueba que en los criterios que hay guardados dentro del archivo cargado
     * existan en el instrumento observacional actualmente cargado.
     *
     * @param criterios
     * @param exceptions
     */
    private static void comprobarCriterios(Criterio[] criterios, List<LegacyToolException> exceptions) {
        Criterio criteriosActuales[] = InstrumentoObservacional.getInstance().getCriterios();

        for (Criterio criterio : criterios) {
            boolean encontrado = false;
            for (Criterio criterioActual : criteriosActuales) {
                if (criterio.getNombre().equals(criterioActual.getNombre())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                String msg = String.format("%s: %s.", ResourceBundle.getBundle("i18n.Bundle").getString("EN EL INSTUMENTO ACTUAL NO SE ENCUENTRA EL CRITERIO "), criterio);
                LegacyToolException exception = new LegacyToolException(msg);
                exceptions.add(exception);
            }
        }
    }

    public static List<FilaRegistro> cargarDatos(List<FilaRegistro> list, List<LegacyToolException> exceptions, boolean resetContent) {
        if (resetContent) {
            getInstance().initRegistro(list, null);
        }
        List<FilaRegistro> datos = new ArrayList<>();
        for (FilaRegistro filaRegistro : list) {
            datos.add(filaRegistro.getFilaRegistroCorrecta( exceptions));
        }
        return datos;
    }

    public String getValorFijo(NodoInformacion nodoInformacion) {
        return datosFijos.get(nodoInformacion);
    }

    @Override
    public void update(Observable o, Object arg) {
        InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
        this.datos = instrumentoObservacional.getCriterios();
        this.datosMixtos = instrumentoObservacional.getDatosMixtos();
    }

    public String[] getColumna(Criterio criterio) {
        String[] valores = new String[datosVariables.size()];
        int i = 0;
        for (FilaRegistro filaRegistro : datosVariables) {
            Categoria categoria = filaRegistro.getCategoria(criterio);
            valores[i++] = categoria == null ? "" : categoria.getCodigo();
        }
        return valores;
    }

    /**
     * What the Lince 1 {@code save()} wrote to a .rlince file (LO-349: lets exporters serialise it without a file).
     *
     * @return instrument name, criteria, fixed data and rows
     */
    public Map<String, Object> getSaveContent() {
        Map<String, Object> map = new HashMap<String, Object>(3);
        map.put(NOMBRE_INSTRUMENTO_OBSERVACIONAL, InstrumentoObservacional.getInstance().getName());
        map.put(CRITERIOS, datos);
        map.put(DATOS_FIJOS, datosFijos);
        map.put(DATOS_VARIABLES, datosVariables);
        return map;
    }

    public String exportToSdisGseqEvento(List<Criterio> criterios) {
        GseqExporter exporter = GseqExporterFactory.createEventExporter();
        return exporter.export(criterios, datosVariables);
    }

    public String exportToSdisGseqMultievento(List<Criterio> criterios) {
        GseqExporter exporter = GseqExporterFactory.createMultieventExporter();
        return exporter.export(criterios, datosVariables);
    }

    public String exportToSdisGseqEventoConTiempo(List<Criterio> criterios) {
        GseqExporter exporter = GseqExporterFactory.createTimedExporter();
        return exporter.export(criterios, datosVariables);
    }

    public String exportToSdisGseqIntervalo(List<Criterio> criterios) {
        GseqExporter exporter = GseqExporterFactory.createIntervalExporter(datosVariables);
        return exporter.export(criterios, datosVariables);
    }

    public String exportToSdisGseqEstado(List<Criterio> criterios) {
        GseqExporter exporter = GseqExporterFactory.createStateExporter();
        return exporter.export(criterios, datosVariables);
    }

    public String exportToTheme5(List<Criterio> criterios) {
        return exportRegistroToTheme(criterios, ";", false);
    }

    private String exportRegistroToTheme(List<Criterio> criterios, String separator, boolean useSequentialTime) {
        String contenido = "";
        contenido += "TIME" + separator + "EVENT\r\n";
        if (datosVariables == null || datosVariables.isEmpty()) {
            return contenido;
        }
        boolean isFirst = true;
        int sequentialTime = 1;
        for (FilaRegistro filaRegistro : datosVariables) {
            String cont = StringUtils.trim(exportFila(criterios, filaRegistro, ","));
            int timeValue = useSequentialTime ? sequentialTime : filaRegistro.getRegisterFrameValue();
            if (isFirst) {
                int startTime = useSequentialTime ? sequentialTime : (filaRegistro.getRegisterFrameValue() - 1);
                contenido += startTime + separator + ":\r\n";
                isFirst = false;
                if (useSequentialTime) {
                    sequentialTime++;
                    timeValue = sequentialTime;
                }
            }
            contenido += timeValue + separator + cont + "\r\n";
            sequentialTime++;
        }
        int footTime = useSequentialTime ? sequentialTime : (datosVariables.get(datosVariables.size() - 1).getRegisterFrameValue() + 1);
        contenido += footTime + separator + "&\r\n";
        return contenido;
    }

    public String exportToSas(List<Object> criterios) {
        return exportRegistroToSas(criterios);
    }

    private String exportRegistroToSas(List<Object> criterios) {
        List<NodoInformacion> fijos = List.of(InstrumentoObservacional.getInstance().getDatosFijos());
        List<NodoInformacion> mixtos = List.of(InstrumentoObservacional.getInstance().getDatosMixtos());
        String contenido = "";

        int numCriterios = criterios.size();
        List<String[]> tabla = new ArrayList<String[]>(datosVariables.size());
        for (FilaRegistro fila : datosVariables) {
            String f[] = new String[numCriterios + 1];
            f[numCriterios] = "1";
            int i = 0;
            for (Object obj : criterios) {
                if (fijos.contains(obj)) {
                    f[i++] = getValorFijo((NodoInformacion) obj);
                } else if (mixtos.contains(obj)) {
                    f[i++] = fila.getDatoMixto((NodoInformacion) obj);
                } else {
                    Categoria c = fila.getCategoria((Criterio) obj);
                    if (c != null) {
                        f[i++] = c.getCodigo();
                    }
                }
            }
            tabla.add(f);
        }

        for (int i = 0; i < tabla.size(); i++) {
            String fila[] = tabla.get(i);
            int contador = 2;
            for (int j = i + 1; j < tabla.size(); j++) {
                if (sonIguales(fila, tabla.get(j))) {
                    tabla.remove(j);
                    j--;
                    tabla.get(i)[numCriterios] = contador++ + "";
                }
            }
        }

        for (String[] sArray : tabla) {
            for (String s : sArray) {
                contenido += s + "\t";
            }
            contenido += "\r\n";
        }

        return contenido;
    }

    private boolean sonIguales(String[] fila1, String[] fila2) {
        int size = fila1.length - 1;
        boolean iguales = true;
        for (int i = 0; i < size && iguales; i++) {
            if (fila1[i] != null) {
                if (!fila1[i].equals(fila2[i])) {
                    iguales = false;
                }
            } else {
                if (fila2[i] != null) {
                    iguales = false;
                }
            }
        }
        return iguales;
    }

    private String exportFila(List<Criterio> criterios, FilaRegistro filaRegistro, String separador) {
        boolean isFirst = true;
        String cont = "";
        for (Criterio criterio : criterios) {
            Categoria categoria = filaRegistro.getCategoria(criterio);
            if (categoria != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    cont += separador;
                }
                cont += categoria.getCodigo();
            }
        }
        return cont;
    }


    public void addRow(int milis, Map<Criterio, Categoria> categoriasSeleccionadas, Map<NodoInformacion, String> datosMixtos) {
        FilaRegistro filaRegistro = new FilaRegistro(milis, categoriasSeleccionadas, datosMixtos, fps);
        datosVariables.add(filaRegistro);
        Collections.sort(datosVariables);
    }

    public static void setFps(double fps) {
        Registro.fps = fps;
    }

}
