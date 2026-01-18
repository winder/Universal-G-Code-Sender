/*
    Copyright 2021-2026 Joacim Breiler

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
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A entity tree model that listens to events for keeping the
 * tree updated
 *
 * @author Joacim Breiler
 */
public class EntityTreeModel implements TreeModel, ControllerListener, EntityListener {
    private static final Logger LOGGER = Logger.getLogger(EntityTreeModel.class.getName());

    private final Controller controller;
    private final Set<TreeModelListener> treeModelListeners = Sets.newConcurrentHashSet();

    public EntityTreeModel(Controller controller) {
        this.controller = controller;
        controller.addListener(this);
        controller.getDrawing().getRootEntity().addListener(this);
        fireFullReload();
    }

    public void release() {
        controller.removeListener(this);
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

    public void notifyTreeStructureChanged(Object object) {
        TreeModelEvent e = new TreeModelEvent(this,
                new Object[]{object});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.NEW_DRAWING) {
            fireFullReload();
            controller.getDrawing().getRootEntity().addListener(this);
        }
    }

    public Drawing getDrawing() {
        return controller.getDrawing();
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        switch (entityEvent.getType()) {
            case CHILD_ADDED -> entityEvent.getParent()
                    .filter(p -> p instanceof EntityGroup)
                    .map(p -> (EntityGroup) p)
                    .ifPresent(parent -> fireChildAdded(parent, entityEvent.getTarget(), parent.getChildren().indexOf(entityEvent.getTarget())));

            case CHILD_REMOVED -> entityEvent.getParent()
                    .filter(p -> p instanceof EntityGroup)
                    .map(p -> (EntityGroup) p)
                    .ifPresent(parent -> fireChildRemoved(parent, entityEvent.getTarget(), parent.getChildren().indexOf(entityEvent.getTarget())));
        }
    }

    private void fireFullReload() {
        fireOnEdt(() -> {
            TreeModelEvent e = new TreeModelEvent(
                    this,
                    new Object[]{getRoot()}
            );
            treeModelListeners.forEach(l -> l.treeStructureChanged(e));
        });
    }

    private void fireOnEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private void fireChildAdded(EntityGroup parent, Entity target, int index) {
        if (index < 0) {
            LOGGER.warning("Entity does not exist in parent " + parent + " > " + target);
            return;
        }

        fireOnEdt(() -> {
            TreeModelEvent e = new TreeModelEvent(this,
                    buildTreeTo(parent), new int[]{index}, new Object[]{target});
            for (TreeModelListener tml : treeModelListeners) {
                tml.treeNodesInserted(e);
            }
        });
    }

    private void fireChildRemoved(EntityGroup parent, Entity target, int index) {
        if (index < 0) {
            LOGGER.warning("Entity does not exist in parent " + parent + " > " + target);
            return;
        }

        fireOnEdt(() -> {
            TreeModelEvent e = new TreeModelEvent(this,
                    buildTreeTo(parent), new int[]{index}, new Object[]{target});
            for (TreeModelListener tml : treeModelListeners) {
                try {
                    tml.treeNodesRemoved(e);
                } catch (Exception ex) {
                    LOGGER.log(Level.INFO, "Could not delete node " + target + " from tree");
                }
            }
        });
    }

    private TreePath buildTreeTo(Entity target) {
        LinkedList<Entity> path = new LinkedList<>();
        path.add(target);

        Optional<EntityGroup> parentFor = controller.getDrawing().getRootEntity().findParentFor(target);
        parentFor.ifPresent(path::addFirst);
        EntityGroup current = parentFor.orElse(null);
        while (current != null) {
            parentFor = controller.getDrawing().getRootEntity().findParentFor(current);
            parentFor.ifPresent(path::addFirst);
            current = parentFor.orElse(null);
        }

        return new TreePath(path.toArray());
    }
}
