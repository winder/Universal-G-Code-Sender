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
package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An entity transfer handler to be able to support drag and drop for reordering
 * entities in a {@link EntitiesTree} component.
 *
 * @author Joacim Breiler
 */
public class EntityTransferHandler extends TransferHandler {
    private static final Logger LOGGER = Logger.getLogger(EntityTransferHandler.class.getSimpleName());

    private final DataFlavor dataFlavor;

    public EntityTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Entity[].class.getName() + "\"";
            dataFlavor = new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find class for " + Entity.class.getSimpleName(), e);
        }
    }

    private static boolean isSelectionInDestination(TransferSupport support) {
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        JTree tree = (JTree) support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());

        int[] rows = tree.getSelectionRows() != null ? tree.getSelectionRows() : new int[0];
        return Arrays.stream(rows).anyMatch(i -> i == dropRow);
    }

    private boolean isDestinationAChild(EntityGroup destinationGroup, TransferSupport support) {
        final JTree tree = (JTree) support.getComponent();

        int[] rows = tree.getSelectionRows() != null ? tree.getSelectionRows() : new int[0];
        for (int row : rows) {
            Entity sourceEntity = (Entity) tree.getPathForRow(row).getLastPathComponent();
            if (isEntityAChild(destinationGroup, sourceEntity)) {
                return true;
            }
        }

        return false;
    }

    private boolean isEntityAChild(EntityGroup entity, Entity destination) {
        if (destination instanceof EntityGroup) {
            EntityGroup destinationGroup = (EntityGroup) destination;
            return destinationGroup.containsChild(entity);
        }
        return false;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop() || !support.isDataFlavorSupported(dataFlavor)) {
            return false;
        }

        // Only allow drop to a group node
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        if (!(dl.getPath().getLastPathComponent() instanceof EntityGroup)) {
            return false;
        }

        // Do not allow a drop on the drag source selections
        if (isSelectionInDestination(support)) {
            return false;
        }

        // Prevent a moving parent node to its children node
        EntityGroup destinationGroup = (EntityGroup) dl.getPath().getLastPathComponent();
        if (isDestinationAChild(destinationGroup, support)) {
            return false;
        }

        support.setShowDropLocation(true);
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent source) {
        JTree tree = (JTree) source;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return null;
        }

        List<Entity> copies = new ArrayList<>();
        List<Entity> toRemove = new ArrayList<>();
        for (TreePath path : paths) {
            Entity entity = (Entity) path.getLastPathComponent();
            copies.add(entity.copy());
            toRemove.add(entity);
        }

        return new EntitiesTransferable(copies, toRemove, dataFlavor);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (!(data instanceof EntitiesTransferable)) {
            return;
        }

        if ((action & MOVE) != MOVE) {
            return;
        }

        JTree tree = (JTree) source;
        EntityTreeModel model = (EntityTreeModel) tree.getModel();
        Drawing drawing = model.getDrawing();

        for (Entity entity : ((EntitiesTransferable) data).getEntitiesToRemove()) {
            drawing.removeEntity(entity);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }

        try {
            Transferable transferable = support.getTransferable();

            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            EntityGroup parent = (EntityGroup) dest.getLastPathComponent();

            int index = childIndex;    // DropMode.INSERT
            if (childIndex == -1) {     // DropMode.ON
                index = parent.getChildren().size();
            }

            Entity[] entities = (Entity[]) transferable.getTransferData(dataFlavor);
            for (Entity entity : entities) {
                parent.addChild(entity, index++);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import data during entity transfer in drag and drop", e);
            return false;
        }

        return true;
    }
}
