/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author wwinder
 */
public class OptionTable extends JTable {
    private DefaultTableModel model;
    private List<Class> types;

    // Class type of cell being edited.
    private Class editingClass = null;

    public OptionTable() {
        types = new ArrayList<>();
        model = new DefaultTableModel(new String[] { "Setting", "Value" },0) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }
        };

        super.setModel(model);
        super.setColumnSelectionAllowed(true);
        super.getTableHeader().setReorderingAllowed(false);
    }

    public void addRow(Option o) {
        model.addRow(new Object[]{o.option, o.getValue()});
        types.add(o.getValue().getClass());
    }

    public void clear() {
        while (model.getRowCount()>0){
            model.removeRow(0);
        }
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column)
    {
        editingClass = null;

        int modelColumn = convertColumnIndexToModel(column);
        if (modelColumn == 1) {
            // TODO: When I have a color type check for that to create a custom
            //       color renderer. Also language combo box?
            Class rowClass = getModel().getValueAt(row, modelColumn).getClass();
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
            return getDefaultEditor(editingClass);
        } else {
            return super.getCellEditor(row, column);
        }
    }

    @Override
    public Class getColumnClass(int column) {
        return editingClass != null ? editingClass : super.getColumnClass(column);
    }


    protected static abstract class Option {
        public String option;
        public String description;
        Option(String name, String d) {
            option = name;
            description = d;
        }
        abstract void setValue(Object v);

        abstract Object getValue();
    }

    public static class StringOption extends Option {
        String value;
        public StringOption(String name, String d, String v) {
            super(name, d);
            value = v;
        }
        @Override
        public void setValue(Object o) {
            if (o instanceof String) {
                value = (String) o;
            } else {
                throw new IllegalArgumentException("Wrong type.");
            }
        }
        @Override
        Object getValue() {
            return value;
        }
    }

    public static class BoolOption extends Option {
        Boolean value;
        public BoolOption(String name, String d, Boolean v) {
            super(name, d);
            value = v;
        }
        @Override
        public void setValue(Object o) {
            if (o instanceof Boolean) {
                value = (Boolean) o;
            } else {
                throw new IllegalArgumentException("Wrong type.");
            }
        }
        @Override
        Object getValue() {
            return value;
        }
    }

    public static class IntOption extends Option {
        Integer value;
        public IntOption(String name, String d, Integer v) {
            super(name, d);
            value = v;
        }
        @Override
        public void setValue(Object o) {
            if (o instanceof Integer) {
                value = (Integer) o;
            } else {
                throw new IllegalArgumentException("Wrong type.");
            }
        }
        @Override
        Object getValue() {
            return value;
        }
    }

    public static class DoubleOption extends Option {
        Double value;
        public DoubleOption(String name, String d, Double v) {
            super(name, d);
            value = v;
        }
        @Override
        public void setValue(Object o) {
            if (o instanceof Double) {
                value = (Double) o;
            } else {
                throw new IllegalArgumentException("Wrong type.");
            }
        }
        @Override
        Object getValue() {
            return value;
        }
    }
}
