package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class StockSimulationTest {

    @Test
    public void fromLineSegmentsShouldDropRapidsAndKeepToolNumbers() {
        // The parser flags nearly every segment as rotational, so rotation must not drop segments
        LineSegment rapid = segment(0, 0, 5, 10, 0, 5, 1);
        rapid.setIsFastTraverse(true);
        LineSegment cut = segment(10, 0, 5, 10, 0, -1, 2);
        cut.setToolNumber(3);
        LineSegment rotation = segment(10, 0, -1, 20, 0, -1, 3);
        rotation.setIsRotation(true);

        List<CutSegment> cuts = CutSegment.fromLineSegments(List.of(rapid, cut, rotation));
        assertThat(cuts).hasSize(2);
        assertThat(cuts.get(1).commandNumber()).isEqualTo(3);
        assertThat(cuts.get(0).toolNumber()).isEqualTo(3);
        assertThat(cuts.get(0).commandNumber()).isEqualTo(2);
        assertThat(cuts.get(0).z1()).isEqualTo(-1);
    }

    @Test
    public void fromLineSegmentsShouldConvertInchesToMillimeters() {
        LineSegment cut = new LineSegment(
                new Position(0, 0, 0, UnitUtils.Units.INCH),
                new Position(1, 0, -0.1, UnitUtils.Units.INCH), 1);
        List<CutSegment> cuts = CutSegment.fromLineSegments(List.of(cut));
        assertThat(cuts.get(0).x1()).isCloseTo(25.4, within(1e-9));
        assertThat(cuts.get(0).z1()).isCloseTo(-2.54, within(1e-9));
    }

    @Test
    public void advanceToShouldApplySegmentsUpToTheCommandNumber() {
        HeightField field = new HeightField(new Bounds3(0, 0, -5, 30, 10, 0), 0.5);
        List<CutSegment> cuts = List.of(
                new CutSegment(5, 5, -1, 10, 5, -1, 0, 1),
                new CutSegment(10, 5, -1, 15, 5, -1, 0, 2),
                new CutSegment(15, 5, -1, 20, 5, -1, 0, 3));
        StockSimulation simulation = new StockSimulation(field, cuts, ToolResolver.fixed(ToolProfile.flat(2)));

        assertThat(simulation.advanceTo(0)).isFalse();
        assertThat(simulation.advanceTo(2)).isTrue();
        assertThat(simulation.applied()).isEqualTo(2);
        assertThat(simulation.isComplete()).isFalse();
        assertThat(field.heightAt(field.columnFloor(12), field.rowFloor(5))).isCloseTo(-1, within(1e-5f));
        assertThat(field.heightAt(field.columnFloor(18), field.rowFloor(5))).isZero();

        assertThat(simulation.advanceTo(2)).isFalse();
        simulation.runAll();
        assertThat(simulation.isComplete()).isTrue();
        assertThat(field.heightAt(field.columnFloor(18), field.rowFloor(5))).isCloseTo(-1, within(1e-5f));
    }

    @Test
    public void toolResolverShouldBeUsedPerToolNumber() {
        HeightField field = new HeightField(new Bounds3(0, 0, -5, 30, 20, 0), 0.25);
        List<CutSegment> cuts = List.of(
                new CutSegment(5, 5, -1, 25, 5, -1, 1, 1),
                new CutSegment(5, 15, -1, 25, 15, -1, 2, 2));
        StockSimulation simulation = new StockSimulation(field, cuts,
                cut -> ResolvedTool.of(cut.toolNumber() == 1 ? ToolProfile.flat(2) : ToolProfile.flat(6), ResolvedTool.Source.FIXED, "test"));
        simulation.runAll();

        assertThat(field.heightAt(field.columnFloor(15), field.rowFloor(7))).isZero();
        assertThat(field.heightAt(field.columnFloor(15), field.rowFloor(17))).isCloseTo(-1, within(1e-5f));
    }

    private static LineSegment segment(double x0, double y0, double z0, double x1, double y1, double z1, int command) {
        return new LineSegment(new Position(x0, y0, z0, UnitUtils.Units.MM), new Position(x1, y1, z1, UnitUtils.Units.MM), command);
    }
}
