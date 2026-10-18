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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;

import java.util.Comparator;
import java.util.List;

/**
 * Moves entities to a new place in the entity tree: another position among their siblings or into
 * another group. The entities keep their order and end up next to each other at the given index of
 * the target group, counted before any of them are taken out of it.
 *
 * @author Joacim Breiler
 */
public class ReorderEntitiesAction implements UndoableAction {

    private record Placement(Entity entity, EntityGroup parent, int index) {
    }

    private final List<Entity> entities;
    private final List<Placement> origins;
    private final EntityGroup target;
    private final int index;

    public ReorderEntitiesAction(EntityGroup root, List<Entity> entities, EntityGroup target, int index) {
        this.entities = List.copyOf(entities);
        this.target = target;
        this.index = index;
        this.origins = this.entities.stream()
                .map(entity -> placementOf(root, entity))
                .toList();
    }

    private static Placement placementOf(EntityGroup root, Entity entity) {
        EntityGroup parent = root.findParentFor(entity)
                .orElseThrow(() -> new IllegalArgumentException("The entity " + entity.getName() + " is not part of the design"));
        return new Placement(entity, parent, parent.getChildren().indexOf(entity));
    }

    @Override
    public void redo() {
        long movedFromBeforeIndex = origins.stream()
                .filter(origin -> origin.parent() == target && origin.index() < index)
                .count();
        int insertAt = index - (int) movedFromBeforeIndex;

        origins.forEach(origin -> origin.parent().removeChild(origin.entity()));
        for (Entity entity : entities) {
            target.addChild(entity, insertAt++);
        }
    }

    @Override
    public void undo() {
        entities.forEach(target::removeChild);
        origins.stream()
                .sorted(Comparator.comparingInt(Placement::index))
                .forEach(origin -> origin.parent().addChild(origin.entity(), origin.index()));
    }

    @Override
    public String toString() {
        return "move entities";
    }
}
