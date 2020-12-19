package com.willwinder.universalgcodesender.gcode.util;

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.willwinder.universalgcodesender.gcode.util.Code.G0;
import static com.willwinder.universalgcodesender.gcode.util.Code.G1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G3;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GcodeParserUtilsTest {
    @Test
    public void stateInitialized() throws Exception {
        GcodeState state = new GcodeState();
        Assert.assertEquals(G0, state.currentMotionMode);
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("X1", 0, new GcodeState());
        Assert.assertEquals(1, metaList.size());
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        Assert.assertEquals(G0, meta.code);
    }

    @Test
    public void multipleAxisWordCommands() {
        assertThatThrownBy(() -> GcodeParserUtils.processCommand("G0G1X1X2", 0, new GcodeState()))
                .isInstanceOf(GcodeParserException.class)
                .hasMessageStartingWith(Localization.getString("parser.gcode.multiple-axis-commands"));
    }

    @Test
    public void missingAxisWords() {
        assertThatThrownBy(() -> GcodeParserUtils.processCommand("G38.2", 0, new GcodeState()))
                .isInstanceOf(GcodeParserException.class)
                .hasMessage(Localization.getString("parser.gcode.missing-axis-commands") + ": G38.2");
    }

    @Test
    public void duplicateFeedException() {
        assertThatThrownBy(() -> GcodeParserUtils.processCommand("F1F1", 0, new GcodeState()))
                .isInstanceOf(GcodeParserException.class)
                .hasMessage("Multiple F-codes on one line.");
    }

    @Test
    public void duplicateSpindleException() {
        assertThatThrownBy(() -> GcodeParserUtils.processCommand("S1S1", 0, new GcodeState()))
                .isInstanceOf(GcodeParserException.class)
                .hasMessage("Multiple S-codes on one line.");
    }

    @Test
    public void g28WithAxes() throws Exception {
        // No exception
        GcodeParserUtils.processCommand("G28 X1 Y2 Z3", 0, new GcodeState());
    }

    @Test
    public void g28NoAxes() throws Exception {
        // No exception
        GcodeParserUtils.processCommand("G28", 0, new GcodeState());
    }

    @Test
    public void motionNoAxes() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("G3", 0, new GcodeState());
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        assertThat(meta.code).isEqualTo(G3);
        assertThat(meta.state.currentPoint).isEqualTo(new Position(0, 0, 0, MM));
    }

    @Test
    public void processCommandWithBlockComment() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("(hello world)G3", 0, new GcodeState());
        assertThat(metaList.size()).isEqualTo(1);

        metaList = GcodeParserUtils.processCommand("(1)(2)G3(3)", 0, new GcodeState());
        assertThat(metaList.size()).isEqualTo(1);
    }

    @Test
    public void spaceInAxisWord() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("G \t1 X-1Y  - 0.\t5Z\n1 .0", 0, new GcodeState());
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        assertThat(meta.code).isEqualTo(G1);
        assertThat(meta.state.currentPoint).isEqualTo(new Position(-1, -0.5, 1, MM));
    }

    @Test
    public void fWordOnly() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("F100", 0, new GcodeState(), true);
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        assertThat(meta.state.speed).isEqualTo(100.0);
    }

    @Test
    public void fWordFromJogCommandShouldNotBeParsed() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("$J=G21G91X10F99", 0, new GcodeState(), true);
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        assertThat(meta.state.speed).isEqualTo(0.0);
    }

    @Test
    public void sWordOnly() throws Exception {
        List<GcodeParser.GcodeMeta> metaList = GcodeParserUtils.processCommand("S100", 0, new GcodeState(), true);
        GcodeParser.GcodeMeta meta = Iterables.getOnlyElement(metaList);
        assertThat(meta.state.spindleSpeed).isEqualTo(100.0);
    }
}
