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

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class EqualSizeConstraintTest {

    @Test
    public void sizeOf_shouldMeasureARotatedEntityAlongItsOwnAxes() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);
        rectangle.rotate(30);

        double width = EqualSizeConstraint.sizeOf(rectangle, Axis.HORIZONTAL);
        double height = EqualSizeConstraint.sizeOf(rectangle, Axis.VERTICAL);

        assertThat(width).isCloseTo(20, within(1e-6));
        assertThat(height).isCloseTo(10, within(1e-6));
    }

    @Test
    public void apply_shouldStretchTheTargetOfAnEqualSizeAboutItsCenter() {
        Rectangle reference = new Rectangle(0, 0, 20, 10);
        Rectangle target = new Rectangle(30, 0, 10, 10);

        Constraint.equalSize(reference, Axis.HORIZONTAL, target, Axis.VERTICAL).apply();

        assertThat(target.getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(target.getSize().getHeight()).isCloseTo(20, within(1e-6));
        assertThat(target.getCenter()).isEqualTo(new Point2D.Double(35, 5));
    }
}
