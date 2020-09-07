/*
    Copyright 2018-2020 Will Winder

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
package com.willwinder.universalgcodesender;

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.willwinder.universalgcodesender.gcode.util.Code.G1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G2;
import static com.willwinder.universalgcodesender.gcode.util.Code.G3;
import static com.willwinder.universalgcodesender.gcode.util.Code.G38_2;
import static com.willwinder.universalgcodesender.gcode.util.Code.G92_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author wwinder
 */
public class GcodePreprocessorUtilsTest {
    
    public GcodePreprocessorUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of overrideSpeed method, of class CommUtils.
     */
    @Test
    public void testOverrideSpeed() {
        System.out.println("overrideSpeed");
        String command;
        double speed;
        String expResult;
        String result;

        
        command = "some command F100 blah blah blah";
        speed = 22.5;
        expResult = "some command F22.5 blah blah blah";
        result = GcodePreprocessorUtils.overrideSpeed(command, speed);
        assertEquals(expResult, result);
        
        command = "some command F100.0 blah blah blah";
        result = GcodePreprocessorUtils.overrideSpeed(command, speed);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseComment method, of class GrblUtils.
     */
    @Test
    public void testParseComment() {
        System.out.println("parseComment");
        String command;
        String expResult;
        String result;
        
        command   = "some command ;comment";
        expResult = "comment";
        result = GcodePreprocessorUtils.parseComment(command);
        assertEquals(expResult, result);
        
        command   = "some (comment here) command ;comment";
        expResult = "comment here";
        result = GcodePreprocessorUtils.parseComment(command);
        assertEquals(expResult, result);
    }

    /**
     * Test of truncateDecimals method, of class GcodePreprocessorUtils.
     */
    @Test
    public void testTruncateDecimals() {
        System.out.println("truncateDecimals");
        int length;
        String command;
        String result;
        String expResult;
        
        // Length tests.
        length = 0;
        command = "G1 X0.11111111111111111111";
        expResult = "G1 X0";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 8;
        expResult = "G1 X0.11111111";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 800;
        expResult = command;
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        // Rounding tests.
        length = 3;
        command = "G1 X1.5555555";
        expResult = "G1 X1.556";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 0;
        expResult = "G1 X2";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 5;
        command = "G1 X1.99999999";
        expResult = "G1 X2";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        // Multiple hits.
        length = 3;
        command = "G1 X1.23456 Y9.87654 Z104.49443";
        expResult = "G1 X1.235 Y9.877 Z104.494";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testParseCodes() {
        System.out.println("parseCodes");
        
        // Basic case, find one gcode.
        List<String> sl = new ArrayList<>();
        sl.add("G0");
        sl.add("X7");
        sl.add("Y5.235235");
        List<String> l = GcodePreprocessorUtils.parseCodes(sl, 'G');
        assertEquals(1, l.size());
        assertEquals("0", l.get(0));
        
        // Find two gcodes.
        sl.add("G20");
        l = GcodePreprocessorUtils.parseCodes(sl, 'G');
        assertEquals(2, l.size());
        assertEquals("0", l.get(0));
        assertEquals("20", l.get(1));
        
        // Find X, mismatched case.
        sl.add("G20");
        l = GcodePreprocessorUtils.parseCodes(sl, 'x');
        assertEquals(1, l.size());
        assertEquals("7", l.get(0));
    }

    @Test
    public void parseCoord() throws Exception {
        List<String> args = ImmutableList.of("G10", "G3", "X100", "y-.5", "Z0.25");
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'x')).isEqualTo(100);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'y')).isEqualTo(-0.5);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'z')).isEqualTo(0.25);

        assertThat(GcodePreprocessorUtils.parseCoord(args, 'X')).isEqualTo(100);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'Y')).isEqualTo(-0.5);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'Z')).isEqualTo(0.25);
    }

    @Test
    public void extractWord() throws Exception {
        List<String> args = ImmutableList.of("G10", "G3", "X100", "y-.5", "Z0.25");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'x')).isEqualTo("X100");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'y')).isEqualTo("y-.5");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'z')).isEqualTo("Z0.25");

        assertThat(GcodePreprocessorUtils.extractWord(args, 'X')).isEqualTo("X100");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'Y')).isEqualTo("y-.5");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'Z')).isEqualTo("Z0.25");
    }
    
    @Test
    public void testGetGcodes() throws Exception {
        List<String> args = ImmutableList.of("F100", "M30", "G1", "G2", "F100", "G3", "G92.1", "G38.2", "S1300");
        Set<Code> codes = GcodePreprocessorUtils.getGCodes(args);
        assertThat(codes).containsExactly(G1, G2, G3, G92_1, G38_2);
    }

    @Test
    public void testExtractMotion() throws Exception {
        assertThat(GcodePreprocessorUtils.extractMotion(G3, "G17 G03 X0 Y12 I0.25 J-0.25 K1.99 F100"))
                .hasFieldOrPropertyWithValue("extracted", "G03X0Y12I0.25J-0.25K1.99")
                .hasFieldOrPropertyWithValue("remainder", "G17F100");

        assertThat(GcodePreprocessorUtils.extractMotion(G1, "G17 G03 X0 Y12 I0.25 J-0.25 K1.99 F100"))
                .isNull();

        assertThat(GcodePreprocessorUtils.extractMotion(G1, ""))
                .isNull();

        assertThat(GcodePreprocessorUtils.extractMotion(G1, "G53 G0 X0"))
                .isNull();

        assertThat(GcodePreprocessorUtils.extractMotion(G1, "G53 G01 X0 F100 S1300"))
                .hasFieldOrPropertyWithValue("extracted", "G53G01X0")
                .hasFieldOrPropertyWithValue("remainder", "F100S1300");

        assertThat(GcodePreprocessorUtils.extractMotion(G3, "G53 G03 X0 F100 S1300"))
                .hasFieldOrPropertyWithValue("extracted", "G03X0")
                .hasFieldOrPropertyWithValue("remainder", "G53F100S1300");

        assertThat(GcodePreprocessorUtils.extractMotion(G1, "X0  Y0 Z1 F100 S1300"))
                .hasFieldOrPropertyWithValue("extracted", "X0Y0Z1")
                .hasFieldOrPropertyWithValue("remainder", "F100S1300");
    }

    @Test
    public void processSleepCommand() {
        String command = "$SLP";
        List<String> args = GcodePreprocessorUtils.splitCommand(command);
        assertEquals(1, args.size());
        assertEquals("$SLP", args.get(0));
    }

    @Test
    public void testSplitCommand() {
        List<String> splitted = GcodePreprocessorUtils.splitCommand("G53F100S1300");
        assertEquals(3, splitted.size());
        assertEquals("G53", splitted.get(0));
        assertEquals("F100", splitted.get(1));
        assertEquals("S1300", splitted.get(2));

        splitted = GcodePreprocessorUtils.splitCommand("G53 F 100 S 1300");
        assertEquals(3, splitted.size());
        assertEquals("G53", splitted.get(0));
        assertEquals("F100", splitted.get(1));
        assertEquals("S1300", splitted.get(2));

        splitted = GcodePreprocessorUtils.splitCommand("G53G90.1S1300");
        assertEquals(3, splitted.size());
        assertEquals("G90.1", splitted.get(1));

        splitted = GcodePreprocessorUtils.splitCommand("G53G90_1S1300");
        assertEquals(4, splitted.size());
        assertEquals("G90", splitted.get(1));
        assertEquals("1", splitted.get(2));
    }

    @Test
    public void splitCommandWithComments() {
        List<String> splitted = GcodePreprocessorUtils.splitCommand("(comment)G1X10");
        assertEquals(3, splitted.size());

        splitted = GcodePreprocessorUtils.splitCommand("(comment)G1X10(comment)");
        assertEquals(4, splitted.size());

        splitted = GcodePreprocessorUtils.splitCommand(";commentG1X10(comment)");
        assertEquals(1, splitted.size());
    }

    @Test
    public void processCommandWithBlockComments() throws Exception {
        List<String> splitted = GcodePreprocessorUtils.splitCommand("(hello world)G3");
        assertThat(splitted.size()).isEqualTo(2);

        splitted = GcodePreprocessorUtils.splitCommand("(1)(2)G3(3)");
        assertThat(splitted.size()).isEqualTo(4);
    }

    @Test
    public void normalizeCommand() throws Exception {
        GcodeState state = new GcodeState();

        // Add state to a complete command, ignoring stale motion mode.
        assertThat(GcodePreprocessorUtils.normalizeCommand("G1X0Y0", state))
                .isEqualTo("F0.0S0.0G1X0Y0");

        state.currentMotionMode = Code.G1;
        state.speed = 12.5;

        // Add state and insert implicit motion mode.
        assertThat(GcodePreprocessorUtils.normalizeCommand("X0Y0", state))
                .isEqualTo("F12.5S0.0G1X0Y0");
    }
}
