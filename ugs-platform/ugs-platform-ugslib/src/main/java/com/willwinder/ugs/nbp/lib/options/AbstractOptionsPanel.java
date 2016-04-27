/*
    Copywrite 2016 Will Winder

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

import com.willwinder.ugs.nbp.lib.options.OptionTable.Option;
import com.willwinder.universalgcodesender.uielements.IChanged;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author wwinder
 */
public abstract class AbstractOptionsPanel extends JPanel implements TableModelListener {

    public abstract void load();
    public abstract void store();
    public abstract boolean valid();

    protected IChanged changer;

    public AbstractOptionsPanel(IChanged change) {
        changer = change;
        initComponents();
        optionTable.getModel().addTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        changer.changed();
    }

    /**
     * Call this in the subclasses to add rows to the panel.
     */
    protected void add(Option o) {
        optionTable.addRow(o);
    }

    /**
     * Remove all the options.
     */
    protected void clear() {
        optionTable.clear();
    }

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea preferenceDescriptionTextArea;
    public OptionTable optionTable;

    /**
     * Setup the UI.
     */
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        optionTable = new OptionTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        preferenceDescriptionTextArea = new javax.swing.JTextArea();

        jScrollPane1.setViewportView(optionTable);
        optionTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (optionTable.getColumnModel().getColumnCount() > 0) {
            optionTable.getColumnModel().getColumn(0).setResizable(false);
        }

        preferenceDescriptionTextArea.setColumns(20);
        preferenceDescriptionTextArea.setRows(5);
        jScrollPane2.setViewportView(preferenceDescriptionTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }
}
