/*
    Copyright 2021 Will Winder

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

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.gui.DrawingListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityTreeModel implements TreeModel, ControllerListener, DrawingListener {
    private final Controller controller;
    private final Set<TreeModelListener> treeModelListeners = Sets.newConcurrentHashSet();

    public EntityTreeModel(Controller controller) {
        this.controller = controller;
        controller.addListener(this);
        controller.getDrawing().addListener(this);
    }

    @Override
    public Object getRoot() {
        return controller.getDrawing().getRootEntity();
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof EntityGroup) {
            return ((EntityGroup) parent).getChildren().get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof EntityGroup) {
            return ((EntityGroup) parent).getChildren().size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof EntityGroup);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        ((Entity) path.getLastPathComponent()).setName(newValue.toString());
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((EntityGroup) parent).getChildren().indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);
    }

    protected void fireTreeStructureChanged(Object object) {
        List<Entity> selection = controller.getSelectionManager().getSelection();
        TreeModelEvent e = new TreeModelEvent(this,
                new Object[]{object});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }

        // Restore old selection
        List<Entity> existingEntities = controller.getDrawing().getEntities();
        List<Entity> newSelection = selection.stream()
                .filter(existingEntities::contains)
                .collect(Collectors.toList());
        controller.getSelectionManager().setSelection(newSelection);
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        fireTreeStructureChanged(controller.getDrawing().getRootEntity());
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        fireTreeStructureChanged(controller.getDrawing().getRootEntity());
    }
}
