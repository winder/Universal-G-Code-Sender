/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.options;

import java.util.Collection;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author wwinder
 */
abstract class AbstractOptionsPanel extends JPanel {
    protected abstract class Option {
        public String option;
        public String description;
        Option(String name, String d) {
            option = name;
            description = d;
        }

        abstract Object getValue();
    }

    protected abstract class StringOption {
        String value = "";
        void setValue(String v) {
            value = v;
        }
        Object getValue() {
            return value;
        }
    }

    protected abstract class BoolOption {
        Boolean value = false;
        void setValue(Boolean v) {
            value = v;
        }
        Object getValue() {
            return value;
        }
    }

    abstract Collection<Option> getOptions();

    AbstractOptionsPanel() {
        initComponents();
        initOptionTable();
    }

    private void initOptionTable() {
        
    }

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea preferenceDescriptionTextArea;
    private javax.swing.JTable preferenceTable;


    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        preferenceTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        preferenceDescriptionTextArea = new javax.swing.JTextArea();

        preferenceTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {}
            },
            new String[] {}
    /*
            new String [] {
                "Title 1", "Title 2"
            }
*/
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        preferenceTable.setColumnSelectionAllowed(true);
        preferenceTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(preferenceTable);
        preferenceTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (preferenceTable.getColumnModel().getColumnCount() > 0) {
            preferenceTable.getColumnModel().getColumn(0).setResizable(false);
            /*
            preferenceTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(SenderPanel.class, "SenderPanel.preferenceTable.columnModel.title0")); // NOI18N
            preferenceTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(SenderPanel.class, "SenderPanel.preferenceTable.columnModel.title1")); // NOI18N
            */
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
