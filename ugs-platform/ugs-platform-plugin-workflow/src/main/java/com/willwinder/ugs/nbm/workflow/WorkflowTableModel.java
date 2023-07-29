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

import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;
import com.willwinder.ugs.nbm.workflow.model.WorkflowTool;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model that manages {@link WorkflowFile}
 *
 * @author Joacim Breiler
 */
public class WorkflowTableModel extends AbstractTableModel {

    private static final int COLUMN_FILENAME = 0;
    private static final int COLUMN_TOOLNAME = 1;
    private static final int COLUMN_COMPLETED = 2;
    public final List<WorkflowFile> fileList = new ArrayList<>();

    public void addRow(WorkflowFile workflowFile) {
        fileList.add(workflowFile);
        fireTableRowsInserted(fileList.size(), fileList.size());
    }

    @Override
    public int getRowCount() {
        return fileList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WorkflowFile workflowFile = fileList.get(rowIndex);
        switch (columnIndex) {
            case COLUMN_FILENAME:
                return workflowFile.getFile();

            case COLUMN_TOOLNAME:
                return workflowFile.getTool().getName();

            case COLUMN_COMPLETED:
                return workflowFile.isCompleted();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == COLUMN_TOOLNAME) {
            get(rowIndex).setTool(new WorkflowTool(aValue.toString()));
            fireTableCellUpdated(rowIndex, columnIndex);
        } else if (columnIndex == COLUMN_COMPLETED) {
            get(rowIndex).setCompleted((Boolean) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLUMN_FILENAME:
                return File.class;

            case COLUMN_TOOLNAME:
                return String.class;

            case COLUMN_COMPLETED:
                return Boolean.class;
        }

        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 || columnIndex == 2;
    }

    public int findRow(WorkflowFile workflowFile) {
        return fileList.indexOf(workflowFile);
    }

    public void removeRow(int row) {
        if (row > fileList.size()) {
            return;
        }

        fileList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void moveRow(int rowIndex, int toIndex) {
        WorkflowFile moveRow = fileList.get(rowIndex);

        if (toIndex > rowIndex) {
            fileList.remove(rowIndex);
            fileList.add(toIndex - 1, moveRow);
        } else {
            fileList.remove(rowIndex);
            fileList.add(toIndex, moveRow);
        }

        fireTableRowsUpdated(Math.min(rowIndex, toIndex), Math.max(rowIndex, toIndex));
    }

    public WorkflowFile get(int index) {
        if (index < 0 || index > fileList.size()) {
            return null;
        }

        return fileList.get(index);
    }

    /**
     * Look for the provided file in the file table.
     *
     * @param file the file to look for
     * @return Row index of the provided file or -1 if not found.
     */
    public int findFileIndex(File file) {
        return fileList.stream().filter(f -> f.getFile().getAbsolutePath().equals(file.getAbsolutePath()))
                .findFirst()
                .map(fileList::indexOf)
                .orElse(-1);
    }

    public void markAsComplete(int fileIndex) {
        get(fileIndex).setCompleted(true);
        fireTableCellUpdated(fileIndex, 2);
    }

    public void resetWorkflow() {
        fileList.forEach(f -> f.setCompleted(false));
        fireTableRowsUpdated(0, fileList.size());
    }
}
