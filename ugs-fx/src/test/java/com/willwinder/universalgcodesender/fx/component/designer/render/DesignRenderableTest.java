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
package com.willwinder.universalgcodesender.fx.component.designer.render;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import org.junit.Test;

import java.awt.geom.Rectangle2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DesignRenderableTest {
    private static final int FLOATS_PER_VERTEX = VertexLayout.LINE.floatsPerVertex();

    @Test
    public void surfacingArrows_shouldPointTowardsNegativeYForTopToBottomPasses() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle rectangle = surfaceRectangle(ToolPathDirection.TOP_BOTTOM, Direction.CLIMB);

        float[] vertices = DesignRenderable.surfacingArrows(bounds, rectangle);

        assertThat(shaftTipY(vertices)).isLessThan(shaftTailY(vertices));
        assertThat(shaftTipX(vertices)).isCloseTo(shaftTailX(vertices), within(1e-6f));
    }

    @Test
    public void surfacingArrows_shouldPointTowardsPositiveXForLeftToRightPasses() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle rectangle = surfaceRectangle(ToolPathDirection.LEFT_RIGHT, Direction.CLIMB);

        float[] vertices = DesignRenderable.surfacingArrows(bounds, rectangle);

        assertThat(shaftTipX(vertices)).isGreaterThan(shaftTailX(vertices));
        assertThat(shaftTipY(vertices)).isCloseTo(shaftTailY(vertices), within(1e-6f));
    }

    @Test
    public void surfacingArrows_shouldPointTowardsNegativeXForRightToLeftPasses() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle rectangle = surfaceRectangle(ToolPathDirection.RIGHT_LEFT, Direction.CLIMB);

        float[] vertices = DesignRenderable.surfacingArrows(bounds, rectangle);

        assertThat(shaftTipX(vertices)).isLessThan(shaftTailX(vertices));
        assertThat(shaftTipY(vertices)).isCloseTo(shaftTailY(vertices), within(1e-6f));
    }

    @Test
    public void surfacingArrows_shouldPointTowardsPositiveYForBottomToTopPasses() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle rectangle = surfaceRectangle(ToolPathDirection.BOTTOM_TOP, Direction.CLIMB);

        float[] vertices = DesignRenderable.surfacingArrows(bounds, rectangle);

        assertThat(shaftTipY(vertices)).isGreaterThan(shaftTailY(vertices));
        assertThat(shaftTipX(vertices)).isCloseTo(shaftTailX(vertices), within(1e-6f));
    }

    @Test
    public void surfacingArrows_shouldDrawTwoArrowsWhenCuttingInBothDirections() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle rectangle = surfaceRectangle(ToolPathDirection.LEFT_RIGHT, Direction.BOTH);

        float[] vertices = DesignRenderable.surfacingArrows(bounds, rectangle);

        assertThat(vertices.length / (2 * FLOATS_PER_VERTEX)).isEqualTo(6);
    }

    private static Rectangle surfaceRectangle(ToolPathDirection toolPathDirection, Direction direction) {
        Rectangle rectangle = new Rectangle(0, 0, 10, 20);
        rectangle.setCutType(CutType.SURFACE);
        rectangle.setToolPathDirection(toolPathDirection);
        rectangle.setDirection(direction);
        return rectangle;
    }

    private static float shaftTailX(float[] vertices) {
        return vertices[0];
    }

    private static float shaftTailY(float[] vertices) {
        return vertices[1];
    }

    private static float shaftTipX(float[] vertices) {
        return vertices[FLOATS_PER_VERTEX];
    }

    private static float shaftTipY(float[] vertices) {
        return vertices[FLOATS_PER_VERTEX + 1];
    }
}
