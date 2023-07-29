/*
    Copyright 2023 Will Winder

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

import com.willwinder.ugs.nbm.workflow.actions.AddGcodeFileAction;
import com.willwinder.ugs.nbm.workflow.actions.RemoveGcodeFileAction;
import com.willwinder.ugs.nbm.workflow.dnd.WorkflowFileTransferHandler;
import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;
import com.willwinder.ugs.nbm.workflow.model.WorkflowTool;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class WorkflowPanel extends JPanel {
    public static final String ACTION_DELETE_ROW = "deleteRow";
    public static final String ACTION_ADD_ROW = "addRow";
    private final Set<WorkflowPanelListener> listeners = new HashSet<>();
    private WorkflowTableModel model;
    private JTable fileTable;
    private JPopupMenu popupMenu;

    public WorkflowPanel() {
        initComponents();
        initListeners();
        registerActions();
    }

    private void registerActions() {
        fileTable.getActionMap().put(ACTION_DELETE_ROW, new RemoveGcodeFileAction(this));
        fileTable.getActionMap().put(ACTION_ADD_ROW, new AddGcodeFileAction(this));
        fileTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE_ROW);
        fileTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), ACTION_DELETE_ROW);
    }

    private void initListeners() {
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int r = fileTable.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < fileTable.getRowCount()) {
                        fileTable.setRowSelectionInterval(r, r);
                        popupMenu.show(fileTable, e.getX(), e.getY());
                    } else {
                        fileTable.clearSelection();
                    }
                }
            }
        });

        ListSelectionModel cellSelectionModel = this.fileTable.getSelectionModel();
        cellSelectionModel.addListSelectionListener((event) -> {
            int selectedRow = fileTable.getSelectedRow();
            listeners.forEach(listener -> listener.onSelectedFile(model.get(selectedRow)));
        });
    }

    /**
     * Add a file to the table and automatically loads it
     *
     * @param gcodeFile the file to add to the workflow
     */
    public void addFileToWorkflow(File gcodeFile) {
        if (gcodeFile == null) {
            return;
        }

        WorkflowFile workflowFile = new WorkflowFile(gcodeFile, new WorkflowTool("default"));

        int rowIndex = model.findRow(workflowFile);
        if (rowIndex < 0) {
            model.addRow(workflowFile);
        }

        // Fire off the selection event to load the file.
        rowIndex = model.findRow(workflowFile);
        fileTable.setRowSelectionInterval(rowIndex, rowIndex);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        model = new WorkflowTableModel();
        fileTable = new JTable(model);
        fileTable.setMinimumSize(new java.awt.Dimension(100, 60));
        fileTable.setDragEnabled(true);
        fileTable.setDropMode(DropMode.INSERT);
        fileTable.setTransferHandler(new WorkflowFileTransferHandler());
        fileTable.setDefaultRenderer(File.class, new FileTableCellRenderer());
        fileTable.setRowHeight(24);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (fileTable.getColumnModel().getColumnCount() > 0) {
            fileTable.getColumnModel().getColumn(0).setHeaderValue(NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title0")); // NOI18N
            fileTable.getColumnModel().getColumn(1).setHeaderValue(NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title1")); // NOI18N
            fileTable.getColumnModel().getColumn(2).setHeaderValue(NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.fileTable.columnModel.title3")); // NOI18N
        }

        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.setMinimumSize(new java.awt.Dimension(100, 60));
        tableScrollPane.setViewportView(fileTable);

        popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem(new AddGcodeFileAction(this)));
        popupMenu.add(new JMenuItem(new RemoveGcodeFileAction(this)));

        setLayout(new BorderLayout());
        add(new WorkflowToolbar(this),  BorderLayout.NORTH);
        add(tableScrollPane,  BorderLayout.CENTER);
    }

    public void markAsComplete(File gcodeFile) {
        int fileIndex = model.findFileIndex(gcodeFile);
        if (fileIndex < 0) return;
        model.markAsComplete(fileIndex);
        listeners.forEach(l -> l.onSelectedFile(model.get(fileIndex)));
    }

    public int getFileIndex(File gcodeFile) {
        return model.findFileIndex(gcodeFile);
    }

    public int getFileCount() {
        return model.getRowCount();
    }

    public void selectFileIndex(int fileIndex) {
        fileTable.setRowSelectionInterval(fileIndex, fileIndex);
    }

    public String getToolName(int fileIndex) {
        return model.get(fileIndex).getTool().getName();
    }

    public void removeSelectedFile() {
        this.model.removeRow(fileTable.getSelectedRow());
    }

    public void addListener(WorkflowPanelListener listener) {
        listeners.add(listener);
    }

    public int getSelectedIndex() {
        if (fileTable == null) {
            return -1;
        }
        return fileTable.getSelectedRow();
    }

    public void resetWorkflow() {
        this.model.resetWorkflow();
    }
}
