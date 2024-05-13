/*
 *  Lince - Automatizacion de datos observacionales
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

import com.lince.observer.data.legacy.utiles.PathArchivos;
import com.lince.observer.legacy.FilaRegistro;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brais
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
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(path)));
        e.setPersistenceDelegate(FilaRegistro.class, new DefaultPersistenceDelegate(new String[]{"milis", "registro", "datosMixtos"}));
        e.writeObject(datos);
        e.close();
    }

    public Object cargar(File path) throws FileNotFoundException {
        Object obj = null;
        XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(path)));
        obj = d.readObject();
        d.close();

        return obj;
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
