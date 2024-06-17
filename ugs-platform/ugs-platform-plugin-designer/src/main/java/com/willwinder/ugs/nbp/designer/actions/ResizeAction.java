/*
    Copyright 2022-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.controls.ResizeUtils;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * ResizeAction implements an action where all the entities in a
 * given selection are resized.
 *
 * @author Joacim Breiler
 */
public class ResizeAction implements UndoableAction {
    private final Anchor anchor;
    private final List<Entity> entities;
    private final Size originalSize;
    private final Size newSize;

    /**
     * Resizes the entities from an anchor position
     *
     * @param entities     a list of entities that is being resized
     * @param anchor       the corner that is being anchored
     * @param originalSize the original size
     * @param newSize      the new size
     */
    public ResizeAction(List<Entity> entities, Anchor anchor, Size originalSize, Size newSize) {
        this.entities = new ArrayList<>(entities);
        this.originalSize = originalSize;
        this.newSize = newSize;
        this.anchor = anchor;
    }

    @Override
    public void redo() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addAll(entities);
        ResizeUtils.performScaling(entityGroup, anchor, originalSize, newSize);
    }

    @Override
    public void undo() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addAll(entities);
        ResizeUtils.performScaling(entityGroup, anchor, newSize, originalSize);
    }

    @Override
    public String toString() {
        return "resize entity";
    }
}
