package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimplifyProcessorTest {
    @Test
    public void testSimplify() throws GcodeParserException {
        SimplifyProcessor processor = new SimplifyProcessor(1);

        GcodeParser gcodeParser = new GcodeParser();

        List<String> result = new ArrayList<>();
        result.addAll(appendCommand(processor, gcodeParser, "G1X0Y0Z0"));
        result.addAll(appendCommand(processor, gcodeParser,"G1X0.9Y0Z0"));
        result.addAll(appendCommand(processor, gcodeParser,"G1X1Y0Z0"));
        result.addAll(appendCommand(processor, gcodeParser,"G1X2Y0Z0"));

        assertEquals(3, result.size());
        assertEquals("G1X0Y0Z0", result.get(0));
        assertEquals("G1X1Y0Z0", result.get(1));
        assertEquals("G1X2Y0Z0", result.get(2));
    }

    private static List<String> appendCommand(SimplifyProcessor processor, GcodeParser gcodeParser, String command) throws GcodeParserException {
        List<String> commands = processor.processCommand(command, gcodeParser.getCurrentState());
        commands.forEach(c -> {
            try {
                gcodeParser.addCommand(c);
            } catch (GcodeParserException e) {
                throw new RuntimeException(e);
            }
        });
        return commands;
    }
}