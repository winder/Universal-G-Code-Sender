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
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author wwinder
 */
public class MeshLevelerTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    private static final Position[][] BIG_FLAT_GRID_Z0 = {
        {new Position(0,0,0,MM), new Position(0, 10, 0, MM)},
        {new Position(10,0,0,MM), new Position(10, 10, 0, MM)}
    };
    private static final Position[][] BIG_FLAT_GRID_Z1 = {
        {new Position(0,0,1,MM), new Position(0, 10, 1,MM)},
        {new Position(10,0,1,MM), new Position(10, 10, 1,MM)}
    };


    private static void sendCommandExpectResult(MeshLeveler ml, GcodeState state, String command, String result) {
        try {
            List<String> expected = Collections.singletonList(result);
            List<String> results = ml.processCommand(command, state);
            Assert.assertEquals(expected, results);
        } catch (GcodeParserException ex) {
            Assert.fail("Unexpected exception.");
        }
    }

    @Test
    public void testNotEnoughPoints() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_NOT_ENOUGH_SAMPLES);

        Position[][] grid = {
            {new Position(MM)},
            {new Position(MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testNonRectangularGrid() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_MESH_SHAPE);

        Position[][] grid = {
            {new Position(MM), new Position(MM)},
            {new Position(MM), new Position(MM), new Position(MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testXAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ALIGNMENT);

        Position[][] grid = {
            {new Position(0,0,0, MM), new Position(1, 0, 0, MM)},
            {new Position(10,1,0, MM), new Position(1, 1, 0, MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testYAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ALIGNMENT);

        Position[][] grid = {
            {new Position(0,0,0, MM), new Position(0, 0, 0, MM)},
            {new Position(1,0,0, MM), new Position(1, 1, 0, MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testXAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ASCENTION);

        Position[][] grid = {
            {new Position(10,0,0, MM), new Position(10, 10, 0, MM)},
            {new Position(0,0,0, MM), new Position(0, 10, 0, MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testYAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ASCENTION);

        Position[][] grid = {
            {new Position(0,10,0, MM), new Position(0, 0, 0, MM)},
            {new Position(10,0,0, MM), new Position(10, 10, 0, MM)}
        };

        new MeshLeveler(0.0, grid, Units.MM);
    }

    @Test
    public void testUnexpectedArc() throws GcodeParserException {
        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_UNEXPECTED_ARC);

        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        ml.processCommand("G2X1", state);
    }

    @Test
    public void testMultipleCommandsWithLine() throws GcodeParserException {
        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(Localization.getString("parser.processor.general.multiple-commands"));

        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        ml.processCommand("G1X1G20", state);
    }

    @Test
    public void testNoZChangesWithFlatMeshOnSurface() throws GcodeParserException {
        MeshLeveler ml = new MeshLeveler(0.0, BIG_FLAT_GRID_Z0, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Y0Z0");
    }

    @Test
    public void testFlatMeshOnSurfaceOffSurface() throws GcodeParserException {
        MeshLeveler ml = new MeshLeveler(1.0, BIG_FLAT_GRID_Z1, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Y0Z0");
    }

    @Test
    public void testNegativeOffset() throws GcodeParserException {
        // The probe will be at 1.0 instead of 0.9 which means the end point needs to be raised 0.1
        MeshLeveler ml = new MeshLeveler(0.9, BIG_FLAT_GRID_Z1, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        sendCommandExpectResult(ml, state, "G1X5", "G1X5Y0Z0.1");
    }

    @Test
    public void testUnevenSurface() throws GcodeParserException {
        /*
                   10
      z=-10 *         |         * z=10
                      |
                      |
        -10 --------------------- 10
                      |
                      |
      z=-10 *         |         * z=10
                   -10
        */
        Position[][] grid = {
            {new Position(-10,-10,-10, MM), new Position(-10, 10, -10, MM)},
            {new Position(10,-10,10, MM), new Position(10, 10, 10, MM)}
        };

        MeshLeveler ml = new MeshLeveler(0.0, grid, Units.MM);

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(-10, -10, 0, MM);
        state.inAbsoluteMode = true;

        // Moving along Y axis on flat line
        sendCommandExpectResult(ml, state, "G1Y-5", "G1X-10Y-5Z-10");
        sendCommandExpectResult(ml, state, "G1Y0", "G1X-10Y0Z-10");
        sendCommandExpectResult(ml, state, "G1Y5", "G1X-10Y5Z-10");
        sendCommandExpectResult(ml, state, "G1Y10", "G1X-10Y10Z-10");

        // Moving along X axis up slope
        sendCommandExpectResult(ml, state, "G1X-5", "G1X-5Y-10Z-5");
        sendCommandExpectResult(ml, state, "G1X0", "G1X0Y-10Z0");
        sendCommandExpectResult(ml, state, "G1X5", "G1X5Y-10Z5");
        sendCommandExpectResult(ml, state, "G1X10", "G1X10Y-10Z10");

        // Moving up slope along X/Y
        sendCommandExpectResult(ml, state, "G1X-5Y-5", "G1X-5Y-5Z-5");
        sendCommandExpectResult(ml, state, "G1X0Y0", "G1X0Y0Z0");
        sendCommandExpectResult(ml, state, "G1X5Y5", "G1X5Y5Z5");
        sendCommandExpectResult(ml, state, "G1X10Y10", "G1X10Y10Z10");
    }
}
