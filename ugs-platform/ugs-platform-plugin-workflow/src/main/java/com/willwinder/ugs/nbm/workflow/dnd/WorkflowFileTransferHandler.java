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
package com.willwinder.ugs.nbm.workflow.dnd;

import com.willwinder.ugs.nbm.workflow.WorkflowTableModel;
import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An entity transfer handler to be able to support drag and drop for reordering
 * entities in a {@link WorkflowTableModel}.
 *
 * @author Joacim Breiler
 */
public class WorkflowFileTransferHandler extends TransferHandler {
    private static final Logger LOGGER = Logger.getLogger(WorkflowFileTransferHandler.class.getSimpleName());

    private final DataFlavor dataFlavor;

    public WorkflowFileTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + WorkflowFile[].class.getName() + "\"";
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class for " + WorkflowFile.class.getSimpleName(), e);
        }
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop() || !support.isDataFlavorSupported(dataFlavor)) {
            return false;
        }

        support.setShowDropLocation(true);
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent source) {
        JTable table = (JTable) source;
        WorkflowTableModel model = (WorkflowTableModel) table.getModel();

        List<WorkflowFile> copies = new ArrayList<>();
        WorkflowFile entity = model.get(table.getSelectedRow());
        copies.add(entity);

        return new WorkflowFileTransferable(copies, dataFlavor);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            Transferable transferable = support.getTransferable();
            JTable table = (JTable) support.getComponent();
            WorkflowTableModel model = (WorkflowTableModel) table.getModel();

            JTable.DropLocation dl = (JTable.DropLocation) support.getDropLocation();
            int childIndex = dl.getRow();

            if (childIndex > model.getRowCount()) {
                return false;
            }

            WorkflowFile[] entities = (WorkflowFile[]) transferable.getTransferData(dataFlavor);
            for (WorkflowFile entity : entities) {
                model.moveRow(model.findRow(entity), childIndex);
            }

            int row = model.findRow(entities[0]);
            table.setRowSelectionInterval(row, row);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import data during entity transfer in drag and drop", e);
            return false;
        }

        return true;
    }
}
