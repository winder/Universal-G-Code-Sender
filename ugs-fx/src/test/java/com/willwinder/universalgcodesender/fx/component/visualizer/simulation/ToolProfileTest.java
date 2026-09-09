package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ToolProfileTest {

    @Test
    public void flatToolShouldHaveNoHeight() {
        ToolProfile tool = ToolProfile.flat(6);
        assertThat(tool.radius()).isEqualTo(3);
        assertThat(tool.heightAt(0)).isZero();
        assertThat(tool.heightAt(3)).isZero();
    }

    @Test
    public void ballToolShouldFollowASphere() {
        ToolProfile tool = ToolProfile.ball(6);
        assertThat(tool.heightAt(0)).isZero();
        assertThat(tool.heightAt(3)).isCloseTo(3, within(1e-9));
        assertThat(tool.heightAt(1.5)).isCloseTo(3 - Math.sqrt(9 - 2.25), within(1e-9));
    }

    @Test
    public void vBitShouldFollowACone() {
        assertThat(ToolProfile.vBit(6, 90).heightAt(2)).isCloseTo(2, within(1e-9));
        assertThat(ToolProfile.vBit(6, 60).heightAt(1)).isCloseTo(Math.sqrt(3), within(1e-9));
        assertThat(ToolProfile.vBit(6, 180).kind()).isEqualTo(ToolProfile.Kind.FLAT);
    }

    @Test
    public void fromDefinitionShouldMapShapesAndUnits() {
        ToolDefinition inchBall = new ToolDefinition();
        inchBall.setShape(EndmillShape.BALL);
        inchBall.setDiameter(0.25);
        inchBall.setDiameterUnit(UnitUtils.Units.INCH);
        ToolProfile ball = ToolProfile.fromDefinition(inchBall);
        assertThat(ball.kind()).isEqualTo(ToolProfile.Kind.BALL);
        assertThat(ball.diameter()).isCloseTo(6.35, within(1e-9));

        ToolDefinition vBit = new ToolDefinition();
        vBit.setShape(EndmillShape.V_BIT);
        vBit.setDiameter(10);
        vBit.setVBitAngleDegrees(60.0);
        assertThat(ToolProfile.fromDefinition(vBit).heightAt(1)).isCloseTo(Math.sqrt(3), within(1e-9));

        ToolDefinition vBitWithoutAngle = new ToolDefinition();
        vBitWithoutAngle.setShape(EndmillShape.V_BIT);
        vBitWithoutAngle.setDiameter(10);
        assertThat(ToolProfile.fromDefinition(vBitWithoutAngle).heightAt(1)).isCloseTo(1, within(1e-9));

        ToolDefinition downcut = new ToolDefinition();
        downcut.setShape(EndmillShape.DOWNCUT);
        downcut.setDiameter(3);
        assertThat(ToolProfile.fromDefinition(downcut).kind()).isEqualTo(ToolProfile.Kind.FLAT);

        ToolDefinition zeroDiameter = new ToolDefinition();
        assertThat(ToolProfile.fromDefinition(zeroDiameter)).isSameAs(ToolProfile.DEFAULT);
    }
}
