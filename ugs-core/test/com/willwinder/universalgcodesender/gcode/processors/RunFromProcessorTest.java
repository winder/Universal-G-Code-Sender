package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> result = processor.processCommand("G0X10Z0F100", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 1;
        result = processor.processCommand("G0Y10S1000", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 2;
        result = processor.processCommand("G0Y11", gcodeState);

        assertEquals(6, result.size());
        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0Z0.0", result.get(1));
        assertEquals("G0X10.0Y10.0", result.get(2));
        assertEquals("S1000.0F100.0", result.get(3));
        assertEquals("G1Z0.0", result.get(4));
        assertEquals("F100.0S1000.0G0Y11", result.get(5));
    }

    @Test
    public void processCommandShouldNotAddZIfNotDefined() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(2);
        GcodeState gcodeState = new GcodeState();
        gcodeState.commandNumber = 0;
        List<String> result = processor.processCommand("G0X10F100", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 1;
        result = processor.processCommand("G0Y10S1000", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 2;
        result = processor.processCommand("G0Y11", gcodeState);

        assertEquals(4, result.size());
        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0X10.0Y10.0", result.get(1));
        assertEquals("S1000.0F100.0", result.get(2));
        assertEquals("F100.0S1000.0G0Y11", result.get(3));
    }

    @Test
    public void processCommandShouldNotAddYIfNotDefined() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(2);
        GcodeState gcodeState = new GcodeState();
        gcodeState.commandNumber = 0;
        List<String> result = processor.processCommand("G0X10F100", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 1;
        result = processor.processCommand("G0S1000", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 2;
        result = processor.processCommand("G0Y11", gcodeState);

        assertEquals(4, result.size());
        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0X10.0", result.get(1));
        assertEquals("S1000.0F100.0", result.get(2));
        assertEquals("F100.0S1000.0G0Y11", result.get(3));
    }

    @Test
    public void processCommandShouldNotAddXIfNotDefined() throws GcodeParserException {
        RunFromProcessor processor = new RunFromProcessor(2);
        GcodeState gcodeState = new GcodeState();
        gcodeState.commandNumber = 0;
        List<String> result = processor.processCommand("G0F100", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 1;
        result = processor.processCommand("G0S1000", gcodeState);
        assertEquals(0, result.size());

        gcodeState.commandNumber = 2;
        result = processor.processCommand("G0Y11", gcodeState);

        assertEquals(4, result.size());
        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0", result.get(1));
        assertEquals("S1000.0F100.0", result.get(2));
        assertEquals("F100.0S1000.0G0Y11", result.get(3));
    }

    @Test
    public void processArcs() throws GcodeParserException {
        List<String> gcode = Arrays.asList("G17 G21 G90 G94 G54 M0 M5 M9",
                "G0 Z1 F100 S100",
                "X-5 Y0",
                "M3",
                "G1 Z0",
                "G2 X0. Y-5 I5 J0",
                "G2 X-5 Y0 I0 J5");

        RunFromProcessor processor = new RunFromProcessor(6);
        GcodeState gcodeState = new GcodeState();

        List<String> result = new ArrayList<>();
        for (int i = 0; i < gcode.size(); i++) {
            gcodeState.commandNumber = i;
            result.addAll(processor.processCommand(gcode.get(i), gcodeState));
        }

        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0Z1.0", result.get(1));
        assertEquals("G0X0.0Y-5.0", result.get(2)); // Should start where the first arc segment ended
        assertEquals("M3S100.0F100.0", result.get(3));
        assertEquals("G1Z0.0", result.get(4));
        assertEquals("F100.0S100.0G2X-5Y0I0J5", result.get(5)); // Should contain the last arc segment only
    }

    @Test
    public void processArcsAfterExpand() throws GcodeParserException {
        List<String> gcode = Arrays.asList("G17 G21 G90 G94 G54 M0 M5 M9",
                "G0 Z1 F100 S100",
                "X-5 Y0",
                "M3",
                "G1 Z0",
                "G2 X0. Y-5 I5 J0",
                "G2 X-5 Y0 I0 J5");

        CommandProcessorList processorList = new CommandProcessorList();
        processorList.add(new ArcExpander(true, 1));
        processorList.add(new RunFromProcessor(6));

        GcodeState gcodeState = new GcodeState();
        gcodeState.currentPoint = new Position(0,0,0, UnitUtils.Units.MM);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < gcode.size(); i++) {
            gcodeState.commandNumber = i;
            result.addAll(processorList.processCommand(gcode.get(i), gcodeState));
        }

        assertEquals("G21G90G91.1G94G54G17", result.get(0));
        assertEquals("G0Z1.0", result.get(1));
        assertEquals("G0X0.0Y-5.0", result.get(2)); // Should start where the first arc segment ended
        assertEquals("M3S100.0F100.0", result.get(3));
        assertEquals("G1Z0.0", result.get(4));

        String regex = "F100\\.0S100\\.0G1X-0\\.9\\d+Y0\\.0\\d+Z0";
        assertTrue("Expected a movement command with the the active feed and speed matching \"" +  regex + "\" but was " + result.get(5), result.get(5).matches(regex));
    }
}
