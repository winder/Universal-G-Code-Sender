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
import com.willwinder.ugs.designer.utils.NestingUtils;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Packs entities within an area that starts at the bottom left corner of the entities, so they
 * take up as little stock as possible. The placements are worked out when the action is created
 * and nothing is moved until it is redone. The whole arrangement is one undo step.
 *
 * @author Joacim Breiler
 */
public class NestAction extends UndoActionList {
    private final NestingUtils.NestingLayout layout;

    /**
     * @param entities      the entities to pack
     * @param maxWidth      the width of the area in millimeters
     * @param spacing       the distance kept between the outlines in millimeters
     * @param allowRotation whether the entities may be rotated to fit better
     */
    public NestAction(List<? extends Entity> entities, double maxWidth, double spacing, boolean allowRotation) {
        this.layout = NestingUtils.layout(entities, bottomLeft(entities), maxWidth, spacing, allowRotation);
        layout.placements().forEach((entity, placement) -> {
            if (placement.rotation() != 0 || !placement.position().equals(entity.getPosition())) {
                add(new PlaceAction(entity, placement));
            }
        });
    }

    private static Point2D bottomLeft(List<? extends Entity> entities) {
        double x = entities.stream().mapToDouble(entity -> entity.getBounds().getMinX()).min().orElse(0);
        double y = entities.stream().mapToDouble(entity -> entity.getBounds().getMinY()).min().orElse(0);
        return new Point2D.Double(x, y);
    }

    /**
     * Whether the packed entities fit within an area of the given size.
     */
    public boolean fitsWithin(double maxWidth, double maxHeight) {
        return layout.fitsWithin(maxWidth, maxHeight);
    }

    @Override
    public String toString() {
        return "nest entities";
    }

    /**
     * Rotates an entity around its center and then moves it so its bounding box ends up at the
     * placed position. The move is measured after the rotation, as rotating changes the bounding
     * box, and it is undone in the reverse order so the rotation is reverted around the same
     * center it was applied to.
     */
    private static final class PlaceAction implements UndoableAction {
        private final Entity entity;
        private final NestingUtils.Placement placement;
        private Point2D center;
        private Point2D delta;

        private PlaceAction(Entity entity, NestingUtils.Placement placement) {
            this.entity = entity;
            this.placement = placement;
        }

        @Override
        public void redo() {
            center = entity.getCenter();
            if (placement.rotation() != 0) {
                entity.rotate(center, placement.rotation());
            }
            Point2D current = entity.getPosition();
            delta = new Point2D.Double(placement.position().getX() - current.getX(), placement.position().getY() - current.getY());
            entity.move(delta);
        }

        @Override
        public void undo() {
            entity.move(new Point2D.Double(-delta.getX(), -delta.getY()));
            if (placement.rotation() != 0) {
                entity.rotate(center, -placement.rotation());
            }
        }
    }
}
