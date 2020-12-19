package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RotateProcessorTest {
    @Test
    public void rotateCommandShouldIncludeFeedRate() throws GcodeParserException {
        RotateProcessor rotateProcessor = new RotateProcessor(new Position(0, 0, 0, UnitUtils.Units.MM), Math.PI);

        List<String> commands = rotateProcessor.processCommand("G0 X1 Y0 F100", new GcodeState());

        assertEquals(1, commands.size());
        assertEquals("F100.0S0.0G0X-1Y-0Z0", commands.get(0));
    }

    @Test
    public void rotateCommandShouldIncludeSpindleSpeed() throws GcodeParserException {
        RotateProcessor rotateProcessor = new RotateProcessor(new Position(0, 0, 0, UnitUtils.Units.MM), Math.PI);

        List<String> commands = rotateProcessor.processCommand("G0 X1 Y0 S100", new GcodeState());

        assertEquals(1, commands.size());
        assertEquals("F0.0S100.0G0X-1Y-0Z0", commands.get(0));
    }

    @Test
    public void rotateCommandShouldIgnoreNoMotionCommands() throws GcodeParserException {
        RotateProcessor rotateProcessor = new RotateProcessor(new Position(0, 0, 0, UnitUtils.Units.MM), Math.PI);

        List<String> commands = rotateProcessor.processCommand("G21 G53", new GcodeState());

        assertEquals(1, commands.size());
        assertEquals("G21 G53", commands.get(0));
    }

    @Test
    public void rotateCommandShouldConvertArcs() throws GcodeParserException {
        RotateProcessor rotateProcessor = new RotateProcessor(new Position(0, 0, 0, UnitUtils.Units.MM), Math.PI);

        List<String> commands = rotateProcessor.processCommand("G2 X0. Y-0.5 I0.5 J0.0", new GcodeState());

        assertEquals(28, commands.size());
    }

    @Test
    public void rotateCommandShouldConvertCoordinatesInDifferentUnits() throws GcodeParserException {
        RotateProcessor rotateProcessor = new RotateProcessor(new Position(25.4, 0, 0, UnitUtils.Units.MM), Math.PI);
        GcodeState gcodeState = new GcodeState();
        gcodeState.units = Code.G20; // Inches
        gcodeState.isMetric = false;
        gcodeState.currentPoint = new Position(2,0,0, UnitUtils.Units.INCH);

        List<String> commands = rotateProcessor.processCommand("G0 X2", gcodeState);

        assertEquals("F0.0S0.0G0X-0Y-0Z0", commands.get(0));
    }
}
