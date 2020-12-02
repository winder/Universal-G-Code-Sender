/*
    Copyright 2016-2020 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.LineSplitter;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.MeshLeveler;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author wwinder
 */
public class GcodeParserTest {
    
    private void testCommand(List<GcodeMeta> segments, int numResults, double speed,
            double x, double y, double z,
            boolean fastTraversal, boolean zMovement, boolean arc, boolean clockwise,
            boolean isMetric,
            int num) {
        int points = 0;
        for (GcodeMeta meta : segments) {
            if (meta.point != null) {
                points++;
                PointSegment ps = meta.point;
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

        assertEquals(numResults, points);

    }

    /**
     * Test of addCommand method, of class GcodeParser.
     */
    @Test
    public void testAddCommand_String() throws Exception {
        System.out.println("addCommand");
        List<GcodeMeta> results;
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
        List<GcodeMeta> results = instance.addCommand("G20 G0X1F150", 123);
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
        instance.clearCommandProcessors();
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
        List<String> result = instance.preprocessCommand(command, instance.getCurrentState());
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
        List<String> result = instance.preprocessCommand(command, instance.getCurrentState());
        assertEquals(1, result.size());
        assertEquals("G01X0.88889F100", result.get(0));

        instance.clearCommandProcessors();
        instance.addCommandProcessor(new CommentProcessor());
        instance.addCommandProcessor(new FeedOverrideProcessor(200.));
        instance.addCommandProcessor(new DecimalProcessor(5));
        instance.addCommandProcessor(new M30Processor());
        instance.addCommandProcessor(new WhitespaceProcessor());
        instance.addCommandProcessor(new CommandLengthProcessor(50));
        result = instance.preprocessCommand(command, instance.getCurrentState());
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
        final String command = "G01X0.88888888888888888888888888888888888888888888";
        instance.preprocessCommand(command, instance.getCurrentState());

        // Should throw an exception when it is 51 characters long.
        Assertions.assertThatThrownBy(() -> instance.preprocessCommand(command + "8", instance.getCurrentState()))
                .isInstanceOf(GcodeParserException.class);
    }

    @Test
    public void autoLevelerProcessorSet() throws Exception {
        System.out.println("autoLevelerProcessorSet");
        GcodeParser gcp = new GcodeParser();
        gcp.addCommandProcessor(new CommentProcessor());
        gcp.addCommandProcessor(new ArcExpander(true, 0.1));
        gcp.addCommandProcessor(new LineSplitter(1));
        Position grid[][] = {
            { new Position(-5,-5,0, MM), new Position(-5,35,0, MM) },
            { new Position(35,-5,0, MM), new Position(35,35,0, MM) }
        };
        gcp.addCommandProcessor(new MeshLeveler(0, grid, Units.MM));

        Path output = Files.createTempFile("autoleveler_processor_set_test.nc", "");

        // Copy resource to temp file since my parser methods need it that way.
        URL file = this.getClass().getClassLoader().getResource("./gcode/circle_test.nc");
        File tempFile = File.createTempFile("temp", "file");
        IOUtils.copy(file.openStream(), FileUtils.openOutputStream(tempFile));

        try (IGcodeWriter gcw = new GcodeStreamWriter(output.toFile())) {
            GcodeParserUtils.processAndExport(gcp, tempFile, gcw);
        }

        IGcodeStreamReader reader = new GcodeStreamReader(output.toFile());

        file = this.getClass().getClassLoader().getResource("./gcode/circle_test.nc.processed");
        Files.lines(Paths.get(file.toURI())).forEach((t) -> {
            try {
                GcodeCommand c = reader.getNextCommand();
                if (c == null) {
                    Assert.fail("Reached end of gcode reader before end of expected commands.");
                }
                Assert.assertEquals(c.getCommandString(), t);
            } catch (IOException ex) {
                Assert.fail("Unexpected exception.");
            }
        });
        assertEquals(1030, reader.getNumRows());
        output.toFile().delete();
    }

    @Test
    public void nonGcodeIgnoresImplicitGcode() throws Exception {
        GcodeParser gcp = new GcodeParser();
        gcp.addCommandProcessor(new CommentProcessor());
        GcodeState initialState = new GcodeState();
        initialState.currentPoint = new Position(0, 0, 1, MM);
        initialState.currentMotionMode = Code.G0;
        List<String> result = gcp.preprocessCommand("M05", initialState);
        assertEquals(1, result.size());
        assertEquals("M05", result.get(0));
    }

    @Test
    public void doubleParenCommentWithCommentProcessorTest() throws Exception {
        String command = "(comment (with subcomment) still in the comment) G01 X10";
        GcodeParser instance = new GcodeParser();
        instance.addCommandProcessor(new CommentProcessor());
        List<String> result = instance.preprocessCommand(command, instance.getCurrentState());
        assertEquals(1, result.size());
        assertEquals(" G01 X10", result.get(0));

    }
}
