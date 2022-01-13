/*
    Copyright 2016-2018 Will Winder

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

package com.willwinder.ugs.nbm.workflow;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.Settings;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.Arrays;

import static com.willwinder.ugs.nbp.lib.services.LocalizingService.lang;

@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbm.workflow//WorkflowWindow//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WorkflowWindowTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = WorkflowWindowTopComponent.WorkflowWindowCategory, id = WorkflowWindowTopComponent.WorkflowWindowActionId)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:WorkflowWindow>",
        preferredID = "WorkflowWindowTopComponent"
)
/**
 * An interface to help organizes a multi-file gcode workflow.
 *
 * UGSEventListener - this is how a plugin can listen to UGS lifecycle events.
 * ListSelectionListener - listen for table selections.
 */
public final class WorkflowWindowTopComponent extends TopComponent implements UGSEventListener, ListSelectionListener {
    public final static String WorkflowWindowTitle = Localization.getString("platform.window.workflow", lang);
    public final static String WorkflowWindowTooltip = Localization.getString("platform.window.workflow.tooltip", lang);
    public final static String WorkflowWindowActionId = "com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent";
    public final static String WorkflowWindowCategory = LocalizingService.CATEGORY_WINDOW;

    // These are the UGS backend objects for interacting with the backend.
    private final Settings settings;
    private final BackendAPI backend;

    // This is used in most functions, so cache it here.
    private DefaultTableModel model;

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
      public Localizer() {
        super(WorkflowWindowCategory, WorkflowWindowActionId, WorkflowWindowTitle);
      }
    }


    /**
     * Initialize the WorkflowWindow, register with the UGS Backend and set some
     * of the required JTable settings.
     */
    public WorkflowWindowTopComponent() {
        setName(WorkflowWindowTitle);
        setToolTipText(WorkflowWindowTooltip);

        initComponents();

        // This is how to access the UGS backend and register the listener.
        // CentralLookup is used to get singleton instances of the UGS Settings
        // and BackendAPI objects.
        settings = CentralLookup.getDefault().lookup(Settings.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        // Only allow contiguous ranges of selections and register as a listener.
        this.fileTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        ListSelectionModel cellSelectionModel = this.fileTable.getSelectionModel();
        cellSelectionModel.addListSelectionListener(this);
    }

    /**
     * Events from backend. Take specific actions based on the control state.
     * File state change - FILE_LOADED: Add the file to the workflow, always do this if the workflow page is loaded.
     * File state change - FILE_STREAM_COMPLETE: When the file send job has finished.
     * 
     * @param cse the event
     */
    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) cse;
            if (fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                this.addFileToWorkflow(backend.getGcodeFile());
            } else if (fileStateEvent.getFileState() == FileState.FILE_STREAM_COMPLETE) {
                this.completeFile(backend.getGcodeFile());
            }
        }
    }

    /**
     * Call when a file's work has been completed to progress to the next step
     * of the work flow.
     * @param gcodeFile the file which is completing.
     */
    public void completeFile(File gcodeFile) {
        if (gcodeFile == null) return;

        // Make sure the file is loaded in the table.
        int fileIndex = findFileIndex(gcodeFile);
        if (fileIndex < 0) return;

        // Mark that it has been completed.
        model.setValueAt(true, fileIndex, 2);

        fileIndex++;
        String message;

        // Make sure there is another command left.
        if (fileIndex < fileTable.getRowCount()) {
            String nextTool = (String) model.getValueAt(fileIndex, 1);
            String messageTemplate =
                    "Finished sending '%s'.\n"
                  + "The next file uses tool '%s'\n"
                  + "Load tool and move machine to its zero location\n"
                  + "and click send to continue this workflow.";
            message = String.format(messageTemplate, gcodeFile.getName(), nextTool);

            // Select the next row, this will trigger a selection event.
            fileTable.setRowSelectionInterval(fileIndex, fileIndex);
            
        // Use a different message if we're finished.
        } else {
            message = "Finished sending the last file!";
        }

        // Display a notification.
        java.awt.EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(new JFrame(), message, 
                    "Workflow Event", JOptionPane.PLAIN_MESSAGE);
        });

    }

    /**
     * ListSelectionListener - load files when they are selected.
     * @param e 
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        int[] selectedRow = fileTable.getSelectedRows();
        // Only load files when there is a single selection.
        if (selectedRow.length == 1) {
            // Pull the file out of the table and set it in the backend.
            String file = (String) model.getValueAt(selectedRow[0], 0);
            try {
                backend.setGcodeFile(new File(file));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    /**
     * Add a file to the table.
     * @param gcodeFile 
     */
    public void addFileToWorkflow(File gcodeFile) {
        if (gcodeFile == null) {
            return;
        }

        int fileIndex = findFileIndex(gcodeFile);
        // Don't re-add a file.
        if (fileIndex >= 0) {
            return;
        }

        model.addRow(new Object[]{
                gcodeFile.getAbsolutePath(),
                "default",
                false
            });

        // Fire off the selection event to load the file.
        int lastRow = fileTable.getRowCount() - 1;
        fileTable.setRowSelectionInterval(lastRow, lastRow);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        tablePanel = new javax.swing.JPanel();
        upButton = new javax.swing.JButton();
        tableScrollPane = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();
        downButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Arrowhead-Up-01-32.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        tableScrollPane.setMinimumSize(new java.awt.Dimension(100, 60));

        fileTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Filename", "Toolname", "Finished"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        fileTable.setMinimumSize(new java.awt.Dimension(100, 60));
        tableScrollPane.setViewportView(fileTable);
        if (fileTable.getColumnModel().getColumnCount() > 0) {
            fileTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title0")); // NOI18N
            fileTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title1")); // NOI18N
            fileTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title3")); // NOI18N
        }

        downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Arrowhead-Down-01-32.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(upButton)
                    .addComponent(downButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(upButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(downButton))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE))
                    .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(settings.getLastOpenedFilename());

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
                File gcodeFile = fileChooser.getSelectedFile();
                settings.setLastOpenedFilename(gcodeFile.getParent());
                addFileToWorkflow(gcodeFile);
        }  

    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) return;

        Arrays.sort(selectedRows);
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            int row = selectedRows[i];
            this.model.removeRow(row);
            this.model.fireTableRowsDeleted(row, row);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int[] selectedRows = fileTable.getSelectedRows();

        // Exit early if nothing is selected.
        if (selectedRows.length == 0) return;

        Arrays.sort(selectedRows);

        // Exit early if the selected range can't move.
        if (selectedRows[0] == 0) return;

        for (int i = 0; i < selectedRows.length; i++) {
            selectedRows[i] = this.moveRow(selectedRows[i], -1);
        }
        int first = selectedRows[0];
        int last = selectedRows[selectedRows.length-1];
        fileTable.setRowSelectionInterval(first, last);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int[] selectedRows = fileTable.getSelectedRows();

        // Exit early if nothing is selected.
        if (selectedRows.length == 0) return;

        Arrays.sort(selectedRows);

        // Exit early if the selected range can't move.
        if (selectedRows[selectedRows.length-1] == fileTable.getRowCount()) return;

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            selectedRows[i] = this.moveRow(selectedRows[i], 1);
        }
        fileTable.setRowSelectionInterval(selectedRows[0], selectedRows[selectedRows.length-1]);
    }//GEN-LAST:event_downButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton downButton;
    private javax.swing.JTable fileTable;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    /**
     * NetBeans module overrides.
     */

    @Override
    public void componentOpened() {
        model = (DefaultTableModel)this.fileTable.getModel();
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /**
     * Helper functions.
     */
    
    /**
     * Look for the provided file in the file table.
     * @param gcodeFile
     * @return Row index of the provided file or -1 if not found.
     */
    private int findFileIndex(File gcodeFile) {
        if (gcodeFile == null) return -1;

        for (int i = 0; i < model.getRowCount(); i++) {
            String file = (String) model.getValueAt(i, 0);
            if (file != null && gcodeFile.getAbsolutePath().equals(file)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Move a given row by some offset. If the offset would move the row outside
     * of the current table size, the row is not moved.
     * @param row row to move.
     * @param offset how far to move row.
     * @return location of row after move.
     */
    private int moveRow(int row, int offset) {
        int dest = row + offset;
        if (dest < 0 || dest >= model.getRowCount()) {
            return row;
        }

        model.moveRow(row, row, dest);
        return dest;
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    public void readProperties(java.util.Properties p) {
        //String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
