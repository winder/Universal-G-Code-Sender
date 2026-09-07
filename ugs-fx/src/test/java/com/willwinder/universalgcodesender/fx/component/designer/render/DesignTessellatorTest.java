package com.willwinder.universalgcodesender.fx.component.designer.render;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneMeshes;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class DesignTessellatorTest {

    @Test
    public void outline_shouldProduceOneSegmentPerEdgeOfAClosedShape() {
        float[] vertices = DesignTessellator.outline(new Rectangle2D.Double(0, 0, 10, 5), Color.BLACK);

        assertThat(vertices.length / (2 * VertexLayout.LINE.floatsPerVertex())).isEqualTo(4);
    }

    @Test
    public void outline_shouldKeepOpenPathsOpen() {
        float[] vertices = DesignTessellator.outline(new Line2D.Double(0, 0, 10, 0), Color.BLACK);

        assertThat(vertices.length / (2 * VertexLayout.LINE.floatsPerVertex())).isEqualTo(1);
    }

    @Test
    public void fill_shouldCoverTheAreaOfARectangle() {
        float[] vertices = DesignTessellator.fill(new Rectangle2D.Double(0, 0, 10, 5));

        assertThat(vertices.length / (3 * SceneMeshes.FLOATS_PER_VERTEX)).isEqualTo(2);
        assertThat(triangleArea(vertices)).isCloseTo(50, within(1e-6));
    }

    @Test
    public void fill_shouldLeaveHolesEmpty() {
        Path2D.Double ring = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        ring.append(new Rectangle2D.Double(0, 0, 10, 10), false);
        ring.append(new Rectangle2D.Double(2, 2, 4, 4), false);

        float[] vertices = DesignTessellator.fill(ring);

        assertThat(triangleArea(vertices)).isCloseTo(100 - 16, within(1e-6));
    }

    @Test
    public void fill_shouldApproximateCurvedShapes() {
        float[] vertices = DesignTessellator.fill(new Ellipse2D.Double(0, 0, 20, 20));

        // Flattening to chords leaves the polygon slightly inside the circle.
        assertThat(triangleArea(vertices)).isCloseTo(Math.PI * 100, within(Math.PI * 100 * 0.01));
    }

    @Test
    public void fill_shouldTreatAPathReturningToItsStartAsClosed() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 5);
        path.lineTo(0, 5);
        path.lineTo(0, 0);

        float[] vertices = DesignTessellator.fill(path);

        assertThat(triangleArea(vertices)).isCloseTo(50, within(1e-6));
    }

    @Test
    public void fill_shouldBeEmptyForOpenPaths() {
        float[] vertices = DesignTessellator.fill(new Line2D.Double(0, 0, 10, 0));

        assertThat(vertices).isEmpty();
    }

    @Test
    public void dashedOutline_shouldCoverHalfThePerimeterAndContinueAroundCorners() {
        float[] vertices = DesignTessellator.dashedOutline(new Rectangle2D.Double(0, 0, 10, 5), Color.BLACK, 1.5);

        assertThat(totalLength(vertices)).isCloseTo(15, within(1e-6));
        int floats = VertexLayout.LINE.floatsPerVertex();
        boolean hasDashAroundCorner = false;
        for (int i = 0; i + 2 * floats <= vertices.length; i += 2 * floats) {
            boolean startsBeforeCorner = vertices[i] < 10 - 1e-6 && vertices[i + 1] == 0;
            boolean endsOnRightEdge = vertices[i + floats] == 10;
            hasDashAroundCorner |= startsBeforeCorner && endsOnRightEdge;
        }
        assertThat(hasDashAroundCorner).as("the pattern carries its phase over the corner").isTrue();
    }

    private static double triangleArea(float[] vertices) {
        double area = 0;
        int stride = SceneMeshes.FLOATS_PER_VERTEX;
        for (int i = 0; i + 3 * stride <= vertices.length; i += 3 * stride) {
            double ax = vertices[i];
            double ay = vertices[i + 1];
            double bx = vertices[i + stride];
            double by = vertices[i + stride + 1];
            double cx = vertices[i + 2 * stride];
            double cy = vertices[i + 2 * stride + 1];
            area += Math.abs((bx - ax) * (cy - ay) - (cx - ax) * (by - ay)) / 2;
        }
        return area;
    }

    private static double totalLength(float[] vertices) {
        double length = 0;
        int floats = VertexLayout.LINE.floatsPerVertex();
        for (int i = 0; i + 2 * floats <= vertices.length; i += 2 * floats) {
            length += Math.hypot(vertices[i + floats] - vertices[i], vertices[i + floats + 1] - vertices[i + 1]);
        }
        return length;
    }

    @Test
    public void texturedQuad_shouldCoverTheBoundsWithTheImageTopAtMaxY() {
        float[] vertices = DesignTessellator.texturedQuad(new Rectangle2D.Double(10, 20, 30, 40));

        assertThat(vertices.length / VertexLayout.TEXTURED.floatsPerVertex()).isEqualTo(6);
        // Bottom left corner samples the bottom row of the image, top right the top row.
        assertThat(vertices).startsWith(10, 20, 0, 0, 1);
        assertThat(vertices).containsSequence(40f, 60f, 0f, 1f, 0f);
    }
}
