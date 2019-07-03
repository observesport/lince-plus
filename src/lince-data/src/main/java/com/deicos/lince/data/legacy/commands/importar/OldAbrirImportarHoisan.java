package com.deicos.lince.data.legacy.commands.importar;

import com.deicos.lince.data.legacy.Command;
import lince.modelo.InstrumentoObservacional.*;
import lince.modelo.Registro;
import com.deicos.lince.data.legacy.utiles.FiltroArchivos;
import com.deicos.lince.data.legacy.utiles.PathArchivos;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Created by deicos on 11/06/2015.
 */
public class OldAbrirImportarHoisan extends Command {
   public OldAbrirImportarHoisan() {
      putValue(Action.NAME, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("HOISAN"));
      putValue(Action.ACTION_COMMAND_KEY, "AbrirImportarHoisan");
      putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("i18n.Bundle").getString("IMPORATAR DESDE HOISAN"));
   }

   @Override
   public void execute() {
      List<FileFilter> fileFilters = new ArrayList<FileFilter>();
      fileFilters.add(new FiltroArchivos("mdb", java.util.ResourceBundle.getBundle("i18n.Bundle").getString("HOISAN")));
      File f = PathArchivos.getPathArchivoAbrir(fileFilters, null, null);
      if (f != null) {
         try {
            Database db = Database.open(f);

            Table criterios = db.getTable("Criterios");
            Table categorias = db.getTable("Categorias");
            InstrumentoObservacional instrumentoObservacional = generateInstrumentObservacional(criterios, categorias);
            Table tiempos = db.getTable("TiemposObservaciones");
            Table tiempoCategorias = db.getTable("TiemposCategoriasObservadas");
            Map<String, Registro> registros = generateRegistros(tiempos, tiempoCategorias, criterios, categorias, instrumentoObservacional);

            File parentFile = f.getParentFile();
            File linceDir = getFreeFile(parentFile, "Lince", "");
            linceDir.mkdir();

            instrumentoObservacional.setPath(getFreeFile(linceDir, f.getName(), ".ilince"));
            instrumentoObservacional.save();

            for (Entry<String, Registro> row : registros.entrySet()) {
               Registro registro = row.getValue();
               registro.setPath(getFreeFile(linceDir, new File(row.getKey()).getName(), ".rlince"));
               registro.save();
            }

         } catch (IOException ex) {
            Logger.getLogger(AbrirImportarHoisan.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   private File getFreeFile(File dir, String name, String extension) {
      File f = new File(dir, name + extension);
      int i = 2;
      while (f.exists()) {
         f = new File(dir, name + " (" + i++ + ")" + extension);
      }
      return f;
   }

   @Override
   public void unExecute() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   private InstrumentoObservacional generateInstrumentObservacional(Table criterios, Table categorias) {
      InstrumentoObservacional.loadNewInstance();
      InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
      RootInstrumentoObservacional root = (RootInstrumentoObservacional) instrumentoObservacional.getModel().getRoot();
      DefaultMutableTreeNode fijo = (DefaultMutableTreeNode) root.getChildAt(0);
      DefaultMutableTreeNode variable = (DefaultMutableTreeNode) root.getChildAt(2);

      for (Map<String, Object> row : criterios) {
         String type = (String) row.get("Tipo");
         if (type.equals("Fijo")) {
            InstrumentoObservacional.getInstance().addHijo(fijo, (String) row.get("Nombre_Criterio"));

            NodoInformacion[] datos = instrumentoObservacional.getDatosFijos();
            datos[datos.length - 1].setDescripcion((String) row.get("Descripcion"));
         } else if (type.equals("Normal")) {
            InstrumentoObservacional.getInstance().addHijo(variable, (String) row.get("Nombre_Criterio"));

            Criterio[] cs = instrumentoObservacional.getCriterios();
            Criterio criterio = cs[cs.length - 1];
            criterio.setDescripcion((String) row.get("Descripcion"));

            generateCategorias(criterio, (Integer) row.get("ID_Criterio"), categorias);
         } else {
            // Ignore
         }
      }
      return instrumentoObservacional;
   }

   private void generateCategorias(Criterio criterio, Integer criterioId, Table categorias) {
      for (Map<String, Object> row : categorias) {
         if (row.get("ID_Criterio_Referencial").equals(criterioId)) {
            InstrumentoObservacional.getInstance().addHijo(criterio, (String) row.get("Descripcion"));

            Categoria categoria = (Categoria) criterio.getChildAt(criterio.getChildCount() - 1);
            categoria.setCodigo((String) row.get("Nombre_Categoria"));
            categoria.setDescripcion((String) row.get("Nucleo_Categorial"));
         }
      }
   }

   private Map<String, Registro> generateRegistros(Table tiempos, Table tiempoCategorias, Table criterios, Table categorias, InstrumentoObservacional instrumentoObservacional) {
      Map<String, Registro> registros = new HashMap<String, Registro>();
      for (Map<String, Object> row : tiempos) {
         String nombre = (String) row.get("Nombre_Video");
         Registro registro = registros.get(nombre);
         if (registros.get(nombre) == null) {
            Registro.loadNewInstance();
            registro = Registro.getInstance();
            registros.put(nombre, registro);
         }
         registro.addRow(((Integer) row.get("TFramesInicial")) * 40, generateRegisterRow((Integer) row.get("ID_Tiempo"), tiempoCategorias, criterios, categorias, instrumentoObservacional.getCriterios()), new HashMap<NodoInformacion, String>());
      }
      return registros;
   }

   private Map<Criterio, Categoria> generateRegisterRow(Integer id, Table tiempoCategorias, Table criterioTable, Table categoriaTable, Criterio criterios[]) {
      Map<Criterio, Categoria> r = new HashMap<Criterio, Categoria>();
      for (Map<String, Object> row : tiempoCategorias) {
         if (row.get("ID_Tiempo").equals(id)) {
            Criterio c = findCriterio(criterios, criterioTable, (Integer) row.get("ID_Criterio"));
            if (c != null) { // TODO registrar los fijos
               Categoria ca = findCategoria(c, categoriaTable, (Integer) row.get("ID_Categoria"));
               r.put(c, ca);
            }
         }
      }
      return r;
   }

   private Criterio findCriterio(Criterio cs[], Table criterios, int id) {
      String nombre = findCreterioCode(criterios, id);
      for (Criterio c : cs) {
         if (c.getNombre().equals(nombre)) {
            return c;
         }
      }
      return null;
   }

   private String findCreterioCode(Table criterioTable, int id) {
      for (Map<String, Object> row : criterioTable) {
         if (row.get("ID_Criterio").equals(id)) {
            return (String) row.get("Nombre_Criterio");
         }
      }
      return null;
   }

   private Categoria findCategoria(Criterio c, Table categoriaTable, Integer id) {
      String code = findCategoriaCode(categoriaTable, id);

      return c.getCategoriaByCodigo(code);
   }

   private String findCategoriaCode(Table categoriaTable, Integer id) {
      for (Map<String, Object> row : categoriaTable) {
         if (row.get("ID_Categoria").equals(id)) {
            return (String) row.get("Nombre_Categoria");
         }
      }
      return null;
   }
}
