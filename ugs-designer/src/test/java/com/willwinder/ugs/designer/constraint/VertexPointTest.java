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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class VertexPointTest {

    @Test
    public void locate_shouldFollowTheShapeWhenRotated() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);
        ShapePoint corner = ShapeSnapping.pointAt(rectangle, new Point2D.Double(20, 0));

        rectangle.rotate(new Point2D.Double(0, 0), -90);
        Optional<Point2D> result = corner.locate(rectangle);

        assertThat(result).isPresent();
        assertThat(result.get().getX()).isCloseTo(0, within(1e-9));
        assertThat(result.get().getY()).isCloseTo(20, within(1e-9));
    }

    @Test
    public void locate_shouldBeEmptyWhenTheShapeHasNoSuchSegment() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        Optional<Point2D> result = new VertexPoint(999, false).locate(rectangle);

        assertThat(result).isEmpty();
    }

    @Test
    public void locate_shouldFindTheEndOfASegment() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        Optional<Point2D> result = new VertexPoint(0, true).locate(rectangle);

        assertThat(result).isPresent().isEqualTo(new VertexPoint(1, false).locate(rectangle));
    }
}
