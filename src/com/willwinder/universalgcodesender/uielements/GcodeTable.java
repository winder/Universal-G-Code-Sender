/*
 * Customized JTable with helpers to coordinate common operations.
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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class GcodeTable extends JTable {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("Persistent JTable Test");
        frame.setSize(300,200);
        frame.setBackground(Color.gray);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        frame.getContentPane().add(topPanel);

        JScrollPane scrollPane = new JScrollPane();
        //topPanel.add(scrollPane);

        String columns[] = {"Column 1", "Column 2", "Column 3", "Checkbox"};
        Class columnData[] = { String.class, String.class, String.class, Boolean.class};
        final GcodeTable table = new GcodeTable();
        scrollPane.setViewportView(table);

        final JTextArea text = new JTextArea();

        JButton addDataButton = new JButton();
        addDataButton.setText("Add num rows:");
        addDataButton.setEnabled(true);
        addDataButton.addActionListener(new java.awt.event.ActionListener() {
            int rowNum = 1;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                final int num = Integer.parseInt(text.getText());
                EventQueue.invokeLater(() -> {
                    for (int i = 0; i < num; i++) {
                        table.addRow(new GcodeCommand("Command " + rowNum++));
                        Thread.yield();
                    }
                });
            }
        });


        BoxLayout bl = new BoxLayout(topPanel, BoxLayout.Y_AXIS);
        topPanel.setLayout(bl);
        topPanel.add(addDataButton);
        topPanel.add(text);
        topPanel.add(scrollPane);

        frame.setVisible(true);
    }

    static final Logger logger = Logger.getLogger(GcodeTable.class.getName());

    //GcodeTableModel model = null;
    DefaultTableModel model = null;
    File persistTo = null;
    PrintWriter writer = null;
    int maxLines = -1;

    private boolean autoWindowScroll = false;
    private int offset = 0;
    private boolean first = true;
    
    final private static int COL_INDEX_COMMAND       = 0;
    final private static int COL_INDEX_ORIG_COMMAND  = 1;
    final private static int COL_INDEX_SENT          = 2;
    final private static int COL_INDEX_DONE          = 3;
    final private static int COL_INDEX_RESPONSE      = 4;

    static String[] columnNames = {
        Localization.getString("gcodeTable.command"),
        Localization.getString("gcodeTable.originalCommand"),
        Localization.getString("gcodeTable.sent"),
        Localization.getString("gcodeTable.done"),
        Localization.getString("gcodeTable.response")
    };
    static Class[] columnTypes =  {
        String.class,
        String.class,
        Boolean.class,
        Boolean.class,
        String.class
    };
    
    public GcodeTable() {
        //model = new GcodeTableModel(null, columnNames, columnTypes);
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int idx) {
                return columnTypes[idx];
            }
        };

        this.setModel(model);
        getTableHeader().setReorderingAllowed(false);
    }

    public void setPersistenceProperties(File f, int maxLines) {
        try {
            this.persistTo = f;
            writer = new PrintWriter(f, "UTF-8");
            writer.append(StringUtils.join(columnNames,","));
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to open file.", e);
        }
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        // This is totally bogus, but they look alright when I throw in a max
        // width for the boolean columns.
        setPreferredColumnWidths(new double[] {0.25, 0.3, 0.2, 0.2, 0.2} );

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
            offset = command.getCommandNumber() * -1;
            first = false;
        }
        model.addRow(new Object[]{
            command.getCommandString(),
            command.getOriginalCommandString(),
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
        String val = (String)model.getValueAt(row, COL_INDEX_COMMAND);
        if (!command.isComment() && commandString != model.getValueAt(row, COL_INDEX_COMMAND)) {
            System.out.printf("Row mismatch [%s] does not match row %d [%s].]\n", commandString, row, model.getValueAt(row, COL_INDEX_COMMAND) ) ;
        }

        model.setValueAt(command.isSent(),      row, COL_INDEX_SENT);
        model.setValueAt(command.isDone(),      row, COL_INDEX_DONE);
        model.setValueAt(command.getResponse(), row, COL_INDEX_RESPONSE);
        
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
