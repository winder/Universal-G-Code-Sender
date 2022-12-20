package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RunFromProcessorTest {

    @Test
    public void processCommandShouldSkipCommandsForLineNumberZero() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(0);
        List<String> result = processor.processCommand("test", new GcodeState());
        assertEquals(1, result.size());
        assertEquals("test", result.get(0));
    }

    @Test
    public void processCommandShouldSkipCommandsForNegativeLineNumbers() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(-1);
        List<String> result = processor.processCommand("test", new GcodeState());
        assertEquals(1, result.size());
        assertEquals("test", result.get(0));
    }

    @Test
    public void processCommandShouldAppendStateUpToSelectedLine() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(2);
        GcodeState gcodeState = new GcodeState();
        gcodeState.commandNumber = 0;
        List<String> result = processor.processCommand("G0X10", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 1;
        result = processor.processCommand("G0Y10", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 2;
        result = processor.processCommand("G0Y11", gcodeState);

        assertEquals(6, result.size());
        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0Z0.0", result.get(1));
        assertEquals("G0X10.0Y10.0", result.get(2));
        assertEquals("S0.0F0.0", result.get(3));
        assertEquals("G1Z0.0", result.get(4));
        assertEquals("F0.0S0.0G0Y11", result.get(5));
    }
}
