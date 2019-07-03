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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Brais
 */
public class Categoria extends DefaultMutableTreeNode {

    private String nombre = "";
    private String codigo = "";
    private String descripcion = "";

    public Categoria() {
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
        if (StringUtils.isEmpty(this.codigo)){
            String codeName = StringUtils.EMPTY;
            for (String item: nombre.split(" ")){
                codeName+= item.substring(0,1);
            }
            this.codigo = codeName;
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
        InstrumentoObservacional.getInstance().treeNodesChanged(null);
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

    @Override
    public String toString() {
        return nombre;
    }

    Categoria getCategoriaByCodigo(String codigoABuscar) {
        int tam = getChildCount();
        Categoria categoria = null;
        if (codigo != null && tam == 0) {
            categoria = codigo.equalsIgnoreCase(codigoABuscar) ? this : null;
        }
        for (int i = 0; i < tam && categoria == null; i++) {
            categoria = ((Categoria) getChildAt(i)).getCategoriaByCodigo(codigoABuscar);
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
        List<Categoria> categorias = new ArrayList<Categoria>();
        if (isLeaf()) {
            categorias.add(this);
        } else {
            int numCategorias = getChildCount();
            for (int i = 0; i < numCategorias; i++) {
                categorias.addAll(((Categoria) getChildAt(i)).getCategoriasHoja());
            }
        }
        return categorias;
    }
}
