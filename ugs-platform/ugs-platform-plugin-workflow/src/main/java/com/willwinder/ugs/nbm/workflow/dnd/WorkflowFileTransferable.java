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

import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

/**
 * A transferable for supporting drag and drop reordering a {@link WorkflowFile}
 *
 * @author Joacim Breiler
 */
public class WorkflowFileTransferable implements Transferable {
    private final List<WorkflowFile> entitiesToAdd;
    private final DataFlavor flavor;

    public WorkflowFileTransferable(List<WorkflowFile> toAdd, DataFlavor flavor) {
        this.entitiesToAdd = toAdd;
        this.flavor = flavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);

        return entitiesToAdd.toArray(new WorkflowFile[0]);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.flavor.equals(flavor);
    }
}
