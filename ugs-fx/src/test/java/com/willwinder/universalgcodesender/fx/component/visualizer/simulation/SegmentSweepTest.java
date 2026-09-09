package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SegmentSweepTest {
    private static final double CELL = 0.25;
    private static final double TOLERANCE = 1e-5;

    private static HeightField field() {
        return new HeightField(new Bounds3(0, 0, -5, 20, 10, 0), CELL);
    }

    private static double heightAt(HeightField field, double x, double y) {
        return field.heightAt(field.columnFloor(x), field.rowFloor(y));
    }

    @Test
    public void horizontalCutShouldCarveASlotOfToolWidth() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, -1, 15, 5, -1);

        assertThat(heightAt(field, 10, 5)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 10, 5.75)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 10, 6.25)).isZero();
        assertThat(heightAt(field, 4.5, 5)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 15.5, 5)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 3.5, 5)).isZero();
        assertThat(heightAt(field, 16.5, 5)).isZero();
    }

    @Test
    public void rampShouldCutToTheDepthOfTheLastPositionCoveringEachNode() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, 0, 15, 5, -2);

        // The tool covers x=6 while its center is between x=5 and x=7, so until s=0.2 and z=-0.4
        assertThat(heightAt(field, 6, 5)).isCloseTo(-0.4, within(TOLERANCE));
        // Everything within a radius of the end position is cut to the full depth
        assertThat(heightAt(field, 14, 5)).isCloseTo(-2, within(TOLERANCE));
        assertThat(heightAt(field, 15.75, 5)).isCloseTo(-2, within(TOLERANCE));
    }

    @Test
    public void vBitShouldLeaveAVShapedGroove() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.vBit(6, 90), 5, 5, -2, 15, 5, -2);

        assertThat(heightAt(field, 10, 5)).isCloseTo(-2, within(TOLERANCE));
        assertThat(heightAt(field, 10, 6)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 10, 4)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 10, 7)).isZero();
        assertThat(heightAt(field, 10, 7.5)).isZero();
    }

    @Test
    public void ballShouldLeaveARoundGroove() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.ball(4), 5, 5, -1, 15, 5, -1);

        assertThat(heightAt(field, 10, 5)).isCloseTo(-1, within(TOLERANCE));
        assertThat(heightAt(field, 10, 6)).isCloseTo(-1 + (2 - Math.sqrt(3)), within(TOLERANCE));
        assertThat(heightAt(field, 10, 7)).isZero();
    }

    @Test
    public void plungeShouldCutACircle() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, 0, 5, 5, -3);

        assertThat(heightAt(field, 5, 5)).isCloseTo(-3, within(TOLERANCE));
        assertThat(heightAt(field, 5.75, 5)).isCloseTo(-3, within(TOLERANCE));
        assertThat(heightAt(field, 6.25, 5)).isZero();
    }

    @Test
    public void cutsShouldStopAtTheStockBottom() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, -8, 15, 5, -8);
        assertThat(heightAt(field, 10, 5)).isCloseTo(-5, within(TOLERANCE));
    }

    @Test
    public void cutsOutsideTheStockShouldBeIgnored() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 30, 30, -1, 40, 30, -1);
        SegmentSweep.sweep(field, ToolProfile.flat(2), -10, 5, -1, -5, 5, -1);
        assertThat(field.isUntouched()).isTrue();
    }

    @Test
    public void cutsShouldNeverRaiseTheSurface() {
        HeightField field = field();
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, -2, 15, 5, -2);
        SegmentSweep.sweep(field, ToolProfile.flat(2), 5, 5, -1, 15, 5, -1);
        assertThat(heightAt(field, 10, 5)).isCloseTo(-2, within(TOLERANCE));
    }
}
