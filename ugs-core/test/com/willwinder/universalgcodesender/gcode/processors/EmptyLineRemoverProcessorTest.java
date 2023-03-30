package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class EmptyLineRemoverProcessorTest {
    @Test
    public void processCommandShouldNotRemoveEmptyLines() throws GcodeParserException {
        EmptyLineRemoverProcessor processor = new EmptyLineRemoverProcessor();
        List<String> lines = processor.processCommand("test", new GcodeState());
        assertEquals(1, lines.size());
    }

    @Test
    public void processCommandShouldRemoveTrimmedEmptyLines() throws GcodeParserException {
        EmptyLineRemoverProcessor processor = new EmptyLineRemoverProcessor();
        List<String> lines = processor.processCommand("", new GcodeState());
        assertEquals(0, lines.size());

        lines = processor.processCommand(" ", new GcodeState());
        assertEquals(0, lines.size());

        lines = processor.processCommand("  ", new GcodeState());
        assertEquals(0, lines.size());

        lines = processor.processCommand("\t", new GcodeState());
        assertEquals(0, lines.size());

        lines = processor.processCommand(null, new GcodeState());
        assertEquals(0, lines.size());
    }
}
