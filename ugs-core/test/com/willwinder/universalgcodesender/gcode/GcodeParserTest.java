/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
public class GcodeParserTest {
    
    public GcodeParserTest() {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void testCommand(List<PointSegment> segments, int numResults, double speed,
            double x, double y, double z,
            boolean fastTraversal, boolean zMovement, boolean arc, boolean clockwise,
            boolean isMetric,
            int num) {
        assertEquals(numResults, segments.size());

        for (PointSegment ps : segments) {
            assertEquals(ps.getSpeed(), speed, 0);
            assertEquals(x, ps.point().x, 0);
            assertEquals(y, ps.point().y, 0);
            assertEquals(z, ps.point().z, 0);
            assertEquals(fastTraversal, ps.isFastTraverse());
            assertEquals(zMovement, ps.isZMovement());
            assertEquals(arc, ps.isArc());
            if (arc) {
                assertEquals(clockwise, ps.isClockwise());
            }
            assertEquals(num, ps.getLineNumber());
            assertEquals(isMetric, ps.isMetric());
        }
    }

    /**
     * Test of addCommand method, of class GcodeParser.
     */
    @Test
    public void testAddCommand_String() throws Exception {
        System.out.println("addCommand");
        List<PointSegment> results;
        GcodeParser instance = new GcodeParser();

        // X movement with speed
        results = instance.addCommand("G20 G0X1F150");
        testCommand(results, 1, 150, 1., 0., 0., true, false, false, false, false, 0);

        results = instance.addCommand("G1Y1F250");
        testCommand(results, 1, 250, 1., 1., 0., false, false, false, false, false, 1);

        // Use same speed from before
        results = instance.addCommand("G1Z1");
        testCommand(results, 1, 250, 1., 1., 1., false, true, false, false, false, 2);

        // Use same G command from before
        results = instance.addCommand("X2Y2Z2");
        testCommand(results, 1, 250, 2., 2., 2., false, false, false, false, false, 3);

        results = instance.addCommand("X-0.5Y0Z0");
        testCommand(results, 1, 250, -0.5, 0., 0., false, false, false, false, false, 4);

        // Clockwise arc!
        results = instance.addCommand("G2 X0. Y0.5 I0.5 J0. F2.5");
        testCommand(results, 1, 2.5, 0., 0.5, 0., false, false, true, true, false, 5);

        results = instance.addCommand("X0.5 Y0. I0. J-0.5");
        testCommand(results, 1, 2.5, 0.5, 0., 0., false, false, true, true, false, 6);

        results = instance.addCommand("X0. Y-0.5 I-0.5 J0.");
        testCommand(results, 1, 2.5, 0., -0.5, 0., false, false, true, true, false, 7);
   
        results = instance.addCommand("X-0.5 Y0. I0. J0.5");
        testCommand(results, 1, 2.5, -0.5, 0., 0., false, false, true, true, false, 8);
   
        // Move up a bit.
        results = instance.addCommand("G0 Z2");
        testCommand(results, 1, 2.5, -0.5, 0., 2., true, true, false, false, false, 9);

        // Counter-clockwise arc!
        results = instance.addCommand("G3 X-0.5 Y0. I0. J0.5");
        testCommand(results, 1, 2.5, -0.5, 0., 2., false, false, true, false, false, 10);

        results = instance.addCommand("X0. Y-0.5 I-0.5 J0.");
        testCommand(results, 1, 2.5, 0., -0.5, 2., false, false, true, false, false, 11);

        results = instance.addCommand("X0.5 Y0. I0. J-0.5");
        testCommand(results, 1, 2.5, 0.5, 0., 2., false, false, true, false, false, 12);

        results = instance.addCommand("X0. Y0.5 I0.5 J0. F2.5");
        testCommand(results, 1, 2.5, 0., 0.5, 2., false, false, true, false, false, 13);
    }

    /**
     * Test of addCommand method, of class GcodeParser.
     */
    @Test
    public void testAddCommand_String_int() throws Exception {
        System.out.println("addCommand");
        GcodeParser instance = new GcodeParser();

        // More or less the same thing as the above test, so just make sure the
        // line number is applied.
        List<PointSegment> results = instance.addCommand("G20 G0X1F150", 123);
        testCommand(results, 1, 150, 1., 0., 0., true, false, false, false, false, 123);
    }

    /**
     * Test of getCurrentPoint method, of class GcodeParser.
     */
    @Test
    public void testGetCurrentState() throws Exception {
        System.out.println("getCurrentPoint");
        GcodeParser instance = new GcodeParser();

        instance.addCommand("G17 G21 G90 G94 G54 M0 M5 M9");
        GcodeState state = instance.getCurrentState();
        assertEquals(Plane.XY, state.plane);
        assertEquals(true, state.isMetric);
        assertEquals(true, state.inAbsoluteMode);
    }

    /**
     * Test of addCommandProcessor method, of class GcodeParser.
     */
    @Test
    public void testAddCommandProcessor() {
        System.out.println("addCommandProcessor");
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        assertEquals(1, instance.numCommandProcessors());
    }

    /**
     * Test of resetCommandProcessors method, of class GcodeParser.
     */
    @Test
    public void testResetCommandProcessors() {
        System.out.println("resetCommandProcessors");
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        assertEquals(1, instance.numCommandProcessors());
        instance.resetCommandProcessors();
        assertEquals(0, instance.numCommandProcessors());
    }

    /**
     * Test of preprocessCommand method, of class GcodeParser.
     */
    @Test
    public void testPreprocessCommandGood() throws Exception {
        System.out.println("preprocessCommandGood");
        // Tests:
        // '(comment)' is removed
        // '; Comment!' is removed
        // 'M30' is removed
        // Decimal truncated to 0.88889
        // Remove spaces
        String command = "(comment) G01 X0.888888888888888888 M30; Comment!";
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        instance.addCommandProcessor(new DecimalProcessor(5));
        instance.addCommandProcessor(new M30Processor());
        instance.addCommandProcessor(new WhitespaceProcessor());
        instance.addCommandProcessor(new CommandLengthProcessor(50));
        List<String> result = instance.preprocessCommand(command);
        assertEquals(1, result.size());
        assertEquals("G01X0.88889", result.get(0));
    }

    @Test
    public void testPreprocessCommandFeedOverride() throws Exception {
        System.out.println("preprocessCommandFeedOverride");
        // Tests:
        // '(comment)' is removed
        // '; Comment!' is removed
        // 'M30' is removed
        // Decimal truncated to 0.88889
        // Remove spaces
        String command = "(comment) G01 X0.888888888888888888 M30 F100; Comment!";
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        instance.addCommandProcessor(new FeedOverrideProcessor(0.));
        instance.addCommandProcessor(new DecimalProcessor(5));
        instance.addCommandProcessor(new M30Processor());
        instance.addCommandProcessor(new WhitespaceProcessor());
        instance.addCommandProcessor(new CommandLengthProcessor(50));
        List<String> result = instance.preprocessCommand(command);
        assertEquals(1, result.size());
        assertEquals("G01X0.88889F100", result.get(0));

        instance.resetCommandProcessors();
        instance.addCommandProcessor(new CommentProcessor());
        instance.addCommandProcessor(new FeedOverrideProcessor(200.));
        instance.addCommandProcessor(new DecimalProcessor(5));
        instance.addCommandProcessor(new M30Processor());
        instance.addCommandProcessor(new WhitespaceProcessor());
        instance.addCommandProcessor(new CommandLengthProcessor(50));
        result = instance.preprocessCommand(command);
        assertEquals(1, result.size());
        assertEquals("G01X0.88889F200.0", result.get(0));
    }

    @Test
    public void testPreprocessCommandException() throws Exception {
        System.out.println("preprocessCommandException?!");
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        // Don't process decimals to make this test easier to create.
        instance.addCommandProcessor(new DecimalProcessor(0));
        instance.addCommandProcessor(new M30Processor());
        instance.addCommandProcessor(new WhitespaceProcessor());
        instance.addCommandProcessor(new CommandLengthProcessor(50));

        // Shouldn't throw if exactly 50 characters long.
        String command = "G01X0.88888888888888888888888888888888888888888888";
        instance.preprocessCommand(command);

        // Should throw an exception when it is 51 characters long.
        boolean threw = false;
        try {
            command += "8";
            instance.preprocessCommand(command);
        } catch (GcodeParserException gpe) {
            threw = true;
        }
        assertEquals(true, threw);
    }

    @Test
    @Ignore // Arc feature disabled for now...
    public void testPreprocessCommandArc() throws Exception {
        System.out.println("preprocessCommandArc");
        /*
        GcodeParser instance = new GcodeParser();
        instance.setConvertArcsToLines(true);
        instance.addCommand("G0X-1");
        List<String> commands = instance.preprocessCommand("G2 Y1 X0 I1 J0");
        System.out.println("num: " + commands.size());
        */
    }
}
