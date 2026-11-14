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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class AxisFrameTest {

    @Test
    public void of_shouldUseTheAxesOfEntitiesRotatedAlike() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(30, 0, 10, 10);
        first.rotate(new Point2D.Double(0, 0), 30);
        second.rotate(new Point2D.Double(0, 0), 30);

        AxisFrame frame = AxisFrame.of(Axis.HORIZONTAL, first, second);

        assertThat(Math.toDegrees(frame.angle())).isCloseTo(-30, within(1e-6));
    }

    @Test
    public void of_shouldUseTheAxesOfTheDesignForEntitiesRotatedDifferently() {
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(30, 0, 10, 10);
        first.rotate(30);

        AxisFrame frame = AxisFrame.of(Axis.VERTICAL, first, second);

        assertThat(frame).isEqualTo(AxisFrame.of(Axis.VERTICAL));
    }

    @Test
    public void of_shouldKeepTheVerticalAxisOfAMirroredEntity() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        rectangle.applyTransform(AffineTransform.getScaleInstance(-1, 1));

        AxisFrame frame = AxisFrame.of(Axis.VERTICAL, rectangle);

        assertThat(frame.along().getX()).isCloseTo(0, within(1e-9));
        assertThat(frame.along().getY()).isCloseTo(1, within(1e-9));
    }

    @Test
    public void carry_shouldMirrorTheFrameAsTheEntityIsMirrored() {
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        AxisFrame before = AxisFrame.ownOf(rectangle);
        rectangle.applyTransform(AffineTransform.getScaleInstance(-1, 1));

        AxisFrame result = before.carry(AxisFrame.of(Axis.HORIZONTAL), AxisFrame.ownOf(rectangle));

        assertThat(result.isMirrored()).isTrue();
        assertThat(result.along().getX()).isCloseTo(-1, within(1e-9));
        assertThat(result.across().getY()).isCloseTo(1, within(1e-9));
    }
}
