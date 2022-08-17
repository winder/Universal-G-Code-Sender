/*
    Copyright 2016-2022 Will Winder

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
package com.willwinder.ugs.nbp.lib.options;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wwinder
 */
public class OptionTableModel extends AbstractTableModel {
    private final List<Option<?>> options;

    public OptionTableModel() {
        options = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        synchronized (options) {
            return options.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Setting";
            case 1:
                return "Value";
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Object.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Option<?> option = options.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return option.localized;
            case 1:
                return option.getValue();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Option<?> option = options.get(rowIndex);
        if (columnIndex == 1) {
            option.setRawValue(aValue);
        }
    }

    public Option<?> get(int index) {
        return options.get(index);
    }

    public void addRow(Option<?> o) {
        options.add(o);
        fireTableDataChanged();
    }

    public void removeRow(int index) {
        options.remove(index);
        fireTableDataChanged();
    }

    public void clear() {
        options.clear();
        fireTableDataChanged();
    }
}
