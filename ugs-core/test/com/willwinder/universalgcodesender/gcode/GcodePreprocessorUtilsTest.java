/*
    Copyright 2023 Will Winder

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

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.util.Code;
import static com.willwinder.universalgcodesender.gcode.util.Code.G1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G2;
import static com.willwinder.universalgcodesender.gcode.util.Code.G3;
import static com.willwinder.universalgcodesender.gcode.util.Code.G38_2;
import static com.willwinder.universalgcodesender.gcode.util.Code.G92_1;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.INCH;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Joacim Breiler
 */
public class GcodePreprocessorUtilsTest {
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

        command = "some command ;comment";
        expResult = "comment";
        result = GcodePreprocessorUtils.parseComment(command);
        assertEquals(expResult, result);

        command = "some (comment here) command ;comment";
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
    public void parseCoord() {
        List<String> args = ImmutableList.of("G10", "G3", "X100", "y-.5", "Z0.25");
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'x')).isEqualTo(100);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'y')).isEqualTo(-0.5);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'z')).isEqualTo(0.25);

        assertThat(GcodePreprocessorUtils.parseCoord(args, 'X')).isEqualTo(100);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'Y')).isEqualTo(-0.5);
        assertThat(GcodePreprocessorUtils.parseCoord(args, 'Z')).isEqualTo(0.25);
    }

    @Test
    public void extractWord() {
        List<String> args = ImmutableList.of("G10", "G3", "X100", "y-.5", "Z0.25");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'x')).isEqualTo("X100");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'y')).isEqualTo("y-.5");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'z')).isEqualTo("Z0.25");

        assertThat(GcodePreprocessorUtils.extractWord(args, 'X')).isEqualTo("X100");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'Y')).isEqualTo("y-.5");
        assertThat(GcodePreprocessorUtils.extractWord(args, 'Z')).isEqualTo("Z0.25");
    }

    @Test
    public void testGetGcodes() {
        List<String> args = ImmutableList.of("F100", "M30", "G1", "G2", "F100", "G3", "G92.1", "G38.2", "S1300");
        Set<Code> codes = GcodePreprocessorUtils.getGCodes(args);
        assertThat(codes).containsExactly(G1, G2, G3, G92_1, G38_2);
    }

    @Test
    public void testExtractMotion() {
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

        splitted = GcodePreprocessorUtils.splitCommand("G01(test) F150.000");
        assertEquals(3, splitted.size());
        assertEquals("G01", splitted.get(0));
        assertEquals("(test)", splitted.get(1));
        assertEquals("F150.000", splitted.get(2));
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
    public void processCommandWithBlockComments() {
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
        state.feedRate = 12.5;

        // Add state and insert implicit motion mode.
        assertThat(GcodePreprocessorUtils.normalizeCommand("X0Y0", state))
                .isEqualTo("F12.5S0.0G1X0Y0");
    }

    @Test
    public void generatePointsAlongArcBDringShouldGenerateAnArc() {
        Position start = new Position(0, 0, 0, MM);
        Position end = new Position(10, 0, 0, MM);
        Position center = new Position(5, 0, 0, MM);
        double radius = 5;

        List<Position> points = GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, true, radius, 0, 1, new PlaneFormatter(Plane.XY));
        assertThatPointsAreWithinBoundary(start, end, radius, points);
        assertEquals(17, points.size());
        assertTrue("Coordinates are generated in wrong units", points.stream().allMatch(p -> p.getUnits() == MM));
    }

    @Test
    public void generatePointsAlongArcBDringShouldHandleNegativeRadius() {
        double radius = -10;
        Position start = new Position(10, 10, 0, MM);
        Position end = new Position(20, 0, 0, MM);
        Position center = GcodePreprocessorUtils.convertRToCenter(start, end, radius, false, true, new PlaneFormatter(Plane.XY));

        List<Position> points = GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, true, radius, 10, 10, new PlaneFormatter(Plane.XY));
        assertPosition(points.get(0), 10, 10, 0);
        assertPosition(points.get(1), 14.122, 18.090, 0);
        assertPosition(points.get(2), 23.090, 19.510, 0);
        assertPosition(points.get(3), 29.510, 13.090, 0);
        assertPosition(points.get(4), 28.090, 4.122, 0);
        assertPosition(points.get(5), 20, 0, 0);
        assertEquals(6, points.size());
        assertTrue("Coordinates are generated in wrong units", points.stream().allMatch(p -> p.getUnits() == MM));
    }

    static void assertPosition(Position position, double x, double y, double z) {
        assertEquals(position.x, x, 0.001);
        assertEquals(position.y, y, 0.001);
        assertEquals(position.z, z, 0.001);
    }

    @Test
    public void getAngle() {
        Position center = new Position(0, 0, 0, MM);
        assertEquals(0, GcodePreprocessorUtils.getAngle(new Position(-10, 0, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI / 2, GcodePreprocessorUtils.getAngle(new Position(0, -10, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI, GcodePreprocessorUtils.getAngle(new Position(10, 0, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI / 2 + Math.PI, GcodePreprocessorUtils.getAngle(new Position(0, 10, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
    }

    @Test
    public void getAngleWithCenterOffset() {
        Position center = new Position(10, 10, 0, MM);
        assertEquals(0, GcodePreprocessorUtils.getAngle(new Position(0, 10, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI / 2, GcodePreprocessorUtils.getAngle(new Position(10, 0, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI, GcodePreprocessorUtils.getAngle(new Position(20, 10, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
        assertEquals(Math.PI / 2 + Math.PI, GcodePreprocessorUtils.getAngle(new Position(10, 20, 0, MM), center, new PlaneFormatter(Plane.XY)), 0.01);
    }


    @Test
    public void convertRToCenterShouldFindCenterOnArc() {
        Position start = new Position(10, 10, 0, MM);
        Position end = new Position(20, 0, 0, MM);
        double radius = 10;
        Position center = GcodePreprocessorUtils.convertRToCenter(start, end, radius, false, true, new PlaneFormatter(Plane.XY));
        assertThat(center).isEqualTo(new Position(10, 0, 0, 0, 0, 0, MM));
    }

    @Test
    public void convertRToCenterShouldFindCenterWithNegativeRadius() {
        Position start = new Position(10, 10, 0, MM);
        Position end = new Position(20, 0, 0, MM);
        double radius = -10;
        Position center = GcodePreprocessorUtils.convertRToCenter(start, end, radius, false, true, new PlaneFormatter(Plane.XY));
        assertThat(center).isEqualTo(new Position(20, 10, 0, 0, 0, 0, MM));
    }

    @Test
    public void calculateNumberOfPointsToExpandShouldCalculate() {
        int numberOfPoints = GcodePreprocessorUtils.calculateNumberOfPointsToExpand(5, MM, 1, 1, Math.PI);
        assertEquals(16, numberOfPoints);
    }

    @Test
    public void calculateNumberOfPointsToExpandShouldHandleNegativeRadius() {
        int numberOfPoints = GcodePreprocessorUtils.calculateNumberOfPointsToExpand(-5, MM, 1, 1, Math.PI);
        assertEquals(16, numberOfPoints);
    }

    @Test
    public void generatePointsAlongArcBDringShouldGenerateAnArcWhenPositionsAreInInches() {
        Position start = new Position(0, 0, 0, UnitUtils.Units.INCH);
        Position end = new Position(10 * UnitUtils.scaleUnits(MM, INCH), 0, 0, UnitUtils.Units.INCH);
        Position center = new Position(5 * UnitUtils.scaleUnits(MM, INCH), 0, 0, UnitUtils.Units.INCH);
        double radius = 5 * UnitUtils.scaleUnits(MM, INCH);

        List<Position> points = GcodePreprocessorUtils.generatePointsAlongArcBDring(start, end, center, true, radius, 0, 1, new PlaneFormatter(Plane.XY));
        assertEquals(17, points.size());
        assertThatPointsAreWithinBoundary(start, end, radius, points);
        assertTrue("Coordinates are generated in wrong units", points.stream().allMatch(p -> p.getUnits() == INCH));
    }

    @Test
    public void overridePositionShouldUpdateTheCoordinatesInCommand() {
        PartialPosition position = PartialPosition.builder(MM).setX(1d).setY(2d).setZ(3d).setA(4d).setB(5d).setC(6d).build();
        String newCommand = GcodePreprocessorUtils.overridePosition("G0X0Y0Z0A0B0C0", position);
        assertEquals("G0X1Y2Z3A4B5C6", newCommand);
    }

    @Test
    public void overridePositionShouldAddMissingCoordinatesToCommand() {
        PartialPosition position = PartialPosition.builder(MM).setX(1d).setY(2d).setZ(3d).setA(4d).setB(5d).setC(6d).build();
        String newCommand = GcodePreprocessorUtils.overridePosition("G0", position);
        assertEquals("G0X1Y2Z3A4B5C6", newCommand);
    }

    @Test
    public void updatePointWithCommandShouldSetPositionIfOriginalIsNaN() {
        Position position = new Position(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, MM);
        Position newPosition = GcodePreprocessorUtils.updatePointWithCommand(position, 1, 2, 3, 4, 5, 6, false);
        assertEquals(1, newPosition.getX(), 0.01);
        assertEquals(2, newPosition.getY(), 0.01);
        assertEquals(3, newPosition.getZ(), 0.01);
        assertEquals(4, newPosition.getA(), 0.01);
        assertEquals(5, newPosition.getB(), 0.01);
        assertEquals(6, newPosition.getC(), 0.01);
        assertEquals(MM, newPosition.getUnits());
    }

    @Test
    public void updatePointWithCommandShouldAddToPosition() {
        Position position = new Position(1, 2, 3, 4, 5, 6, MM);
        Position newPosition = GcodePreprocessorUtils.updatePointWithCommand(position, 1, 2, 3, 4, 5, 6, false);
        assertEquals(2, newPosition.getX(), 0.01);
        assertEquals(4, newPosition.getY(), 0.01);
        assertEquals(6, newPosition.getZ(), 0.01);
        assertEquals(8, newPosition.getA(), 0.01);
        assertEquals(10, newPosition.getB(), 0.01);
        assertEquals(12, newPosition.getC(), 0.01);
        assertEquals(MM, newPosition.getUnits());
    }

    static void assertThatPointsAreWithinBoundary(Position start, Position end, double radius, List<Position> points) {
        assertTrue(points.stream().allMatch(p -> p.x >= start.x));
        assertTrue(points.stream().allMatch(p -> p.x <= end.x));
        assertTrue(points.stream().allMatch(p -> p.y >= end.y));
        assertTrue(points.stream().allMatch(p -> p.y >= end.y));
        assertTrue(points.stream().allMatch(p -> p.y <= end.y + radius));
        assertTrue(points.stream().allMatch(p -> p.y <= end.y + radius));
    }
}