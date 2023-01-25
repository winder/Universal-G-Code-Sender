/*
    Copyright 2021-2023 Will Winder

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
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class EntitiesTree extends JTree implements TreeSelectionListener, SelectionListener {

    private final transient Controller controller;

    public EntitiesTree(Controller controller, TreeModel treeModel) {
        super(treeModel);
        setEditable(true);
        setRootVisible(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setCellRenderer(new EntityCellRenderer());
        enableDragAndDrop();

        expandRow(0);
        ((EntityTreeModel) getModel()).notifyTreeStructureChanged(controller.getDrawing().getRootEntity());

        this.controller = controller;
        registerListeners();
    }

    private void registerListeners() {
        addTreeSelectionListener(this);
        addMouseListener(new EntitiesTreePopupListener());
        controller.getSelectionManager().addSelectionListener(this);
    }

    private void enableDragAndDrop() {
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new EntityTransferHandler());
    }

    public void release() {
        controller.getSelectionManager().removeSelectionListener(this);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Arrays.asList(e.getPaths()).forEach(p -> {
            Entity entity = (Entity) p.getLastPathComponent();
            if (e.isAddedPath(p) && !controller.getSelectionManager().isSelected(entity)) {
                controller.getSelectionManager().addSelection(entity);
            } else if (!e.isAddedPath(p) && controller.getSelectionManager().isSelected(entity)) {
                controller.getSelectionManager().removeSelection(entity);
            }
        });
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        EntityGroup rootEntity = controller.getDrawing().getRootEntity();
        List<TreePath> treePathList = EntityTreeUtils.getSelectedPaths(controller, rootEntity, Collections.emptyList());
        TreePath[] treePaths = treePathList.toArray(new TreePath[0]);
        setSelectionPaths(treePaths);
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // filter property change of "dropLocation" with newValue==null,
        // since this will result in a NPE in BasicTreeUI.getDropLineRect(...)
        if (newValue == null && "dropLocation".equals(propertyName)) {
            return;
        }

        super.firePropertyChange(propertyName, oldValue, newValue);
    }
}
