/*
 *  LINCE PLUS - Automatizacion de datos observacionales. Inherited from legacy Lince 1.2.
 *  Copyright (C) 2011  Brais Gabin Moreira
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
package com.lince.observer.data.legacy.datos;

import com.lince.observer.data.component.PackageAwareXMLSerializer;
import com.lince.observer.data.legacy.utiles.PathArchivos;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alberto Soto-Fernandez
 */
public class ControladorArchivos {

    private static ControladorArchivos instance = null;

    public static synchronized ControladorArchivos getInstance() {
        if (instance == null) {
            instance = new ControladorArchivos();
        }
        return instance;
    }

    private ControladorArchivos() {
    }

    public void guardar(File path, Object datos) throws FileNotFoundException {
//        deprecated part
//        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(path)));
//        e.setPersistenceDelegate(FilaRegistro.class, new DefaultPersistenceDelegate(new String[]{"milis", "registro", "datosMixtos"}));
//        e.writeObject(datos);
//        e.close();
        try {
            PackageAwareXMLSerializer.encodeToFile(datos, path);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Object cargar(File path) throws IOException {
        if (path == null || !path.exists()) {
            throw new FileNotFoundException("File does not exist: " + (path != null ? path.getAbsolutePath() : "null"));
        }

        if (!path.isFile()) {
            throw new IOException("Path is not a file: " + path.getAbsolutePath());
        }
        try (PackageAwareXMLSerializer decoder = new PackageAwareXMLSerializer(new BufferedInputStream(new FileInputStream(path)))) {
            Object result = decoder.readObject();
            if (result == null) {
                throw new IOException("Failed to read object from file: " + path.getAbsolutePath());
            }
            return result;
        } catch (FileNotFoundException e) {
            throw new IOException("File not found or cannot be read: " + path.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new IOException("Error reading from file: " + path.getAbsolutePath(), e);
        } catch (Exception e) {
            throw new IOException("Unexpected error while reading file: " + path.getAbsolutePath(), e);
        }
    }

    public void guardarSerializable(File path, Serializable datos) throws IOException {
        ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(path));
        salida.writeObject(datos);
        salida.close();
    }

    public Object cargarSerializable(File path) throws IOException, ClassNotFoundException {
        ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(path));
        Object data = entrada.readObject();

        entrada.close();

        return data;
    }

    public void crearArchivoDeTexto(File f, String contenido) {
        try {
            //"Windows-1252"
            //final String fileEncoding = "UTF-8";
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
            bw.write(contenido);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(PathArchivos.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
