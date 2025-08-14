/*
    Copyright 2017-2023 Will Winder

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
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.INCH;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

/**
 * @author wwinder
 */
public class MeshLevelerTest {
    private static final Position[][] BIG_FLAT_GRID_Z0 = {
            {new Position(0, 0, 0, MM), new Position(0, 10, 0, MM)},
            {new Position(10, 0, 0, MM), new Position(10, 10, 0, MM)}
    };
    private static final Position[][] BIG_FLAT_GRID_Z1 = {
            {new Position(0, 0, 1, MM), new Position(0, 10, 1, MM)},
            {new Position(10, 0, 1, MM), new Position(10, 10, 1, MM)}
    };
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static void sendCommandExpectResult(MeshLeveler ml, GcodeState state, String command, String... result) {
        try {
            List<String> expected = Lists.newArrayList(result);
            List<String> results = ml.processCommand(command, state);
            assertEquals(expected, results);
        } catch (GcodeParserException ex) {
            Assert.fail("Unexpected exception.");
        }
    }

    /**
     * Helper method for generating a sloped mesh in the following format where V is given through the parameter "value"
     * in the supplied units
     *
     * <pre>
     *                         V
     *       z=-V  *           |           * z=V
     *                         |
     *                         |
     *            -V --------------------- V
     *                         |
     *                         |
     *       z=-V  *           |           * z=V
     *                        -V
     * </pre>
     *
     * @param value the min/max value for each axis
     * @param units the units in which the value is given in
     * @return a Position grid
     */
    private static Position[][] generateSlopeMesh(double value, Units units) {
        return new Position[][]{
                {new Position(-value, -value, -value, units), new Position(-value, value, -value, units)},
                {new Position(value, -value, value, units), new Position(value, value, value, units)}
        };
    }

    @Test
    public void testNotEnoughPoints() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_NOT_ENOUGH_SAMPLES);

        Position[][] grid = {
                {new Position(MM)},
                {new Position(MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testNonRectangularGrid() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_MESH_SHAPE);

        Position[][] grid = {
                {new Position(MM), new Position(MM)},
                {new Position(MM), new Position(MM), new Position(MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testXAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ALIGNMENT);

        Position[][] grid = {
                {new Position(0, 0, 0, MM), new Position(1, 0, 0, MM)},
                {new Position(10, 1, 0, MM), new Position(1, 1, 0, MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testYAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ALIGNMENT);

        Position[][] grid = {
                {new Position(0, 0, 0, MM), new Position(0, 0, 0, MM)},
                {new Position(1, 0, 0, MM), new Position(1, 1, 0, MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testXAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ASCENTION);

        Position[][] grid = {
                {new Position(10, 0, 0, MM), new Position(10, 10, 0, MM)},
                {new Position(0, 0, 0, MM), new Position(0, 10, 0, MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testYAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ASCENTION);

        Position[][] grid = {
                {new Position(0, 10, 0, MM), new Position(0, 0, 0, MM)},
                {new Position(10, 0, 0, MM), new Position(10, 10, 0, MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void smallerProbedAreaShouldNotInterpolateUnprobedArea() {
        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z1);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        // Probed area should be leveled
        sendCommandExpectResult(ml, state, "G1X0Y0Z0", "G1X0Y0Z1");
        sendCommandExpectResult(ml, state, "G1X10Y10Z0", "G1X10Y10Z1");

        // Area outside should not be leveled
        sendCommandExpectResult(ml, state, "G1X-1Y-1Z0", "G1X-1Y-1Z0");
        sendCommandExpectResult(ml, state, "G1X11Y11Z0", "G1X11Y11Z0");
    }

    @Test
    public void testUnexpectedArc() throws GcodeParserException {
        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_UNEXPECTED_ARC);

        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        ml.processCommand("G2X1", state);
    }

    @Test
    public void testMultipleCommandsWithLine() {
        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X1G20", "G1X1G20Z0", "G1X1G20");
    }

    @Test
    public void testNoZChangesWithFlatMeshOnSurface() {
        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Z0");
    }

    @Test
    public void testFlatMeshOnSurfaceOffSurface() {
        MeshLeveler ml = new MeshLeveler(1.0, BIG_FLAT_GRID_Z1);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Z0");
    }

    @Test
    public void testNegativeOffset() {
        // The probe will be at 1.0 instead of 0.9 which means the end point needs to be raised 0.1
        MeshLeveler ml = new MeshLeveler(0.9, BIG_FLAT_GRID_Z1);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Z0.1");
    }

    @Test
    public void testProbedPointsInMMWithAbsoluteGcodeInInches() {
        Position[][] grid = generateSlopeMesh(25.4, MM);
        MeshLeveler ml = new MeshLeveler(0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G20;
        state.currentPoint = new Position(-1, -1, 0, INCH);
        state.inAbsoluteMode = true;

        // Moving along Y axis on flat line
        sendCommandExpectResult(ml, state, "G1Y-1", "G1Y-1Z-1");
        sendCommandExpectResult(ml, state, "G1Y0", "G1Y0Z-1");
        sendCommandExpectResult(ml, state, "G1Y1", "G1Y1Z-1");

        // Moving along X axis up slope
        sendCommandExpectResult(ml, state, "G1X-0.5", "G1X-0.5Z-0.5");
        sendCommandExpectResult(ml, state, "G1X0", "G1X0Z0");
        sendCommandExpectResult(ml, state, "G1X0.5", "G1X0.5Z0.5");
        sendCommandExpectResult(ml, state, "G1X1", "G1X1Z1");

        // Moving up slope along X/Y
        sendCommandExpectResult(ml, state, "G1X-0.5Y-0.5", "G1X-0.5Y-0.5Z-0.5");
        sendCommandExpectResult(ml, state, "G1X0Y0", "G1X0Y0Z0");
        sendCommandExpectResult(ml, state, "G1X0.5Y0.5", "G1X0.5Y0.5Z0.5");
        sendCommandExpectResult(ml, state, "G1X1Y1", "G1X1Y1Z1");
    }

    @Test
    public void testProbedPointsInInchesWithAbsoluteGcodeInMM() {
        Position[][] grid = generateSlopeMesh(1, INCH);
        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G21;
        state.currentPoint = new Position(-25.4, -25.4, 0, MM);
        state.inAbsoluteMode = true;

        // Moving along Y axis on flat line
        sendCommandExpectResult(ml, state, "G1Y-25.4", "G1Y-25.4Z-25.4");
        sendCommandExpectResult(ml, state, "G1Y0", "G1Y0Z-25.4");
        sendCommandExpectResult(ml, state, "G1Y25.4", "G1Y25.4Z-25.4");

        // Moving along X axis up slope
        sendCommandExpectResult(ml, state, "G1X-12.7", "G1X-12.7Z-12.7");
        sendCommandExpectResult(ml, state, "G1X0", "G1X0Z0");
        sendCommandExpectResult(ml, state, "G1X12.7", "G1X12.7Z12.7");
        sendCommandExpectResult(ml, state, "G1X25.4", "G1X25.4Z25.4");

        // Moving up slope along X/Y
        sendCommandExpectResult(ml, state, "G1X-12.7Y-12.7", "G1X-12.7Y-12.7Z-12.7");
        sendCommandExpectResult(ml, state, "G1X0Y0", "G1X0Y0Z0");
        sendCommandExpectResult(ml, state, "G1X12.7Y12.7", "G1X12.7Y12.7Z12.7");
        sendCommandExpectResult(ml, state, "G1X25.4Y25.4", "G1X25.4Y25.4Z25.4");
    }

    @Test
    public void testProbedPointsInMMWithRelativeGcodeInMM() {
        Position[][] grid = generateSlopeMesh(10, MM);
        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G21;
        state.currentPoint = new Position(  -10, -10, -10, MM);
        state.inAbsoluteMode = false;

        // We are not updating the state, so all start positions are from the "currentPoint"
        sendCommandExpectResult(ml, state, "G1Y10", "G1Y10Z0");
        sendCommandExpectResult(ml, state, "G1Y20", "G1Y20Z0");
        sendCommandExpectResult(ml, state, "G1X10Y10", "G1X10Y10Z10");
        sendCommandExpectResult(ml, state, "G1X20Y20", "G1X20Y20Z20");
        sendCommandExpectResult(ml, state, "G1X10", "G1X10Z10");
        sendCommandExpectResult(ml, state, "G1X20", "G1X20Z20");
    }

    @Test
    public void testProbedPointsInMMWithRelativeGcodeInInches() {
        Position[][] grid = generateSlopeMesh(25.4, MM);
        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G20;
        state.currentPoint = new Position(-1, -1, -1, INCH);
        state.inAbsoluteMode = false;

        // We are not updating the state, so all start positions are from the "currentPoint"
        sendCommandExpectResult(ml, state, "G1Y1", "G1Y1Z0");
        sendCommandExpectResult(ml, state, "G1Y2", "G1Y2Z0");
        sendCommandExpectResult(ml, state, "G1X1Y1", "G1X1Y1Z1");
        sendCommandExpectResult(ml, state, "G1X2Y2", "G1X2Y2Z2");
        sendCommandExpectResult(ml, state, "G1X1", "G1X1Z1");
        sendCommandExpectResult(ml, state, "G1X2", "G1X2Z2");
    }

    @Test
    public void testProbedPointsInInchesWithRelativeGcodeInMM() {
        Position[][] grid = generateSlopeMesh(1, INCH);
        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G21;
        state.currentPoint = new Position(-25.4, -25.4, 0, MM);
        state.inAbsoluteMode = false;

        // We are not updating the state, so all start positions are from the "currentPoint"
        sendCommandExpectResult(ml, state, "G1Y25.4", "G1Y25.4Z0");
        sendCommandExpectResult(ml, state, "G1Y50.8", "G1Y50.8Z0");
        sendCommandExpectResult(ml, state, "G1X25.4Y25.4", "G1X25.4Y25.4Z25.4");
        sendCommandExpectResult(ml, state, "G1X50.8Y50.8", "G1X50.8Y50.8Z50.8");
        sendCommandExpectResult(ml, state, "G1X25.4", "G1X25.4Z25.4");
        sendCommandExpectResult(ml, state, "G1X50.8", "G1X50.8Z50.8");
    }

    @Test
    public void testUnevenSurfaceMillimeters() {
        Position[][] grid = generateSlopeMesh(10, MM);

        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G21;
        state.currentPoint = new Position(-10, -10, 0, MM);
        state.inAbsoluteMode = true;

        // Moving along Y axis on flat line
        sendCommandExpectResult(ml, state, "G1Y-5", "G1Y-5Z-10");
        sendCommandExpectResult(ml, state, "G1Y0", "G1Y0Z-10");
        sendCommandExpectResult(ml, state, "G1Y5", "G1Y5Z-10");
        sendCommandExpectResult(ml, state, "G1Y10", "G1Y10Z-10");

        // Moving along X axis up slope
        sendCommandExpectResult(ml, state, "G1X-5", "G1X-5Z-5");
        sendCommandExpectResult(ml, state, "G1X0", "G1X0Z0");
        sendCommandExpectResult(ml, state, "G1X5", "G1X5Z5");
        sendCommandExpectResult(ml, state, "G1X10", "G1X10Z10");

        // Moving up slope along X/Y
        sendCommandExpectResult(ml, state, "G1X-5Y-5", "G1X-5Y-5Z-5");
        sendCommandExpectResult(ml, state, "G1X0Y0", "G1X0Y0Z0");
        sendCommandExpectResult(ml, state, "G1X5Y5", "G1X5Y5Z5");
        sendCommandExpectResult(ml, state, "G1X10Y10", "G1X10Y10Z10");
    }


    @Test
    public void testUnevenSurfaceInches() {
        Position[][] grid = {
                {new Position(-10, -10, -10, INCH), new Position(-10, 10, -10, INCH)},
                {new Position(10, -10, 10, INCH), new Position(10, 10, 10, INCH)}
        };

        MeshLeveler ml = new MeshLeveler(0.0, grid);

        GcodeState state = new GcodeState();
        state.units = Code.G20;
        state.currentPoint = new Position(-10, -10, 0, INCH);
        state.inAbsoluteMode = true;

        // Moving along Y axis on flat line
        sendCommandExpectResult(ml, state, "G1Y-5", "G1Y-5Z-10");
        sendCommandExpectResult(ml, state, "G1Y0", "G1Y0Z-10");
        sendCommandExpectResult(ml, state, "G1Y5", "G1Y5Z-10");
        sendCommandExpectResult(ml, state, "G1Y10", "G1Y10Z-10");

        // Moving along X axis up slope
        sendCommandExpectResult(ml, state, "G1X-5", "G1X-5Z-5");
        sendCommandExpectResult(ml, state, "G1X0", "G1X0Z0");
        sendCommandExpectResult(ml, state, "G1X5", "G1X5Z5");
        sendCommandExpectResult(ml, state, "G1X10", "G1X10Z10");

        // Moving up slope along X/Y
        sendCommandExpectResult(ml, state, "G1X-5Y-5", "G1X-5Y-5Z-5");
        sendCommandExpectResult(ml, state, "G1X0Y0", "G1X0Y0Z0");
        sendCommandExpectResult(ml, state, "G1X5Y5", "G1X5Y5Z5");
        sendCommandExpectResult(ml, state, "G1X10Y10", "G1X10Y10Z10");
    }
}
