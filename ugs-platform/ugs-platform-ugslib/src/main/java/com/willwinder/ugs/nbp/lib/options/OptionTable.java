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

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wwinder
 */
public class OptionTable extends JTable {
    private final OptionTableModel model;
    private final List<Class> types;
    private ArrayList<TableCellRenderer> editors = new ArrayList<>();

    // Class type of cell being edited.
    private Class editingClass = null;

    public OptionTable() {
        types = new ArrayList<>();
        model = new OptionTableModel();
        setRowHeight(20);
        super.setModel(model);
        super.setColumnSelectionAllowed(true);
        super.getTableHeader().setReorderingAllowed(false);
    }

    public void addRow(Option<?> o) {
        model.addRow(o);
        types.add(o.getValue().getClass());
        editors.add(null);
    }

    public void clear() {
        model.clear();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        editingClass = null;

        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            if (editors.get(row) != null) return editors.get(row);

            // TODO: When I have a color type check for that to create a custom
            //       color renderer. Also language combo box?
            Class rowClass = getModel().getValueAt(row, modelColumn).getClass();
            if (rowClass == java.awt.Color.class) {
                return new ColorRenderer(true);
            } else if (rowClass == ComboRenderer.class) {
                ComboRenderer box = (ComboRenderer) getModel().getValueAt(row, modelColumn);
                editors.set(row, box);
                return box;
            }
            return getDefaultRenderer(rowClass);
        } else {
            return super.getCellRenderer(row, column);
        }
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        editingClass = null;
        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            // TODO: When I have a color type check for that to create a custom
            //       color picker. Also language combo box?
            editingClass = getModel().getValueAt(row, modelColumn).getClass();
            if (editingClass == java.awt.Color.class) {
                return new ColorEditor();
            } else if (editingClass == ComboRenderer.class) {
                ComboRenderer box = (ComboRenderer) getModel().getValueAt(row, modelColumn);
                return new DefaultCellEditor(box);
            }
            return getDefaultEditor(editingClass);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    @Override
    public Class getColumnClass(int column) {
        return editingClass != null ? editingClass : super.getColumnClass(column);
    }


    public Option<?> getOption(int index) {
        return model.get(index);
    }
}
