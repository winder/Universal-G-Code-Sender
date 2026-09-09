package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GcodeLines;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs a real program from the repository through the whole pipeline the renderable uses, apart
 * from the upload to the GPU.
 */
public class StockPipelineTest {

    @Test
    public void shouldSimulateARealProgramEndToEnd() throws Exception {
        File file = new File("../test_files/spiral.gcode");
        assertThat(file).exists();

        List<LineSegment> segments = GcodeLines.parseSegments(file);
        List<CutSegment> cuts = CutSegment.fromLineSegments(segments);
        assertThat(cuts).isNotEmpty();

        ToolResolver tools = ToolResolver.fixed(ToolProfile.flat(3.175));
        StockDefinition stock = StockDefinition.fromCuts(cuts, tools).orElseThrow();
        assertThat(stock.bounds().width()).isGreaterThan(0);
        assertThat(stock.bounds().maxZ()).isGreaterThan(stock.bounds().minZ());

        HeightField field = stock.createHeightField();
        assertThat(field.nodeCount()).isLessThanOrEqualTo((int) (StockDefinition.MAX_NODES * 1.05));

        long start = System.nanoTime();
        new StockSimulation(field, cuts, tools).runAll();
        HeightFieldMesher.Mesh mesh = HeightFieldMesher.mesh(field);
        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("spiral.gcode: " + segments.size() + " segments, " + cuts.size() + " cuts, "
                + field.columns() + "x" + field.rows() + " nodes, " + mesh.triangleCount() + " triangles in " + millis + " ms");

        assertThat(field.isUntouched()).isFalse();
        assertThat(mesh.triangleCount()).isGreaterThan(0);
        assertThat(mesh.vertexCount() % 3).isZero();
    }
}
