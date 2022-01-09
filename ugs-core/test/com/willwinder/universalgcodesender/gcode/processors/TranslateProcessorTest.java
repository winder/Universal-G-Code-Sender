package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TranslateProcessorTest {

    @Test
    public void translateXYShouldNotChangeZ() throws GcodeParserException {
        TranslateProcessor translateProcessor = new TranslateProcessor(new Position(10, 10, 0, UnitUtils.Units.MM));

        GcodeState state = new GcodeState();
        List<String> processedCommands = translateProcessor.processCommand("G0 X10 Y10 Z10", state);

        assertEquals(1, processedCommands.size());
        assertEquals("F0.0S0.0G0X20Y20Z10", processedCommands.get(0));
    }
    
    @Test
    public void translateXYWithNegativeValuesWithDecimalsShouldBePossible() throws GcodeParserException {
        TranslateProcessor translateProcessor = new TranslateProcessor(new Position(-20.5, -20.5, 0, UnitUtils.Units.MM));

        GcodeState state = new GcodeState();
        List<String> processedCommands = translateProcessor.processCommand("G0 X10 Y10 Z10", state);

        assertEquals(1, processedCommands.size());
        assertEquals("F0.0S0.0G0X-10.5Y-10.5Z10", processedCommands.get(0));
    }
}