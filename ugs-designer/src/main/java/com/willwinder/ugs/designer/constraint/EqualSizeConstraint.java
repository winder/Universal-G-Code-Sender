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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Entity;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Keeps one size of the target, its width or its height, the same as one size of the
 * reference, so the width of one entity can follow the height of another, or even of the same
 * entity. The sizes are measured along each entity's own axes, and the target is stretched
 * about its center to keep them equal. Like a size it holds no position, so it does not count
 * against the constraints moving the entities.
 */
public final class EqualSizeConstraint extends Constraint {
    private Axis referenceAxis;
    private Axis targetAxis;

    EqualSizeConstraint(Entity reference, Axis referenceAxis, Entity target, Axis targetAxis) {
        super(reference, target);
        this.referenceAxis = referenceAxis;
        this.targetAxis = targetAxis;
    }

    @Override
    public Kind getKind() {
        return Kind.EQUAL_SIZE;
    }

    /**
     * The axis of the reference's size.
     */
    public Axis getReferenceAxis() {
        return referenceAxis;
    }

    /**
     * The axis of the target's size.
     */
    public Axis getTargetAxis() {
        return targetAxis;
    }

    @Override
    public List<Axis> getAxes() {
        return List.of();
    }

    /**
     * The size of the entity along its own axis, which follows the entity's rotation.
     */
    public static double sizeOf(Entity entity, Axis sizeAxis) {
        Rectangle2D bounds = AxisFrame.ownOf(entity).boundsOf(entity);
        return sizeAxis == Axis.HORIZONTAL ? bounds.getWidth() : bounds.getHeight();
    }

    @Override
    public void apply() {
        applyResizing(true);
    }

    /**
     * Stretches the target or the reference along the axis of its size, about its center, to
     * the size the other one has: the target as {@link #apply()} does, or the reference, as when
     * the target was resized.
     */
    public void applyResizing(boolean resizeTarget) {
        Entity entity = resizeTarget ? target : reference;
        Axis resizedAxis = resizeTarget ? targetAxis : referenceAxis;
        double wanted = resizeTarget ? sizeOf(reference, referenceAxis) : sizeOf(target, targetAxis);
        stretch(entity, AxisFrame.of(resizedAxis, entity), entity.getCenter(), sizeOf(entity, resizedAxis), wanted);
    }

    @Override
    void flip() {
        super.flip();
        Axis flippedTargetAxis = referenceAxis;
        referenceAxis = targetAxis;
        targetAxis = flippedTargetAxis;
    }
}
