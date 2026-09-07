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
        assertThat(snapper.nudgeStep()).isEqualTo(1);
    }

    @Test
    public void snapRounded_shouldRoundToWholeOrTenthMillimetersBeforeSnapping() {
        Snapper snapper = new Snapper(() -> 0);

        assertThat(snapper.snapRounded(12.34, false)).isEqualTo(12);
        assertThat(snapper.snapRounded(12.34, true)).isEqualTo(12.3);
    }

    @Test
    public void nudgeStep_shouldBeTheGridSizeWhenSnapping() {
        assertThat(new Snapper(() -> 2.5).nudgeStep()).isEqualTo(2.5);
    }
}
