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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Brais
 */
public class RootInstrumentoObservacional extends DefaultMutableTreeNode {

    private String nombre = "";
    private String descripcion = "";
    private String autor = "";
    private String ano = "";

    public RootInstrumentoObservacional() {
    }

    public RootInstrumentoObservacional(String nombre) {
        this.nombre = nombre;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
        InstrumentoObservacional.getInstance().treeNodesChanged(null);
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
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
}
