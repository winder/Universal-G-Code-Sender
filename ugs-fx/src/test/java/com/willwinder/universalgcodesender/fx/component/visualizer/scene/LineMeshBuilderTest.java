package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import javafx.scene.paint.Color;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineMeshBuilderTest {

    @Test
    public void add_shouldWriteTwoVerticesWithColorAndCommand() {
        LineMeshBuilder builder = new LineMeshBuilder(1);

        float[] vertices = builder.add(1, 2, 3, 4, 5, 6, Color.color(0.5, 0.25, 1.0), 42).build();

        assertThat(builder.segmentCount()).isEqualTo(1);
        assertThat(builder.vertexCount()).isEqualTo(2);
        assertThat(vertices).hasSize(2 * VertexLayout.LINE.floatsPerVertex());
        assertThat(vertices).containsExactly(
                1, 2, 3, 0.5f, 0.25f, 1.0f, 1.0f, 42,
                4, 5, 6, 0.5f, 0.25f, 1.0f, 1.0f, 42);
    }

    @Test
    public void add_shouldTagLinesWithoutCommandAsNoCommand() {
        float[] vertices = new LineMeshBuilder().add(0, 0, 0, 1, 0, 0, Color.RED).build();

        assertThat(vertices[7]).isEqualTo(LineMeshBuilder.NO_COMMAND);
        assertThat(vertices[15]).isEqualTo(LineMeshBuilder.NO_COMMAND);
    }

    @Test
    public void add_shouldGrowBeyondTheExpectedSize() {
        LineMeshBuilder builder = new LineMeshBuilder(1);

        for (int i = 0; i < 100; i++) {
            builder.add(i, 0, 0, i + 1, 0, 0, Color.WHITE);
        }

        assertThat(builder.segmentCount()).isEqualTo(100);
        assertThat(builder.build()).hasSize(100 * 2 * VertexLayout.LINE.floatsPerVertex());
    }

    @Test
    public void grid_shouldSnapOutwardsToWholeSteps() {
        LineMeshBuilder grid = LineMeshBuilder.grid(-5, 0, 25, 10, 10, 0, Color.GRAY);

        // Columns at -10, 0, 10, 20, 30 and rows at 0, 10.
        assertThat(grid.segmentCount()).isEqualTo(5 + 2);
        float[] vertices = grid.build();
        assertThat(vertices[0]).isEqualTo(-10);
        assertThat(vertices[VertexLayout.LINE.floatsPerVertex() + 1]).isEqualTo(10);
    }
}
