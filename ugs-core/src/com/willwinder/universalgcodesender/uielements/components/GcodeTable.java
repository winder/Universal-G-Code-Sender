/*
    Copyright 2013-2018 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Customized JTable with helpers to coordinate common operations.
 *
 * @author wwinder
 */
public class GcodeTable extends JTable {
    private GCodeTableModel model = null;

    private boolean autoWindowScroll = false;
    private int offset = 0;
    private boolean first = true;

    public GcodeTable() {
        model = new GCodeTableModel();

        this.setModel(model);
        getTableHeader().setReorderingAllowed(false);
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        // This is totally bogus, but they look alright when I throw in a max
        // width for the boolean columns.
        setPreferredColumnWidths(new double[] {0.25, 0.3, 0.2, 0.2, 0.2, 0.2, 0.2} );

        getColumnModel().getColumn(GCodeTableModel.COL_INDEX_SENT).setResizable(false);
        getColumnModel().getColumn(GCodeTableModel.COL_INDEX_SENT).setMaxWidth(50);
        getColumnModel().getColumn(GCodeTableModel.COL_INDEX_DONE).setResizable(false);
        getColumnModel().getColumn(GCodeTableModel.COL_INDEX_DONE).setMaxWidth(50);
    }
    
    public void setAutoWindowScroll(boolean autoWindowScroll) {
        this.autoWindowScroll = autoWindowScroll;
    }

    /**
     * Delete all rows from the table.
     */
    public void clear() {
        while (model.getRowCount()>0){
            model.removeRow(0);
        }
        //model.dropData();
        this.offset = 0;
        this.first = true;
    }
    
    /**
     * Update table with a GcodeCommand.
     */
    public void addRow(final GcodeCommand command) {
        if (first) {
            offset = command.getId() * -1;
            first = false;
        }
        model.add(command);
        
        scrollTable(this.getRowCount());
    }
    
    /**
     * Update table with a GcodeCommand.
     */
    public void updateRow(final GcodeCommand command) {
        int row = command.getId() + offset;
        scrollTable(row);
    }
    
    /**
     * Helper function to scroll table to specific row number.
     */
    private void scrollTable(int toRow) {
        // Scroll if selected.
        if (this.autoWindowScroll) {
            if (isVisible()) {
                scrollToVisible(toRow);
            }
        }
    }
    
    /**
     * Helper function to set preferred widths as a percentage.
     * http://stackoverflow.com/questions/1046005/jtable-column-resize-isnt-working
     */
    private void setPreferredColumnWidths(double[] percentages) {
        Dimension tableDim = getPreferredSize();

        double total = 0;

        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            total += percentages[i];
        }

        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth(
                 (int)(tableDim.width * (percentages[i] / total)));
        }
    }
    
    /**
     * Helper function to scroll table to specific row.
     */
    private void scrollToVisible(int rowIndex) {
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        scrollRectToVisible(new Rectangle(getCellRect(rowIndex, 0, true)));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
