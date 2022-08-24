/*
    Copyright 2016 Will Winder

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

import com.willwinder.universalgcodesender.uielements.IChanged;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * @author wwinder
 */
public abstract class AbstractOptionsPanel extends JPanel implements TableModelListener, ListSelectionListener {

    protected final IChanged changer;
    protected OptionTable optionTable;
    private JTextArea preferenceDescriptionTextArea;

    public AbstractOptionsPanel(IChanged change) {
        changer = change;
        initComponents();
        optionTable.getModel().addTableModelListener(this);
        optionTable.getSelectionModel().addListSelectionListener(this);
    }

    public abstract void load();

    public abstract void store();

    public abstract boolean valid();

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


    /**
     * Setup the UI.
     */
    private void initComponents() {
        optionTable = new OptionTable();
        JScrollPane jScrollPane1 = new JScrollPane();
        JScrollPane jScrollPane2 = new JScrollPane();

        jScrollPane1.setViewportView(optionTable);
        optionTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (optionTable.getColumnModel().getColumnCount() > 0) {
            optionTable.getColumnModel().getColumn(0).setResizable(false);
        }

        preferenceDescriptionTextArea = new JTextArea();
        preferenceDescriptionTextArea.setLineWrap(true);
        preferenceDescriptionTextArea.setWrapStyleWord(true);
        preferenceDescriptionTextArea.setColumns(20);
        preferenceDescriptionTextArea.setRows(5);
        jScrollPane2.setViewportView(preferenceDescriptionTextArea);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
                        .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }

    public abstract void cancel();

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (optionTable.getModel().getRowCount() > 0) {
            Option<?> option = optionTable.getOption(optionTable.getSelectedRow());
            preferenceDescriptionTextArea.setText(option.getDescription());
        }
    }
}
