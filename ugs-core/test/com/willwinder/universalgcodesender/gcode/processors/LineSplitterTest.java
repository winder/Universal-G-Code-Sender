/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Point3d;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author wwinder
 */
public class LineSplitterTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static void splitterHarness(
            double splitterLength, Point3d start, String command, List<String> expected) throws Exception {
        LineSplitter instance = new LineSplitter(splitterLength);

        GcodeState state = new GcodeState();
        state.currentPoint = start;
        state.inAbsoluteMode = true;

        List<String> result = instance.processCommand(command, state);
        assertEquals(expected, result);
    }
    
    /**
     * Lines being split across each X/Y/Z axis.
     */
    @Test
    public void testSingleAxis() throws Exception {
        System.out.println("splitSingleAxis");

        List<String> expected;

        expected = Arrays.asList("G1X0Y0Z0", "G1X1Y0Z0");
        splitterHarness(1, new Point3d(-1, 0, 0), "G1X1", expected);

        expected = Arrays.asList("G1X0Y0Z0", "G1X0Y1Z0");
        splitterHarness(1, new Point3d(0, -1, 0), "G1Y1", expected);

        expected = Arrays.asList("G1X0Y0Z0", "G1X0Y0Z1");
        splitterHarness(1, new Point3d(0, 0, -1), "G1Z1", expected);
    }

    /**
     * Line running across all axes.
     */
    @Test
    public void testDiagonalLine() throws Exception {
        System.out.println("diagonalLine");

        List<String> expected = Arrays.asList("G1X-0.5Y-0.5Z-0.5", "G1X0Y0Z0", "G1X0.5Y0.5Z0.5", "G1X1Y1Z1");
        splitterHarness(1, new Point3d(-1, -1, -1), "G1X1Y1Z1", expected);
    }

    /**
     * Lines shorter than the length should not modify the input command.
     */
    @Test
    public void testIgnoreShortLine() throws Exception {
        System.out.println("shortLine");

        String command = "G1X0.01";
        double length = 2;
        List<String> expected = Arrays.asList(command);
        splitterHarness(length, new Point3d(0, 0, 0), command, expected);
    }

    /**
     * The split line should use G1 or G0 depending on the original line.
     */
    @Test
    public void testG1G0() throws Exception {
        System.out.println("G1G0");
        List<String> expected;

        expected = Arrays.asList("G1X0Y0Z0", "G1X1Y0Z0");
        splitterHarness(1, new Point3d(-1, 0, 0), "G1X1", expected);

        expected = Arrays.asList("G0X0Y0Z0", "G0X1Y0Z0");
        splitterHarness(1, new Point3d(-1, 0, 0), "G0X1", expected);
    }

    /**
     * Multiple commands on 1 line sent to the splitter should throw.
     */
    @Test
    public void testMultipleCommandsError() throws Exception {
        System.out.println("multipleCommandsError");
        LineSplitter instance = new LineSplitter(2);

        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(Localization.getString("parser.processor.general.multiple-commands"));

        String command = "G92 G1X1Y1Z1";
        splitterHarness(1, new Point3d(0, 0, 0), command, null);

        Assert.fail("Should throw an exception before reaching this point.");
    }

    /**
     * Commands without G0/G1 should not be modified.
     */
    @Test
    public void testIgnoreNonLines() throws Exception {
        System.out.println("ignoreNonLines");

        String command = "G92G2X1Y1Z1";
        List<String> expected = Arrays.asList("G92G2X1Y1Z1");
        splitterHarness(2, new Point3d(-1, -1, -1), command, expected);
    }
    
    /**
     * Sloppy fractional result.
     * (0,0,0) -> (1,0,0) line needs to split into 3 parts.
     * Verify that the infinite fraction is truncated to 4 digits.
     * Verify that the endpoint equals the real endpoint not the interpolated endpoint.
     */
    @Test
    public void testSloppyFractionRounding() throws Exception {
        System.out.println("sloppyFractions");

        double maxSegmentLength = 0.4;
        String command = "G1X1";
        List<String> expected = Arrays.asList("G1X0.3333Y0Z0", "G1X0.6667Y0Z0", "G1X1Y0Z0");
        splitterHarness(maxSegmentLength, new Point3d(0, 0, 0), command, expected);
    }
}
