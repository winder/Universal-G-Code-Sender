package com.willwinder.ugs.nbp.lib.options;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComboRenderer extends JComboBox
                           implements TableCellRenderer {
  public ComboRenderer() {
      super();
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
      boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }

    setSelectedItem(value);
    return this;
  }
}