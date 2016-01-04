/*
 * This class is mainly to satiet netbeans and its automatic code generation.
 */

/*
    Copywrite 2013 Will Winder

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

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author wwinder
 */

// class that extends the AbstractTableModel
class GcodeTableModel extends AbstractTableModel {

    ArrayList<Object[]> al;
    String[] header;
    Class[] types;
    
    GcodeTableModel(Object[][] obj, String[] header, Class[] types) {
        // save the header
        this.header = header;	
        // save the types
        this.types = types;
        // and the rows
        al = new ArrayList<>();
        // copy the rows into the ArrayList
        if (obj != null) {
            for(int i = 0; i < obj.length; ++i) {
                al.add(obj[i]);
            }
        }
    }
    // method that needs to be overload. The row count is the size of the ArrayList
    @Override
    public int getRowCount() {
        return al.size();
    }

    // method that needs to be overload. The column count is the size of our header
    @Override
    public int getColumnCount() {
        return header.length;
    }

    // method that needs to be overload. The object is in the arrayList at rowIndex
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return al.get(rowIndex)[columnIndex];
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
        al.add(row);
        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        al.get(row)[column] = aValue;
        fireTableCellUpdated(row, column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    void dropData() {
        al = new ArrayList<>();
        // inform the GUI that I have change
        fireTableDataChanged();
    }
}