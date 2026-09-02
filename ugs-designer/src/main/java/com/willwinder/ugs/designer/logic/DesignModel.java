/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.logic;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.DoubleConsumer;

/**
 * The entities of the design being edited and the snap to grid size, independent of any UI
 * toolkit. The Swing {@code Drawing} shows and edits this model; other front ends read and
 * change it directly.
 */
public class DesignModel {
    public static final double DEFAULT_SNAP_TO_GRID = 1;

    private final EntityGroup root = new EntityGroup();
    private final Set<DoubleConsumer> snapToGridListeners = new CopyOnWriteArraySet<>();
    private double snapToGrid = DEFAULT_SNAP_TO_GRID;

    /**
     * The group holding every top level entity. Listen to it for changes anywhere in the tree.
     */
    public EntityGroup getRootEntity() {
        return root;
    }

    /**
     * Every leaf entity in the tree, with groups flattened away.
     */
    public List<Entity> getEntities() {
        List<Entity> result = new ArrayList<>();
        root.getChildren().forEach(entity -> collectLeaves(entity, result));
        return result;
    }

    public List<Entity> getEntitiesAt(Point2D point) {
        return root.getChildrenAt(point);
    }

    public List<Entity> getEntitiesIntersecting(Shape shape) {
        return root.getChildrenIntersecting(shape);
    }

    public void insertEntity(Entity entity) {
        root.addChild(entity);
    }

    public void insertEntities(List<Entity> entities) {
        entities.forEach(root::addChild);
    }

    public void removeEntity(Entity entity) {
        removeEntities(Collections.singletonList(entity));
    }

    /**
     * Removes the entities wherever they sit in the tree, including inside groups.
     */
    public void removeEntities(List<Entity> entities) {
        removeRecursively(root, entities);
    }

    public void clear() {
        root.removeAll();
    }

    /**
     * The grid the tools snap to in millimeters, zero for no snapping.
     */
    public double getSnapToGrid() {
        return snapToGrid;
    }

    public void setSnapToGrid(double snapToGrid) {
        this.snapToGrid = snapToGrid;
        snapToGridListeners.forEach(listener -> listener.accept(snapToGrid));
    }

    public void addSnapToGridListener(DoubleConsumer listener) {
        snapToGridListeners.add(listener);
    }

    public void removeSnapToGridListener(DoubleConsumer listener) {
        snapToGridListeners.remove(listener);
    }

    private static void collectLeaves(Entity entity, List<Entity> result) {
        if (entity instanceof EntityGroup group) {
            group.getChildren().forEach(child -> collectLeaves(child, result));
        } else {
            result.add(entity);
        }
    }

    private static void removeRecursively(EntityGroup parent, List<Entity> entities) {
        parent.getChildren().forEach(child -> {
            if (child instanceof EntityGroup group) {
                removeRecursively(group, entities);
            }
        });
        parent.removeAll(entities);
    }
}
