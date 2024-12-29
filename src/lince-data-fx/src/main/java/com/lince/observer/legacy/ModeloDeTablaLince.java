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
package com.lince.observer.legacy;

import com.lince.observer.data.util.TimeCalculations;
import com.lince.observer.legacy.instrumentoObservacional.Categoria;
import com.lince.observer.legacy.instrumentoObservacional.Criterio;
import com.lince.observer.legacy.instrumentoObservacional.InstrumentoObservacional;
import com.lince.observer.legacy.instrumentoObservacional.NodoInformacion;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 *
 * @author Brais
 */
public class ModeloDeTablaLince extends AbstractTableModel {

    public List<FilaRegistro> datosVariables;
    protected NodoInformacion datosMixtos[];
    protected Criterio datos[];
    TimeCalculations timeCalculations = new TimeCalculations();

    public ModeloDeTablaLince() {
    }

    public ModeloDeTablaLince(List<FilaRegistro> datosVariables) {
        this.datosVariables = datosVariables;

        InstrumentoObservacional instrumentoObservacional = InstrumentoObservacional.getInstance();
        this.datos = instrumentoObservacional.getCriterios();
        this.datosMixtos = instrumentoObservacional.getDatosMixtos();
    }

    public void removeRow(int indice) {
        datosVariables.remove(indice);
        fireTableRowsDeleted(indice, indice);
        necesarioSave();
    }

    public void removeRows(int i, int j) {
        for (; i <= j; i++) {
            datosVariables.remove(i);
        }
        fireTableRowsDeleted(i, j);
        necesarioSave();
    }

    public void cleanRows() {
        datosVariables.clear();
        fireTableDataChanged();
        necesarioSave();
    }

    @Override
    public int getRowCount() {
        return datosVariables.size();
    }

    @Override
    public int getColumnCount() {
        return (datos == null) ? 2 : datos.length + datosMixtos.length + 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int col) {
        FilaRegistro fila = datosVariables.get(rowIndex);
        switch (col) {
            case 0:
                return timeCalculations.formatTimeWithOptionalHours(fila.getMilis());
            case 1:
                return fila.getRegisterFrameValue();
            default:
                if (col - 2 < datos.length) {
                    Categoria categoria = fila.getCategoria(datos[col - 2]);
                    return (categoria == null) ? null : categoria.getCodigo();
                } else {
                    return fila.getDatoMixto(datosMixtos[col - 2 - datos.length]);
                }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex - 2 < datos.length) {
            Criterio criterio = datos[columnIndex - 2];
            datosVariables.get(rowIndex).setCategoria(criterio, criterio.getCategoriaByCodigo((String) aValue));
        } else {
            NodoInformacion nodoInformacion = datosMixtos[columnIndex - datos.length - 2];
            datosVariables.get(rowIndex).setCategoria(nodoInformacion, (String) aValue);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
        necesarioSave();
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return java.util.ResourceBundle.getBundle("i18n.Bundle").getString("SEGUNDOS");
            case 1:
                return java.util.ResourceBundle.getBundle("i18n.Bundle").getString("FRAMES");
            default:
                if (col - 2 < datos.length) {
                    return datos[col - 2].getNombre();
                } else {
                    return datosMixtos[col - 2 - datos.length].getNombre();
                }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 1 ? Number.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex >= 2;
    }

    public long getMilisOfRow(int selectedRow) {
        return datosVariables.get(selectedRow).getMilis();
    }

    protected void necesarioSave() {
    }
}
