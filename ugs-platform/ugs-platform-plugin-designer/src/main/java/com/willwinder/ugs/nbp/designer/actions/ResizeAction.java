/*
    Copyright 2022 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.controls.Location;
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
    private final Location location;
    private final List<Entity> entities;
    private final Size originalSize;
    private final Size newSize;

    public ResizeAction(List<Entity> entities, Location location, Size originalSize, Size newSize) {
        this.entities = new ArrayList<>(entities);
        this.location = location;
        this.originalSize = originalSize;
        this.newSize = newSize;
    }

    @Override
    public void redo() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addAll(entities);
        ResizeUtils.performScaling(entityGroup, location, originalSize, newSize);
    }

    @Override
    public void undo() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addAll(entities);
        ResizeUtils.performScaling(entityGroup, location, newSize, originalSize);
    }
}
