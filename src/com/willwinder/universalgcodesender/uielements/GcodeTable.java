/*
 * Customized JTable with helpers to coordinate common operations.
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

import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 *
 * @author wwinder
 */
public class GcodeTable extends JTable {
    private boolean autoWindowScroll = false;
    private int offset = 0;
    
    final private static int COL_INDEX_COMMAND  = 0;
    final private static int COL_INDEX_SENT     = 1;
    final private static int COL_INDEX_DONE     = 2;
    final private static int COL_INDEX_RESPONSE = 3;
    
    public GcodeTable() {
        getTableHeader().setReorderingAllowed(false);
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        // This is totally bogus, but they look alright when I throw in a max
        // width for the boolean columns.
        setPreferredColumnWidths(new double[] {0.55, 0.2, 0.2, 0.2} );

        getColumnModel().getColumn(COL_INDEX_SENT).setResizable(false);
        getColumnModel().getColumn(COL_INDEX_SENT).setMaxWidth(50);
        getColumnModel().getColumn(COL_INDEX_DONE).setResizable(false);
        getColumnModel().getColumn(COL_INDEX_DONE).setMaxWidth(50);
    }
    
    public void setAutoWindowScroll(boolean autoWindowScroll) {
        this.autoWindowScroll = autoWindowScroll;
    }

    /**
     * Delete all rows from the table.
     */
    public void clear() {
        while (getModel().getRowCount()>0){
            ((GcodeTableModel)this.getModel()).removeRow(0);
        }
        this.offset = 0;
    }
    
    /**
     * Update table with a GcodeCommand.
     */
    public void addRow(final GcodeCommand command) {
        String commandString = command.getCommandString();
        if (command.isComment())
            commandString = "; " + command.getComment();
        else if (command.hasComment())
            commandString += "; " + command.getComment();
            
        ((GcodeTableModel)this.getModel()).addRow(new Object[]{
            commandString,
            command.isSent(),
            command.isDone(),
            command.getResponse()});
        
        scrollTable(this.getRowCount());
    }
    
    /**
     * Update table with a GcodeCommand.
     */
    public void updateRow(final GcodeCommand command) {

        String commandString = command.getCommandString();
        int row = command.getCommandNumber() + offset;
        
        // Check for modified command string
        if (commandString != getModel().getValueAt(row, COL_INDEX_COMMAND)) {
            System.out.printf("Row mismatch [%s] does not match row %d [%s].]\n", commandString, row, getModel().getValueAt(row, COL_INDEX_COMMAND) ) ;
        }

        getModel().setValueAt(command.isSent(),      row, COL_INDEX_SENT);
        getModel().setValueAt(command.isDone(),      row, COL_INDEX_DONE);
        getModel().setValueAt(command.getResponse(), row, COL_INDEX_RESPONSE);
        
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

    public void setOffset() {
        setOffset(this.getRowCount());
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
