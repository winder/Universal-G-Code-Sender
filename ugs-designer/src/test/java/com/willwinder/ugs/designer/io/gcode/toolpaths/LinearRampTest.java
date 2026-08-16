/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.io.gcode.toolpaths.LinearRamp.RampedPath;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LinearRampTest {
    /**
     * The distance the tool needs to move along the path to descend one millimeter with the ramp
     * angle used by {@link LinearRamp}
     */
    private static final double LENGTH_PER_MILLIMETER = 1d / Math.tan(Math.toRadians(15));

    @Test
    public void create_shouldDescendAlongTheBeginningOfAClosedPath() {
        List<PartialPosition> ring = ring();

        RampedPath ramp = LinearRamp.create(ring, null, 0, 1, 500).orElseThrow();

        // The descent starts where the tool path starts
        assertPosition(ramp.entry().orElseThrow(), 0, 0, -1);

        // Rapid down to the depth that the previous pass left
        assertEquals(2, ramp.segments().size());
        assertEquals(SegmentType.MOVE, ramp.segments().get(0).getType());
        assertEquals(0, ramp.segments().get(0).getPoint().getZ(), 0.001);
        assertFalse(ramp.segments().get(0).getPoint().hasX());

        // Descend to the full depth of cut along the first edge, in the direction the path is cut
        assertEquals(SegmentType.LINE, ramp.segments().get(1).getType());
        assertPosition(ramp.segments().get(1).getPoint(), 0, LENGTH_PER_MILLIMETER, -1);
        assertEquals(Integer.valueOf(500), ramp.segments().get(1).getFeedSpeed());
    }

    @Test
    public void create_shouldCutClosedPathFromTheEndOfTheRampAndBackOverIt() {
        List<PartialPosition> ring = ring();

        RampedPath ramp = LinearRamp.create(ring, null, 0, 1, 500).orElseThrow();

        // The cut continues where the ramp ended, goes around the path and ends by clearing out the
        // material that the ramp left behind
        List<PartialPosition> cutPath = ramp.cutPath();
        assertEquals(6, cutPath.size());
        assertPosition(cutPath.get(0), 0, LENGTH_PER_MILLIMETER, -1);
        assertPosition(cutPath.get(1), 0, 100, -1);
        assertPosition(cutPath.get(2), 100, 100, -1);
        assertPosition(cutPath.get(3), 100, 0, -1);
        assertPosition(cutPath.get(4), 0, 0, -1);
        assertPosition(cutPath.get(5), 0, LENGTH_PER_MILLIMETER, -1);
    }

    @Test
    public void create_shouldDescendFromTheDepthOfThePreviousPass() {
        List<PartialPosition> ring = ring();

        RampedPath ramp = LinearRamp.create(ring, null, 2, 3, 500).orElseThrow();

        assertEquals(-2, ramp.segments().get(0).getPoint().getZ(), 0.001);
        assertPosition(ramp.segments().get(1).getPoint(), 0, LENGTH_PER_MILLIMETER, -3);
    }

    @Test
    public void create_shouldDescendAroundTheCornersOfAClosedPath() {
        List<PartialPosition> ring = List.of(position(0, 0), position(1, 0), position(1, 100), position(0, 0));

        RampedPath ramp = LinearRamp.create(ring, null, 0, 1, 500).orElseThrow();

        // The descent turns with the path, descending at a constant rate along the way
        assertEquals(3, ramp.segments().size());
        assertPosition(ramp.segments().get(1).getPoint(), 1, 0, -1 / LENGTH_PER_MILLIMETER);
        assertPosition(ramp.segments().get(2).getPoint(), 1, LENGTH_PER_MILLIMETER - 1, -1);

        assertPosition(ramp.cutPath().get(0), 1, LENGTH_PER_MILLIMETER - 1, -1);
        assertPosition(ramp.cutPath().get(1), 1, 100, -1);
        assertPosition(ramp.cutPath().get(2), 0, 0, -1);
        assertPosition(ramp.cutPath().get(3), 1, 0, -1);
        assertPosition(ramp.cutPath().get(4), 1, LENGTH_PER_MILLIMETER - 1, -1);
    }

    @Test
    public void create_shouldDescendUpToTheStartOfALoopThatTheToolPathCarriesOnFrom() {
        List<PartialPosition> ring = ring();
        List<PartialPosition> withNextRing = new ArrayList<>(ring);
        withNextRing.addAll(List.of(position(20, 20), position(20, 80), position(80, 80)));

        RampedPath ramp = LinearRamp.create(withNextRing, null, 0, 1, 500).orElseThrow();

        // Descending leads up to where the loop starts, following the direction the loop is cut in
        assertPosition(ramp.entry().orElseThrow(), LENGTH_PER_MILLIMETER, 0, -1);
        assertPosition(ramp.segments().get(1).getPoint(), 0, 0, -1);

        // Which leaves the loop and the rings after it to be cut exactly as they were laid out
        assertPosition(ramp.cutPath().get(0), 0, 0, -1);
        assertPosition(ramp.cutPath().get(1), 0, 100, -1);
        assertPosition(ramp.cutPath().get(4), LENGTH_PER_MILLIMETER, 0, -1);
        assertPosition(ramp.cutPath().get(5), 0, 0, -1);
        assertPosition(ramp.cutPath().get(6), 20, 20, -1);
        assertEquals(9, ramp.cutPath().size());
    }

    @Test
    public void create_shouldDescendTowardsTheBeginningOfAnOpenPath() {
        List<PartialPosition> path = List.of(position(0, 0), position(1, 0), position(1, 100));

        RampedPath ramp = LinearRamp.create(path, null, 0, 1, 500).orElseThrow();

        // An open path can not be extended to clear out the ramp, so the tool starts a bit into the
        // path and descends back towards its beginning instead
        assertPosition(ramp.entry().orElseThrow(), 1, LENGTH_PER_MILLIMETER - 1, -1);
        assertEquals(3, ramp.segments().size());
        assertPosition(ramp.segments().get(1).getPoint(), 1, 0, -1 + 1 / LENGTH_PER_MILLIMETER);
        assertPosition(ramp.segments().get(2).getPoint(), 0, 0, -1);

        // The path is then cut in its entirety, clearing out the ramp as it goes
        assertEquals(path, ramp.cutPath());
    }

    @Test
    public void create_shouldUseTheWholePathWhenItIsTooShortToDescendAlong() {
        List<PartialPosition> ring = List.of(position(0, 0), position(0, 1), position(1, 1), position(0, 0));

        RampedPath ramp = LinearRamp.create(ring, null, 0, 1, 500).orElseThrow();

        // A path this short makes the ramp steeper than the ramp angle instead of moving outside it,
        // descending along the whole path and then cutting all of it at the full depth of cut
        assertEquals(4, ramp.segments().size());
        assertPosition(ramp.segments().get(3).getPoint(), 0, 0, -1);
        assertEquals(4, ramp.cutPath().size());
        assertPosition(ramp.cutPath().get(0), 0, 0, -1);
        assertPosition(ramp.cutPath().get(3), 0, 0, -1);
    }

    @Test
    public void create_shouldDescendFromWhereTheToolIsStandingOnTheClosedPath() {
        List<PartialPosition> ring = ring();
        PartialPosition tool = new PartialPosition(0d, 10d, 0d, UnitUtils.Units.MM);

        RampedPath ramp = LinearRamp.create(ring, tool, 0, 1, 500).orElseThrow();

        // The tool is standing on the path already, so it can start descending without being moved
        assertTrue(ramp.entry().isEmpty());

        // Descending starts from the depth the tool is standing at and continues along the path
        assertEquals(1, ramp.segments().size());
        assertPosition(ramp.segments().get(0).getPoint(), 0, 10 + LENGTH_PER_MILLIMETER, -1);

        // And the path is turned to be cut from there and back over the ramp again
        assertEquals(7, ramp.cutPath().size());
        assertPosition(ramp.cutPath().get(0), 0, 10 + LENGTH_PER_MILLIMETER, -1);
        assertPosition(ramp.cutPath().get(1), 0, 100, -1);
        assertPosition(ramp.cutPath().get(4), 0, 0, -1);
        assertPosition(ramp.cutPath().get(5), 0, 10, -1);
        assertPosition(ramp.cutPath().get(6), 0, 10 + LENGTH_PER_MILLIMETER, -1);
    }

    @Test
    public void create_shouldDescendFromWhereTheToolIsStandingOnACornerOfTheClosedPath() {
        List<PartialPosition> ring = ring();
        PartialPosition tool = new PartialPosition(100d, 100d, 0d, UnitUtils.Units.MM);

        RampedPath ramp = LinearRamp.create(ring, tool, 0, 1, 500).orElseThrow();

        assertTrue(ramp.entry().isEmpty());
        assertPosition(ramp.segments().get(0).getPoint(), 100, 100 - LENGTH_PER_MILLIMETER, -1);
        assertPosition(ramp.cutPath().get(0), 100, 100 - LENGTH_PER_MILLIMETER, -1);
        assertPosition(ramp.cutPath().get(ramp.cutPath().size() - 1), 100, 100 - LENGTH_PER_MILLIMETER, -1);
    }

    @Test
    public void create_shouldMoveToThePathWhenTheToolIsStandingSomewhereElse() {
        List<PartialPosition> ring = ring();
        PartialPosition tool = new PartialPosition(50d, 50d, 0d, UnitUtils.Units.MM);

        RampedPath ramp = LinearRamp.create(ring, tool, 0, 1, 500).orElseThrow();

        // The tool is not on the path, so it has to be moved to the start of it and rapid back down
        assertPosition(ramp.entry().orElseThrow(), 0, 0, -1);
        assertEquals(SegmentType.MOVE, ramp.segments().get(0).getType());
        assertEquals(0, ramp.segments().get(0).getPoint().getZ(), 0.001);
    }

    @Test
    public void create_shouldNotRampWhenAlreadyAtTheTargetDepth() {
        Optional<RampedPath> ramp = LinearRamp.create(ring(), null, 1, 1, 500);

        assertTrue(ramp.isEmpty());
    }

    @Test
    public void create_shouldNotRampWhenThePathHasNoLength() {
        Optional<RampedPath> ramp = LinearRamp.create(List.of(position(0, 0), position(0, 0)), null, 0, 1, 500);

        assertTrue(ramp.isEmpty());
    }

    @Test
    public void create_shouldNotRampWhenThereIsNoPathToRampAlong() {
        Optional<RampedPath> ramp = LinearRamp.create(Collections.singletonList(position(0, 0)), null, 0, 1, 500);

        assertTrue(ramp.isEmpty());
    }

    private static List<PartialPosition> ring() {
        return List.of(position(0, 0), position(0, 100), position(100, 100), position(100, 0), position(0, 0));
    }

    private static void assertPosition(PartialPosition position, double x, double y, double z) {
        assertEquals(x, position.getAxis(Axis.X), 0.001);
        assertEquals(y, position.getAxis(Axis.Y), 0.001);
        assertEquals(z, position.getAxis(Axis.Z), 0.001);
    }

    private static PartialPosition position(double x, double y) {
        return new PartialPosition(x, y, -1d, UnitUtils.Units.MM);
    }
}
