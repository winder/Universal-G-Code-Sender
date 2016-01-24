/**
 * A table model which persists data to a file, this is an attempt to have an
 * arbitrary sized table. It seems to add too much overhead though.
 */

/*
    Copywrite 2013-2016 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.uielements;

import javax.swing.table.AbstractTableModel;
import third_party.PersistentVector;

/**
 *
 * @author wwinder
 */
class GcodeTableModel extends AbstractTableModel {
    PersistentVector modelData;
    String[] header;
    Class[] types;
    
    /**
     * A TableModel which persists old data to the disk.
     * @param obj
     * @param header
     * @param types 
     */
    GcodeTableModel(Object[][] obj, String[] header, Class[] types) {
        // save the header
        this.header = header;	
        // save the types
        this.types = types;
        // and the rows
        modelData = new PersistentVector();

        // copy the rows into the ArrayList
        if (obj != null) {
            for(int i = 0; i < obj.length; ++i) {
                //modelData.add(obj[i]);
                modelData.addElement(obj[i]);
            }
        }
    }

    // method that needs to be overload. The row count is the size of the ArrayList
    @Override
    public int getRowCount() {
        return modelData.size();
    }

    // method that needs to be overload. The column count is the size of our header
    @Override
    public int getColumnCount() {
        return header.length;
    }

    // method that needs to be overload. The object is in the arrayList at rowIndex
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Object[])modelData.elementAt(rowIndex))[columnIndex];
        //return modelData.get(rowIndex)[columnIndex];
    }
    
    // a method to return the column name 
    @Override
    public String getColumnName(int index) {
        return header[index];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }
    
    void addRow(Object[] row) {
        int rowCount = getRowCount();
        modelData.addElement(row);
        fireTableRowsInserted(rowCount, rowCount);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        Object[] r = (Object[]) modelData.elementAt(row);
        r[column] = aValue;
        modelData.setElementAt(r, row);
        fireTableCellUpdated(row, column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    void dropData() {
        modelData = new PersistentVector();
        // inform the GUI that I have change
        fireTableDataChanged();
    }
}