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

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DistanceConstraintTest {

    @Test
    public void measured_shouldHoldTheCurrentSignedDistance() {
        Rectangle reference = new Rectangle(20, 0, 10, 10);
        Rectangle target = new Rectangle(0, 0, 5, 5);

        DistanceConstraint constraint = DistanceConstraint.measured(reference, at(reference, Anchor.BOTTOM_LEFT), target, at(target, Anchor.BOTTOM_RIGHT), Axis.HORIZONTAL, 0);

        assertThat(constraint.getDistance()).isCloseTo(-15, within(1e-6));
    }

    @Test
    public void apply_shouldMoveTargetAlongTheAxisOnly() {
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(50, 50, 10, 10);
        DistanceConstraint constraint = new DistanceConstraint(reference, at(reference, Anchor.TOP_LEFT), target, at(target, Anchor.BOTTOM_LEFT), Axis.VERTICAL, 5, 0);

        constraint.apply();

        assertThat(target.getPosition().getY()).isCloseTo(15, within(1e-6));
        assertThat(target.getPosition().getX()).isCloseTo(50, within(1e-6));
    }

    @Test
    public void apply_shouldNotScaleASizeToZero() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        DistanceConstraint constraint = new DistanceConstraint(rectangle, at(rectangle, Anchor.BOTTOM_LEFT), rectangle,
                at(rectangle, Anchor.BOTTOM_RIGHT), Axis.HORIZONTAL, 0, 0);

        constraint.apply();

        assertThat(rectangle.getSize().getWidth()).isCloseTo(10, within(1e-6));
    }

    @Test
    public void apply_shouldLineUpTheTargetWithTheReferenceForAnAlignment() {
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(3, 30, 10, 10);
        AlignmentConstraint constraint = Constraint.aligned(reference, at(reference, Anchor.BOTTOM_LEFT), target,
                at(target, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL);

        constraint.apply();

        assertThat(target.getPosition().getX()).isCloseTo(0, within(1e-6));
        assertThat(target.getPosition().getY()).isCloseTo(30, within(1e-6));
    }

    @Test
    public void apply_shouldMoveTheTargetPointOntoTheReferencePointForACoincidence() {
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(30, 20, 4, 4);
        CoincidentConstraint constraint = Constraint.coincident(reference, at(reference, Anchor.CENTER), target, at(target, Anchor.CENTER));

        constraint.apply();

        assertThat(target.getCenter()).isEqualTo(new Point2D.Double(5, 5));
    }

    @Test
    public void distanceFor_shouldKeepTheTargetOnItsSide() {
        Rectangle reference = new Rectangle(20, 0, 10, 10);
        Rectangle target = new Rectangle(0, 0, 5, 5);
        DistanceConstraint constraint = new DistanceConstraint(reference, at(reference, Anchor.BOTTOM_LEFT), target,
                at(target, Anchor.BOTTOM_RIGHT), Axis.HORIZONTAL, -15, 0);

        OptionalDouble result = constraint.distanceFor(8);

        assertThat(result).hasValue(-8);
    }

    @Test
    public void distanceFor_shouldRefuseASizeOfZero() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        DistanceConstraint constraint = new DistanceConstraint(rectangle, at(rectangle, Anchor.BOTTOM_LEFT), rectangle,
                at(rectangle, Anchor.BOTTOM_RIGHT), Axis.HORIZONTAL, 10, 0);

        OptionalDouble result = constraint.distanceFor(0);

        assertThat(result).isEmpty();
    }

    @Test
    public void distanceFor_shouldIgnoreTheSignOfASize() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        DistanceConstraint constraint = new DistanceConstraint(rectangle, at(rectangle, Anchor.BOTTOM_LEFT), rectangle,
                at(rectangle, Anchor.BOTTOM_RIGHT), Axis.HORIZONTAL, 10, 0);

        OptionalDouble result = constraint.distanceFor(-4);

        assertThat(result).hasValue(4);
    }

    private static ShapePoint at(Entity entity, Anchor anchor) {
        return ShapeSnapping.pointAt(entity, entity.getPosition(anchor));
    }
}
