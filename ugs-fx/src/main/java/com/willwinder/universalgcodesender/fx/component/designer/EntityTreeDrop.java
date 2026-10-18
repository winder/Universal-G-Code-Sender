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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;

import java.util.List;

/**
 * Where a drag over a row of the {@link EntityTreeView} would put the dragged entities, kept free
 * of JavaFX so the rules can be unit tested.
 */
final class EntityTreeDrop {

    // The share of a group row, at its top and bottom, that inserts next to it instead of into it.
    private static final double EDGE_SHARE = 0.25;

    enum Position {
        BEFORE, INTO, AFTER
    }

    record Target(EntityGroup parent, int index) {
    }

    private EntityTreeDrop() {
    }

    /**
     * A group row drops into the group over its middle and next to it near its edges. Any other
     * row only inserts before or after itself.
     */
    static Position positionFor(boolean isGroup, double y, double height) {
        if (isGroup) {
            if (y < height * EDGE_SHARE) {
                return Position.BEFORE;
            }
            if (y > height * (1 - EDGE_SHARE)) {
                return Position.AFTER;
            }
            return Position.INTO;
        }
        return y < height / 2 ? Position.BEFORE : Position.AFTER;
    }

    static Target resolve(Position position, Entity entity, EntityGroup parent) {
        if (position == Position.INTO && entity instanceof EntityGroup group) {
            return new Target(group, group.getChildren().size());
        }
        int index = parent.getChildren().indexOf(entity);
        return new Target(parent, position == Position.AFTER ? index + 1 : index);
    }

    /**
     * Entities can not be dropped into themselves or into one of their own descendants.
     */
    static boolean canDrop(List<Entity> dragged, Target target) {
        if (dragged.isEmpty()) {
            return false;
        }
        for (Entity entity : dragged) {
            if (entity == target.parent()) {
                return false;
            }
            if (entity instanceof EntityGroup group && group.containsChild(target.parent())) {
                return false;
            }
        }
        return true;
    }
}
