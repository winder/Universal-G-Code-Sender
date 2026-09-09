package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class StockDefinitionTest {
    private static final ToolResolver TOOL = ToolResolver.fixed(ToolProfile.flat(4));

    @Test
    public void shouldBeEmptyWithoutCuts() {
        assertThat(StockDefinition.fromCuts(List.of(), TOOL)).isEmpty();
    }

    @Test
    public void topShouldBeZeroWhenAllCutsAreBelowZero() {
        Optional<StockDefinition> stock = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, -1, 50, 0, -1, 0, 1),
                new CutSegment(50, 0, -1, 50, 30, -6, 0, 2)), TOOL);

        assertThat(stock).isPresent();
        double inflate = 2 + StockDefinition.MARGIN;
        assertThat(stock.get().bounds().minX()).isCloseTo(-inflate, within(1e-9));
        assertThat(stock.get().bounds().maxX()).isCloseTo(50 + inflate, within(1e-9));
        assertThat(stock.get().bounds().minY()).isCloseTo(-inflate, within(1e-9));
        assertThat(stock.get().bounds().maxY()).isCloseTo(30 + inflate, within(1e-9));
        assertThat(stock.get().bounds().maxZ()).isZero();
        assertThat(stock.get().bounds().minZ()).isEqualTo(-6);
    }

    @Test
    public void topShouldBeTheHighestCutWhenCutsArePositive() {
        StockDefinition stock = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, 8, 50, 0, 8, 0, 1),
                new CutSegment(50, 0, 8, 50, 30, 2, 0, 2)), TOOL).orElseThrow();

        assertThat(stock.bounds().maxZ()).isEqualTo(8);
        // The deepest cut is assumed to go through, so it is the bottom of the stock
        assertThat(stock.bounds().minZ()).isEqualTo(2);
    }

    @Test
    public void plungesShouldNotRaiseTheTopOfTheStock() {
        StockDefinition stock = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, 5, 0, 0, -1, 0, 1),
                new CutSegment(0, 0, -1, 50, 0, -1, 0, 2),
                new CutSegment(50, 0, -1, 50, 0, 5, 0, 3)), TOOL).orElseThrow();
        assertThat(stock.bounds().maxZ()).isZero();
        assertThat(stock.bounds().minZ()).isEqualTo(-1);

        StockDefinition positive = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, 20, 0, 0, 8, 0, 1),
                new CutSegment(0, 0, 8, 50, 0, 8, 0, 2)), TOOL).orElseThrow();
        assertThat(positive.bounds().maxZ()).isEqualTo(8);
        assertThat(positive.bounds().minZ()).isEqualTo(8 - StockDefinition.MIN_THICKNESS);
    }

    @Test
    public void drillingOnlyProgramsShouldFallBackToThePlungeHeights() {
        StockDefinition stock = StockDefinition.fromCuts(List.of(
                new CutSegment(10, 10, 0, 10, 10, -6, 0, 1),
                new CutSegment(20, 10, 0, 20, 10, -6, 0, 2)), TOOL).orElseThrow();
        assertThat(stock.bounds().maxZ()).isZero();
        assertThat(stock.bounds().minZ()).isEqualTo(-6);
    }

    @Test
    public void manualStockShouldBeUsedAsGiven() {
        Bounds3 given = new Bounds3(-10, -10, -12, 90, 60, 0);
        StockDefinition stock = StockDefinition.resolve(StockSpec.manual(given), List.of(
                new CutSegment(0, 0, -1, 50, 0, -1, 0, 1)), TOOL).orElseThrow();
        assertThat(stock.bounds()).isEqualTo(given);
        assertThat(stock.cellSize()).isCloseTo(StockDefinition.cellSizeFor(given, 2), within(1e-9));

        assertThat(StockDefinition.resolve(StockSpec.manual(new Bounds3(0, 0, 0, 0, 10, 5)), List.of(
                new CutSegment(0, 0, -1, 50, 0, -1, 0, 1)), TOOL)).isEmpty();
    }

    @Test
    public void stockShouldNeverBeThinnerThanTheMinimumThickness() {
        StockDefinition stock = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, 0, 50, 0, 0, 0, 1)), TOOL).orElseThrow();
        assertThat(stock.bounds().maxZ()).isZero();
        assertThat(stock.bounds().minZ()).isEqualTo(-StockDefinition.MIN_THICKNESS);
    }

    @Test
    public void cellSizeShouldSampleTheSmallestToolButRespectTheNodeBudget() {
        StockDefinition small = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, -1, 10, 0, -1, 0, 1)), TOOL).orElseThrow();
        assertThat(small.cellSize()).isCloseTo(2 / StockDefinition.NODES_PER_RADIUS, within(1e-9));

        StockDefinition large = StockDefinition.fromCuts(List.of(
                new CutSegment(0, 0, -1, 1000, 1000, -1, 0, 1)), TOOL).orElseThrow();
        HeightField field = large.createHeightField();
        assertThat(field.nodeCount()).isLessThanOrEqualTo((int) (StockDefinition.MAX_NODES * 1.05));
    }
}
