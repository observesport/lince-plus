package com.lince.observer.data.legacy.plugins;

import com.lince.observer.data.LinceDataConstants;
import com.healthmarketscience.jackcess.*;
import com.lince.observer.legacy.FilaRegistro;
import com.lince.observer.legacy.instrumentoObservacional.*;
import com.lince.observer.legacy.Registro;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Created by Alberto Soto on 08/06/2015.
 */
public class HoisanTool {

    private Logger log = LoggerFactory.getLogger(HoisanTool.class.getName());
    private static final String HOISAN_TEMPLATE_FILE = "hoisanTemplate.mdb";
    private static final int FRAME_WINDOW = 40;
    private static final String LINCE_CRITERIA_TYPE = "Lince";

    private HashMap<Criterio, Integer> criteriaMap = new HashMap<Criterio, Integer>();
    private HashMap<Categoria, Integer> categoryMap = new HashMap<Categoria, Integer>();
    private Integer actorId;

    /**
     * Reads a HOISAN database into the legacy model without writing anything (LO-349). Replaces the
     * {@link InstrumentoObservacional} and {@link Registro} singletons, so callers must hold the legacy model lock
     * ({@code LegacyBridge.withLegacyModel}) while they read the result.
     *
     * @param file HOISAN .mdb file
     * @return the instrument (the singleton) and one register per video name, in table order
     * @throws IOException when the file is not a readable Access database with the HOISAN tables
     */
    public HoisanData read(File file) throws IOException {
        Database db = open(file, true);
        try {
            Table criterios = requireTable(db, HoisanVars.CRITERIA_TABLE_NAME);
            Table categorias = requireTable(db, HoisanVars.CATEGORY_TABLE_NAME);
            Table tiempos = requireTable(db, HoisanVars.OBSERVATION_TIMES_TABLE_NAME);
            Table tiempoCategorias = requireTable(db, HoisanVars.OBSERVATION_CATEGORY_TABLE_NAME);
            InstrumentoObservacional instrumentoObservacional = generateInstrumentObservacional(criterios, categorias);
            Map<String, Registro> registros = generateRegistros(tiempos, tiempoCategorias, criterios, categorias, instrumentoObservacional);
            return new HoisanData(instrumentoObservacional, registros);
        } finally {
            db.close();
        }
    }

    /**
     * Opens an Access database that refuses linked tables: Jackcess would otherwise open the linked file named by the
     * database (any local path or UNC share) read-write the first time a linked HOISAN table is read.
     */
    private static Database open(File file, boolean readOnly) throws IOException {
        Database db = Database.open(file, readOnly);
        db.setLinkResolver((linker, linkeeFileName) -> {
            throw new IOException("Linked tables are not supported");
        });
        return db;
    }

    private static Table requireTable(Database db, HoisanVars name) throws IOException {
        Table table = db.getTable(name.toString());
        if (table == null) {
            throw new IOException("Not a HOISAN database, missing table " + name);
        }
        return table;
    }

    /**
     * Legacy model read from a HOISAN database.
     *
     * @param instrument the instrument (the {@link InstrumentoObservacional} singleton when read)
     * @param registers  one register per HOISAN video name, in table order
     */
    public record HoisanData(InstrumentoObservacional instrument, Map<String, Registro> registers) {
    }

    /**
     * Writes the current legacy instrument and register as a HOISAN database. The database is closed whether the
     * write succeeds or not.
     *
     * @param outputFile target, its extension is forced to .mdb
     * @return null on success, otherwise the error
     */
    public String exportFile(File outputFile) {
        try {
            File realBaseFile = createValidEmptyHoisanFile(outputFile);
            //tablas vacias
            try (Database db = open(realBaseFile, false)) {
                store(db);
            }
            return null;
        } catch (Exception e) {
            log.error("Error copying file ", e);
            return e.toString();
        }
    }

    /**
     * Writes the current legacy instrument and register into the empty HOISAN tables of {@code db} and flushes it.
     * The caller closes the database.
     *
     * @param db database copied from the HOISAN template
     * @throws IOException when the database cannot be written
     */
    protected void store(Database db) throws IOException {
        Table tCriterios = db.getTable(HoisanVars.CRITERIA_TABLE_NAME.toString());
        Table tCategorias = db.getTable(HoisanVars.CATEGORY_TABLE_NAME.toString());
        Table tTiempos = db.getTable(HoisanVars.OBSERVATION_TIMES_TABLE_NAME.toString());
        Table tTiempoCategorias = db.getTable(HoisanVars.OBSERVATION_CATEGORY_TABLE_NAME.toString());

        //usado para obtener cuadros de dialogo en logica brais
        String[] otros = {LinceDataConstants.COL_TFRAMES, LinceDataConstants.COL_DURACION_FR, LinceDataConstants.COL_TSEGUNDOS, LinceDataConstants.COL_DURACION_SEC, LinceDataConstants.COL_TMILISEGUNDOS, LinceDataConstants.COL_DURACION_MS};
        //desde criterios tenemos toda la jerarquia de elementos a insertar en tables criterio y categoria
        List<Criterio> criterios = List.of(InstrumentoObservacional.getInstance().getCriterios());
        List<NodoInformacion> datosMixtos = List.of(InstrumentoObservacional.getInstance().getDatosMixtos());
        List<NodoInformacion> datosFijos = List.of(InstrumentoObservacional.getInstance().getDatosFijos());
        //usado para exportar e importar en codigo brais
        //en registro -> datos variables, tendriamos todos los datos necesarios para el registro temporal
        Registro registro = Registro.getInstance();
        //tCriterios.addRow(Column.AUTO_NUMBER,"tst","Lince","loren ipsum")
        this.actorId = storeActor(db);
        log.warn("empezando");
        storeCriteria(db, tCriterios, criterios, tCategorias);
        log.warn("guardados criterios");
        storeObservationTimes(db, registro, tTiempos, tTiempoCategorias);
        log.warn("guardados tiempos");
        log.warn("Finalizado");
        db.flush();
    }


    private Integer storeActor(Database db) {
        try {
            Table table = db.getTable(HoisanVars.ACTORS_TABLE_NAME.toString());
            table.addRow(Column.AUTO_NUMBER, "Lince", "Exportación originada por Lince", "EventoMultiModal");
            Integer aux = findFieldWithValue(table, "Lince", HoisanVars.ACTORS_NAME, HoisanVars.ACTORS_PK);
            db.flush();
            Table t2 = db.getTable(HoisanVars.RESEARCH_TABLE_NAME.toString());
            t2.addRow(Column.AUTO_NUMBER, "Registros Lince", "Registros Lince");
            return aux;
        } catch (Exception e) {
            log.error("iniciando actores del sistema");
        }
        return null;
    }

    private void storeCriteria(Database db, Table data, List<Criterio> innerData, Table tCategories) {

        for (Criterio criterio : innerData) {
            try {
                data.addRow(Column.AUTO_NUMBER, criterio.getNombre(), "Lince", criterio.getDescripcion());
                Integer id = findCriteriaByName(data, criterio.getNombre());
                //db.flush();
                this.criteriaMap.put(criterio, id);
                for (Categoria cat : criterio.getCategoriasHijo()) {
                    //no entiendo pq va con doble id, pero asi funciona correctamente
                    //si no, da number format exception
                    String catName = StringUtils.substring(cat.getNombre(), 0, 49);
                    tCategories.addRow(Column.AUTO_NUMBER
                            , catName
                            , id
                            , cat.getDescripcion()
                            , cat.getCodigo()
                            , null);
                    //db.flush();
                    Integer catId = findCategoryByCode(tCategories, cat.getCodigo());
                    this.categoryMap.put(cat, catId);
                }
            } catch (Exception e) {
                log.error("storing criteria");
            }
        }
        try {
            db.flush();
        } catch (Exception e) {
            log.error("storing criteria -final commit");
        }


    }

    private void storeObservationTimes(Database db, Registro registro, Table tTiempos, Table tTiemposCategoria) {
        try {
            String fileuri = "Not defined. Imported from Lince";
            String tipoDato = "EventoMultiModal";
            Date date = new Date();
            for (FilaRegistro entry : registro.datosVariables) {
                try {
                    tTiempos.addRow(Column.AUTO_NUMBER, entry.getRegisterFrameValue(), entry.getMilis(), entry.getRegisterFrameValue() + 1, entry.getMilis() + 1, fileuri, tipoDato, date.toString(), "00:00:01", 0);
                    Integer id = findFieldWithValue(tTiempos, entry.getRegisterFrameValue(), HoisanVars.TIME_INITIAL_FRAME, HoisanVars.TIME_ID);
                    //db.flush();
                    storeObservationDataItem(db, tTiemposCategoria, entry, id);
                } catch (Exception e) {
                    log.error("storing criteria");
                }
            }
        } catch (Exception e) {
            log.error("Storing observation data", e);
        }
    }

    /**
     * toma jeroma !!!
     *
     * @param data
     * @param node
     * @param <T>
     * @return
     */
    private <T extends DefaultMutableTreeNode> Integer getElementMapId(Map<T, Integer> data, T node) {
        for (Map.Entry<T, Integer> entry : data.entrySet()) {
            if (entry.getKey().equals(node)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private int storeObservationDataItem(Database db, Table tTiemposCategoria, FilaRegistro item, Integer itemId) {
        int counter = 0;
        for (Map.Entry<Criterio, Categoria> data : item.getRegistro().entrySet()) {
            try {
                Integer categoryId = getElementMapId(this.categoryMap, data.getValue());
                Integer criterioId = getElementMapId(this.criteriaMap, data.getKey());
                tTiemposCategoria.addRow(Column.AUTO_NUMBER, categoryId, itemId, criterioId, this.actorId, "Lince", 1);
                counter++;
                log.warn("Insertando Tiempo categoria " + counter);
                //db.flush();
            } catch (Exception e) {
                log.error("Storing observation data", e);
            }
        }
        return counter;
    }

    /**
     * Creates on selected File a valid Hoisan Empty File
     *
     * @param outputFile
     * @throws IOException
     */
    private File createValidEmptyHoisanFile(File outputFile) throws IOException {
        try (InputStream template = getClass().getClassLoader().getResourceAsStream("template/" + HOISAN_TEMPLATE_FILE)) {
            if (template != null) {
                FileUtils.copyInputStreamToFile(template, outputFile);
            } else {
                //buscamos en la misma ubicación del jar
                FileUtils.copyFile(new File("./template/" + HOISAN_TEMPLATE_FILE), outputFile);
            }
        }
        File rtn = new File(StringUtils.substringBeforeLast(outputFile.getPath(), ".") + ".mdb");
        outputFile.renameTo(rtn);
        return rtn;
    }


    /**
     * @param criterios
     * @param categorias
     * @return
     */
    private InstrumentoObservacional generateInstrumentObservacional(Table criterios, Table categorias) {
        InstrumentoObservacional.loadNewInstance();
        InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
        RootInstrumentoObservacional root = (RootInstrumentoObservacional) instrumentoObservacional.getModel().getRoot();
        DefaultMutableTreeNode fijo = (DefaultMutableTreeNode) root.getChildAt(0);
        DefaultMutableTreeNode variable = (DefaultMutableTreeNode) root.getChildAt(2);

        for (Map<String, Object> row : criterios) {
            String type = HoisanVars.CRITERIA_TYPE.getStringValue(row);
            if (HoisanVars.CRITERIA_TYPE_FIXED.equals(type)) {
                //type.equals("Fijo")) {
                InstrumentoObservacional.getInstance().addHijo(fijo
                        , HoisanVars.CRITERIA_NAME.getStringValue(row));
                NodoInformacion[] datos = instrumentoObservacional.getDatosFijos();
                datos[datos.length - 1].setDescripcion(HoisanVars.CRITERIA_DESCRIPTION.getStringValue(row));
            } else if (HoisanVars.CRITERIA_TYPE_NORMAL.equals(type) || LINCE_CRITERIA_TYPE.equals(type)) {
                //"Lince" is the type exportFile writes for every criterion
                //(type.equals("Normal")) {
                InstrumentoObservacional.getInstance().addHijo(variable, HoisanVars.CRITERIA_NAME.getStringValue(row));
                Criterio[] cs = instrumentoObservacional.getCriterios();
                Criterio criterio = cs[cs.length - 1];
                criterio.setDescripcion(HoisanVars.CRITERIA_DESCRIPTION.getStringValue(row));
                generateCategorias(criterio, HoisanVars.CRITERIA_ID.getIntValue(row), categorias);
            } else {
                // Ignore
            }
        }
        return instrumentoObservacional;
    }

    /**
     * @param criterio
     * @param criterioId
     * @param categorias
     */
    private void generateCategorias(Criterio criterio, Integer criterioId, Table categorias) {
        for (Map<String, Object> row : categorias) {
            String currentCriteriaFK = HoisanVars.CATEGORY_CRITERIA_ID.getStringValue(row);
            if (currentCriteriaFK.equals(String.valueOf(criterioId))) {
                InstrumentoObservacional.getInstance().addHijo(criterio, HoisanVars.CATEGORY_DESCRIPTION.getStringValue(row));
                Categoria categoria = (Categoria) criterio.getChildAt(criterio.getChildCount() - 1);
                categoria.setCodigo(HoisanVars.CATEGORY_NAME.getStringValue(row));
                categoria.setDescripcion(HoisanVars.CATEGORY_CORE.getStringValue(row));
            }
        }
    }

    /**
     * @param tiempos
     * @param tiempoCategorias
     * @param criterios
     * @param categorias
     * @param instrumentoObservacional
     * @return
     */
    private Map<String, Registro> generateRegistros(Table tiempos, Table tiempoCategorias, Table criterios, Table categorias, InstrumentoObservacional instrumentoObservacional) {
        Map<String, Registro> registros = new LinkedHashMap<String, Registro>();
        for (Map<String, Object> row : tiempos) {
            String nombre = HoisanVars.TIME_VIDEO_NAME.getStringValue(row);
            Integer timeID = HoisanVars.TIME_ID.getIntValue(row);
            Integer initialFrame = HoisanVars.TIME_INITIAL_FRAME.getIntValue(row);
            Registro registro = registros.get(nombre);
            if (registros.get(nombre) == null) {
                Registro.loadNewInstance();
                registro = Registro.getInstance();
                registros.put(nombre, registro);
            }
            Map<Criterio, Categoria> registerItem = generateRegisterRow(timeID
                    , tiempoCategorias
                    , criterios
                    , categorias
                    , instrumentoObservacional.getCriterios());
            registro.addRow(initialFrame * FRAME_WINDOW
                    , registerItem
                    , new HashMap<NodoInformacion, String>());
        }
        return registros;
    }

    /**
     * @param timeId
     * @param tiempoCategorias
     * @param criterioTable
     * @param categoriaTable
     * @param criterios
     * @return
     */
    private Map<Criterio, Categoria> generateRegisterRow(Integer timeId, Table tiempoCategorias, Table criterioTable, Table categoriaTable, Criterio criterios[]) {
        Map<Criterio, Categoria> rtn = new HashMap<Criterio, Categoria>();
        for (Map<String, Object> row : tiempoCategorias) {
            final Integer currentCriteriaID = HoisanVars.CRITERIA_ID.getIntValue(row);
            final Integer currentCategoryID = HoisanVars.CATEGORY_ID.getIntValue(row);
            final Integer currentTimeID = HoisanVars.TIME_ID.getIntValue(row);
            if (currentTimeID.equals(timeId)) {
                Criterio criteria = findCriterio(criterios, criterioTable, currentCriteriaID);
                if (criteria != null) {
                    Categoria ca = findCategory(criteria, categoriaTable, currentCategoryID);
                    rtn.put(criteria, ca);
                }
            }
        }
        return rtn;
    }

    /**
     * @param cs
     * @param criterios
     * @param id
     * @return
     */
    private Criterio findCriterio(Criterio cs[], Table criterios, int id) {
        String nombre = findCriteriaById(criterios, id);
        for (Criterio c : cs) {
            if (StringUtils.equals(c.getNombre(), nombre)) {
                return c;
            }
        }
        return null;
    }

    /**
     * @param c
     * @param categoriaTable
     * @param id
     * @return
     */
    private Categoria findCategory(Criterio c, Table categoriaTable, Integer id) {
        String code = findCategoryById(categoriaTable, id);
        return c.getCategoriaByCodigo(code);
    }


    /**
     * Devuelve la etiqueta de un id para criterios
     *
     * @param criteriaTable
     * @param id
     * @return
     */
    private String findCriteriaById(Table criteriaTable, int id) {
        return findFieldWithValue(criteriaTable, id, HoisanVars.CRITERIA_ID, HoisanVars.CRITERIA_NAME);
    }

    private Integer findCriteriaByName(Table criteriaTable, String name) {
        return findFieldWithValue(criteriaTable, name, HoisanVars.CRITERIA_NAME, HoisanVars.CRITERIA_ID);
    }

    /**
     * Devuelve la etiqueta de un id para categorias
     *
     * @param categoriaTable
     * @param id
     * @return
     */
    private String findCategoryById(Table categoriaTable, Integer id) {
        return findFieldWithValue(categoriaTable, id, HoisanVars.CATEGORY_ID, HoisanVars.CATEGORY_NAME);
    }

    private Integer findCategoryByCode(Table categoriaTable, String code) {
        return findFieldWithValue(categoriaTable, code, HoisanVars.CATEGORY_CORE, HoisanVars.CATEGORY_ID);
    }

    /**
     * Metodo generico para buscar una etiqueta en una table
     *
     * @param table
     * @param id
     * @param idField
     * @param labelField
     * @return
     */
    private <T> T findFieldWithValue(Table table, Object id, HoisanVars idField, HoisanVars labelField) {
        try {
            CursorBuilder cursorBuilder = new CursorBuilder(table);
            Cursor cursor = cursorBuilder.toCursor();
            while (cursor.findNextRow(Collections.singletonMap(idField.toString(), id))) {
                Map<String, Object> rowi = cursor.getCurrentRow();
                if (rowi != null && rowi.get(labelField.toString()) != null) {
                    return (T) rowi.get(labelField.toString());
                }
            }
        } catch (Exception e) {
            log.error("Err", e);
        }
        //metodo lento
        for (Map<String, Object> row : table) {
            try {
                Object aux = row.get(idField.toString());
                if (aux != null && aux.equals(id)) {
                    return (T) row.get(labelField.toString());
                }
            } catch (Exception e) {
                log.error("Err", e);
            }
        }
        return null;
    }
}
