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

import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.model.Size;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class ShapeSnappingTest {

    @Test
    public void snap_shouldOnlySnapToTheCenterOfAPoint() {
        Point point = new Point(10, 10);

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(point, new Point2D.Double(11, 10), 0.5);

        assertThat(result).isEmpty();
        assertThat(ShapeSnapping.snap(point, new Point2D.Double(10.8, 10), 1)).hasValueSatisfying(snap -> {
            assertThat(snap.kind()).isEqualTo(ShapeSnapping.Kind.CENTER);
            assertThat(snap.position()).isEqualTo(new Point2D.Double(10, 10));
        });
    }

    @Test
    public void snap_shouldSnapToTheNearestVertex() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(rectangle, new Point2D.Double(19, 0.5), 2);

        assertThat(result).hasValueSatisfying(snap -> {
            assertThat(snap.position()).isEqualTo(new Point2D.Double(20, 0));
            assertThat(snap.point()).isInstanceOf(VertexPoint.class);
        });
    }

    @Test
    public void snap_shouldNotSnapToAPointAlongAnEdge() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(rectangle, new Point2D.Double(10, 10.5), 2);

        assertThat(result).isEmpty();
    }

    @Test
    public void snap_shouldSnapToTheCenter() {
        Ellipse circle = new Ellipse(0, 0);
        circle.setSize(new Size(20, 20));

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(circle, new Point2D.Double(10.5, 9.5), 1);

        assertThat(result).isPresent();
        assertThat(result.get().point()).isEqualTo(CenterPoint.INSTANCE);
    }

    @Test
    public void snap_shouldSnapToTheVerticesOfACurve() {
        Ellipse circle = new Ellipse(0, 0);
        circle.setSize(new Size(20, 20));

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(circle, new Point2D.Double(20.3, 10.2), 1);

        assertThat(result).hasValueSatisfying(snap -> {
            assertThat(snap.position().getX()).isCloseTo(20, within(1e-9));
            assertThat(snap.position().getY()).isCloseTo(10, within(1e-9));
        });
    }

    @Test
    public void snap_shouldFindNothingAwayFromTheShape() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(rectangle, new Point2D.Double(5, 5), 1);

        assertThat(result).isEmpty();
    }

    @Test
    public void snap_shouldSnapToTheEndOfAnOpenPath() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);

        Optional<ShapeSnapping.Snap> result = ShapeSnapping.snap(path, new Point2D.Double(10.2, 9.8), 1);

        assertThat(result).isPresent();
        assertThat(result.get().position()).isEqualTo(new Point2D.Double(10, 10));
    }

    @Test
    public void pointAt_shouldFindTheVertexAtThePosition() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        ShapePoint result = ShapeSnapping.pointAt(rectangle, new Point2D.Double(20, 10));

        assertThat(result.locate(rectangle)).contains(new Point2D.Double(20, 10));
    }

    @Test
    public void pointAt_shouldRefuseAPointAlongAnEdge() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 10);

        assertThatThrownBy(() -> ShapeSnapping.pointAt(rectangle, new Point2D.Double(10, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
