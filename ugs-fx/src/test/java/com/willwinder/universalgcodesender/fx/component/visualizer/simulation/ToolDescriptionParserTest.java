package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ToolDescriptionParserTest {

    @Test
    public void shouldReadMillimeterFlatTools() {
        ToolProfile tool = ToolDescriptionParser.parse("6mm Upcut").orElseThrow();
        assertThat(tool.kind()).isEqualTo(ToolProfile.Kind.FLAT);
        assertThat(tool.diameter()).isCloseTo(6, within(1e-9));
        assertThat(ToolDescriptionParser.parse("3,175mm Downcut").orElseThrow().diameter()).isCloseTo(3.175, within(1e-9));
    }

    @Test
    public void shouldReadBallAndVBits() {
        ToolProfile ball = ToolDescriptionParser.parse("6.35mm Ball").orElseThrow();
        assertThat(ball.kind()).isEqualTo(ToolProfile.Kind.BALL);

        ToolProfile vBit = ToolDescriptionParser.parse("6mm V-bit 60°").orElseThrow();
        assertThat(vBit.kind()).isEqualTo(ToolProfile.Kind.V_BIT);
        assertThat(vBit.heightAt(1)).isCloseTo(Math.sqrt(3), within(1e-9));

        ToolProfile defaultAngle = ToolDescriptionParser.parse("6mm V-bit").orElseThrow();
        assertThat(defaultAngle.heightAt(1)).isCloseTo(1, within(1e-9));
    }

    @Test
    public void shouldReadInchFractions() {
        assertThat(ToolDescriptionParser.parse("1/4\" Upcut").orElseThrow().diameter()).isCloseTo(6.35, within(1e-9));
        assertThat(ToolDescriptionParser.parse("1/8\" V-bit 90°").orElseThrow().kind()).isEqualTo(ToolProfile.Kind.V_BIT);
        assertThat(ToolDescriptionParser.parse("0.25in Ball").orElseThrow().diameter()).isCloseTo(6.35, within(1e-9));
    }

    @Test
    public void shouldRejectDescriptionsWithoutADiameter() {
        assertThat(ToolDescriptionParser.parse("My favourite cutter")).isEmpty();
        assertThat(ToolDescriptionParser.parse("0mm Upcut")).isEmpty();
        assertThat(ToolDescriptionParser.parse(null)).isEmpty();
    }
}
