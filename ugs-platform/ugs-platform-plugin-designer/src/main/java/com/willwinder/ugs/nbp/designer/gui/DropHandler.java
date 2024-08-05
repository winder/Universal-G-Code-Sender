/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.ToolImportAction;
import static com.willwinder.ugs.nbp.designer.actions.ToolImportAction.FILE_NAME_EXTENSION_FILTERS;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Listens for drag'n'drop of known file types and imports it via the {@link ToolImportAction}.
 *
 * @author Joacim Breiler
 */
public class DropHandler implements DropTargetListener {
    private static Optional<String> getFilename(DataFlavor stringFlavor, Transferable transferable) {
        try {
            String filename = (String) transferable.getTransferData(stringFlavor);
            if (hasCorrectExtension(filename)) {
                return Optional.of(filename);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            // Never mind...
        }

        return Optional.empty();
    }

    private static boolean hasCorrectExtension(String filename) {
        return Arrays.stream(FILE_NAME_EXTENSION_FILTERS)
                .flatMap(s -> Arrays.stream(s.getExtensions()))
                .anyMatch(extension -> StringUtils.endsWithIgnoreCase(filename, extension));
    }

    private Optional<String> getFilename(DropTargetDragEvent dtde) {
        DataFlavor stringFlavor = DataFlavor.stringFlavor;
        if (!dtde.isDataFlavorSupported(stringFlavor)) {
            return Optional.empty();
        }

        Transferable transferable = dtde.getTransferable();
        return getFilename(stringFlavor, transferable);
    }

    private Optional<String> getFilename(DropTargetDropEvent dtde) {
        DataFlavor stringFlavor = DataFlavor.stringFlavor;
        if (!dtde.isDataFlavorSupported(stringFlavor)) {
            return Optional.empty();
        }

        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable transferable = dtde.getTransferable();
        return getFilename(stringFlavor, transferable);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        Optional<String> filename = getFilename(dtde);
        if (filename.isEmpty()) {
            dtde.rejectDrag();
            return;
        }

        dtde.acceptDrag(DnDConstants.ACTION_COPY);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        dragEnter(dtde);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // Not used
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // Not used
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Optional<String> filename = getFilename(dtde);
        if (filename.isEmpty()) {
            dtde.dropComplete(false);
            return;
        }

        ThreadHelper.invokeLater(() -> {
            File file = filename.map(File::new).get();
            ToolImportAction.readDesign(ControllerFactory.getController(), CentralLookup.getDefault().lookup(BackendAPI.class), file);
            dtde.dropComplete(true);
        });
    }
}