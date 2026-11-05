package com.willwinder.universalgcodesender.fx.component.designer.editor;

import org.junit.Test;

import java.awt.geom.Point2D;

import static org.assertj.core.api.Assertions.assertThat;

public class SnapperTest {

    @Test
    public void snap_shouldRoundToTheGrid() {
        Snapper snapper = new Snapper(() -> 5);

        assertThat(snapper.snap(12.4)).isEqualTo(10);
        assertThat(snapper.snap(12.6)).isEqualTo(15);
        assertThat(snapper.snap(new Point2D.Double(1, 8))).isEqualTo(new Point2D.Double(0, 10));
    }

    @Test
    public void snap_shouldLeaveValuesAloneWithoutAGrid() {
        Snapper snapper = new Snapper(() -> 0);

        assertThat(snapper.snap(12.34)).isEqualTo(12.34);
    }

    @Test
    public void snapRounded_shouldRoundToWholeOrTenthMillimetersWithoutAGrid() {
        Snapper snapper = new Snapper(() -> 0);

        assertThat(snapper.snapRounded(12.34, false)).isEqualTo(12);
        assertThat(snapper.snapRounded(12.34, true)).isEqualTo(12.3);
    }

    @Test
    public void snapRounded_shouldSnapToGridLinesFinerThanAMillimeter() {
        Snapper snapper = new Snapper(() -> 0.5);

        assertThat(snapper.snapRounded(12.6, false)).isEqualTo(12.5);
        assertThat(snapper.snapRounded(12.6, true)).isEqualTo(12.5);
    }

    @Test
    public void nudge_shouldMoveAnOffGridValueOntoTheNextGridLine() {
        Snapper snapper = new Snapper(() -> 0.5);

        assertThat(snapper.nudge(0.1, 1, 1)).isEqualTo(0.5);
        assertThat(snapper.nudge(0.1, -1, 1)).isEqualTo(0);
        assertThat(snapper.nudge(-0.1, -1, 1)).isEqualTo(-0.5);
    }

    @Test
    public void nudge_shouldMoveAnOnGridValueByWholeGridSteps() {
        Snapper snapper = new Snapper(() -> 0.5);

        assertThat(snapper.nudge(1.5, 1, 1)).isEqualTo(2);
        assertThat(snapper.nudge(1.5, -1, 1)).isEqualTo(1);
        assertThat(snapper.nudge(1.5, 1, 10)).isEqualTo(6.5);
        assertThat(snapper.nudge(0.1, 1, 10)).isEqualTo(5);
    }

    @Test
    public void nudge_shouldMoveAMillimeterPerStepWithoutAGrid() {
        Snapper snapper = new Snapper(() -> 0);

        assertThat(snapper.nudge(0.1, 1, 1)).isEqualTo(1.1);
        assertThat(snapper.nudge(0.1, -1, 10)).isCloseTo(-9.9, org.assertj.core.api.Assertions.within(1e-9));
    }
}
