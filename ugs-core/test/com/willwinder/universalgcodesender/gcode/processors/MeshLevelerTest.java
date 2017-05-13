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
import java.util.Collections;
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
public class MeshLevelerTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    private static Position[][] bigFlatGridZ0 = {
        {new Position(0,0,0,Units.MM), new Position(0, 10, 0,Units.MM)},
        {new Position(10,0,0,Units.MM), new Position(10, 10, 0,Units.MM)}
    };
    private static Position[][] bigFlatGridZ1 = {
        {new Position(0,0,1,Units.MM), new Position(0, 10, 1,Units.MM)},
        {new Position(10,0,1,Units.MM), new Position(10, 10, 1,Units.MM)}
    };

    @Test
    public void testNotEnoughPoints() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_NOT_ENOUGH_SAMPLES);

        Position[][] grid = {
            {new Position()},
            {new Position()}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testNonRectangularGrid() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_MESH_SHAPE);

        Position[][] grid = {
            {new Position(), new Position()},
            {new Position(), new Position(), new Position()}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testXAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ALIGNMENT);

        Position[][] grid = {
            {new Position(0,0,0,Units.MM), new Position(0, 10, 0,Units.MM)},
            {new Position(10,1,0,Units.MM), new Position(10, 10, 0,Units.MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testYAlignment() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ALIGNMENT);

        Position[][] grid = {
            {new Position(0,0,0,Units.MM), new Position(1, 10, 0,Units.MM)},
            {new Position(10,0,0,Units.MM), new Position(10, 10, 0,Units.MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testXAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_X_ASCENTION);

        Position[][] grid = {
            {new Position(10,0,0,Units.MM), new Position(10, 10, 0,Units.MM)},
            {new Position(0,0,0,Units.MM), new Position(0, 10, 0,Units.MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testYAscention() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_Y_ASCENTION);

        Position[][] grid = {
            {new Position(0,10,0,Units.MM), new Position(0, 0, 0,Units.MM)},
            {new Position(10,0,0,Units.MM), new Position(10, 10, 0,Units.MM)}
        };

        new MeshLeveler(0.0, grid);
    }

    @Test
    public void testUnexpectedArc() throws GcodeParserException {
        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(MeshLeveler.ERROR_UNEXPECTED_ARC);

        MeshLeveler ml = new MeshLeveler(0.0, bigFlatGridZ0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0, 0, 0);
        state.inAbsoluteMode = true;

        ml.processCommand("G2X1", state);
    }

    @Test
    public void testMultipleCommandsWithLine() throws GcodeParserException {
        expectedEx.expect(GcodeParserException.class);
        expectedEx.expectMessage(Localization.getString("parser.processor.general.multiple-commands"));

        MeshLeveler ml = new MeshLeveler(0.0, bigFlatGridZ0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0, 0, 0);
        state.inAbsoluteMode = true;

        ml.processCommand("G1X1G92", state);
    }

    @Test
    public void testNoZChangesWithFlatMeshOnSurface() throws GcodeParserException {
        MeshLeveler ml = new MeshLeveler(0.0, bigFlatGridZ0);

        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0, 0, 0);
        state.inAbsoluteMode = true;

        List<String> expected = Collections.singletonList("G1X5Y0Z0");
        List<String> results = ml.processCommand("G1X5", state);

        Assert.assertEquals(expected, results);
    }

    @Test
    public void testFlatMeshOnSurfaceOffSurface() throws GcodeParserException {
        MeshLeveler ml = new MeshLeveler(1.0, bigFlatGridZ1);

        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0, 0, 0);
        state.inAbsoluteMode = true;

        List<String> expected = Collections.singletonList("G1X5Y0Z0");
        List<String> results = ml.processCommand("G1X5", state);

        Assert.assertEquals(expected, results);
    }

    @Test
    public void testNegativeOffset() throws GcodeParserException {
        // The probe will be at 1.0 which means the end point needs to be lowered 0.1
        MeshLeveler ml = new MeshLeveler(0.9, bigFlatGridZ1);

        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0, 0, 0);
        state.inAbsoluteMode = true;

        List<String> expected = Collections.singletonList("G1X5Y0Z-0.1");
        List<String> results = ml.processCommand("G1X5", state);

        Assert.assertEquals(expected, results);
    }
}
