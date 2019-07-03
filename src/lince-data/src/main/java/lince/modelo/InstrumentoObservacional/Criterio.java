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
package lince.modelo.InstrumentoObservacional;

import com.deicos.lince.data.bean.wrapper.LinceRegisterWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Brais
 */
public class Criterio extends DefaultMutableTreeNode {

    private String nombre = "";
    private boolean persistente = false;
    private String descripcion = "";

    public Criterio() {
    }

    public Criterio(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        InstrumentoObservacional.getInstance().treeNodesChanged(null);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        InstrumentoObservacional.getInstance().treeNodesChanged(null);
    }

    public boolean isPersistente() {
        return persistente;
    }

    public void setPersistente(boolean persistente) {
        this.persistente = persistente;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public Categoria getCategoriaByCodigo(String codigo) {
        int tam = getChildCount();
        Categoria categoria = null;
        for (int i = 0; i < tam && categoria == null; i++) {
            categoria = ((Categoria) getChildAt(i)).getCategoriaByCodigo(codigo);
        }
        return categoria;
    }

    public Categoria[] getCategoriasHijo() {
        int numCategorias = getChildCount();
        Categoria categorias[] = new Categoria[numCategorias];
        for (int i = 0; i < numCategorias; i++) {
            categorias[i] = (Categoria) getChildAt(i);
        }
        return categorias;
    }

    public List<Categoria> getCategoriasHoja() {
        int numCategorias = getChildCount();
        List<Categoria> categorias = new ArrayList<Categoria>();
        for (int i = 0; i < numCategorias; i++) {
            categorias.addAll(((Categoria) getChildAt(i)).getCategoriasHoja());
        }
        return categorias;
    }

    public boolean empty() {
        return this.isLeaf();
    }

    /**
     * We need to override to allow comparison inside Collections
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        try {
            if (obj.getClass().isAssignableFrom(Criterio.class)) {
                return StringUtils.equals(this.nombre, ((Criterio) obj).nombre);
            }
            return super.equals(obj);
        } catch (Exception e) {
            return false;
        }
    }
}
