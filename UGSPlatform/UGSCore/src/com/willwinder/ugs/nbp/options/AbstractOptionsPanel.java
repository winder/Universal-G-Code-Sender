/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.options;

import com.willwinder.ugs.nbp.options.OptionTable.Option;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 * @author wwinder
 */
abstract class AbstractOptionsPanel extends JPanel implements TableModelListener {

    AbstractOptionsPanel() {
        initComponents();
        optionTable.getModel().addTableModelListener(this);
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
