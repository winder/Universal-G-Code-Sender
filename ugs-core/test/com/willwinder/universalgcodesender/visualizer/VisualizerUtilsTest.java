package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.PointSegment;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VisualizerUtilsTest {

    public static void assertPosition(double x, double y, double z, double a, double b, double c, Position position) {
        assertEquals("Expected position X", x, position.x, 0.1d);
        assertEquals("Expected position Y", y, position.y, 0.1d);
        assertEquals("Expected position Z", z, position.z, 0.1d);
        assertEquals("Expected position A", a, position.a, 0.1d);
        assertEquals("Expected position B", b, position.b, 0.1d);
        assertEquals("Expected position C", c, position.c, 0.1d);
    }

    @Test
    public void toCartesianShouldNotApplyRotationIfZero() {
        Position position = new Position(10, 11, 12, 0, 0, 0, UnitUtils.Units.MM);
        Position position1 = position.getCartesian();
        assertEquals(10d, position1.x, 0.1);
        assertEquals(11d, position1.y, 0.1);
        assertEquals(12d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldRotateAAroundX() {
        Position position = new Position(10, 10, 10, 180, 0, 0, UnitUtils.Units.MM);
        Position position1 = position.getCartesian();
        assertEquals(10d, position1.x, 0.1);
        assertEquals(-10d, position1.y, 0.1);
        assertEquals(-10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldDefaultUndefinedAxisToZeroWhenRotating() {
        // A wrapped 4-axis file (only X/Z/A) never defines Y, so Y is undefined (NaN). The rotation
        // around X should still be calculated, treating the missing Y as 0.
        Position position = new Position(10, Double.NaN, 10, 180, 0, 0, UnitUtils.Units.MM);

        Position position1 = position.getCartesian();

        assertEquals(10d, position1.x, 0.1);
        assertEquals(-0d, position1.y, 0.1);
        assertEquals(-10d, position1.z, 0.1);
    }

    @Test
    public void toCartesianShouldDefaultUndefinedAxisToZeroWithoutRotation() {
        Position position = new Position(10, Double.NaN, 10, 0, 0, 0, UnitUtils.Units.MM);

        Position position1 = position.getCartesian();

        assertEquals(10d, position1.x, 0.1);
        assertEquals(0d, position1.y, 0.1);
        assertEquals(10d, position1.z, 0.1);
    }

    @Test
    public void toCartesianShouldRotateBAroundY() {
        Position position = new Position(10, 10, 10, 0, 180, 0, UnitUtils.Units.MM);
        Position position1 = position.getCartesian();
        assertEquals(-10d, position1.x, 0.1);
        assertEquals(10d, position1.y, 0.1);
        assertEquals(-10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldRotateCAroundZ() {
        Position position = new Position(10, 10, 10, 0, 0, 180, UnitUtils.Units.MM);
        Position position1 = position.getCartesian();
        assertEquals(-10d, position1.x, 0.1);
        assertEquals(-10d, position1.y, 0.1);
        assertEquals(10d, position1.z, 0.1);
        assertEquals(0.0, position1.a, 0.1);
        assertEquals(0.0, position1.b, 0.1);
        assertEquals(0.0, position1.c, 0.1);
        assertEquals(UnitUtils.Units.MM, position1.getUnits());
    }

    @Test
    public void toCartesianShouldWrapPositiveRotationTowardPositiveAxis() {
        // A point at +Z rotated +90 degrees about A wraps toward +Y, matching how the post wraps
        // +Y into +A. A plain right-hand rotation would send it to -Y and mirror the model.
        Position position = new Position(0, 0, 10, 90, 0, 0, UnitUtils.Units.MM);

        Position position1 = position.getCartesian();

        assertEquals(0d, position1.x, 0.1);
        assertEquals(10d, position1.y, 0.1);
        assertEquals(0d, position1.z, 0.1);
    }

    @Test
    public void toCartesianShouldComposeMultipleAxisRotations() {
        // Rotating (0,1,0) by A=90 (+Y -> +Z) and then B=90 (+Z -> +X) must land on (1,0,0).
        // Each axis has to use the result of the previous rotation, not the original coordinates.
        Position position = new Position(0, 1, 0, 90, 90, 0, UnitUtils.Units.MM);

        Position position1 = position.getCartesian();

        assertEquals(1d, position1.x, 0.1);
        assertEquals(0d, position1.y, 0.1);
        assertEquals(0d, position1.z, 0.1);
    }

    @Test
    public void expandRotationalLineSegmentWithoutABCAxesShouldBeIgnored() {
        Position startPosition = new Position(0, 0, 10, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(1, result.size());
        assertPosition(0, 0, 10, Double.NaN, Double.NaN, Double.NaN, result.get(0).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentWithStartingAShouldBeIgnored() {
        Position startPosition = new Position(0, 0, 10, 0, Double.NaN, Double.NaN, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(1, result.size());
        assertPosition(0, 0, 10, Double.NaN, Double.NaN, Double.NaN, result.get(0).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentWithEndingAZeroShouldBeZero() {
        Position startPosition = new Position(0, 0, 10,  UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 0, Double.NaN, Double.NaN, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(1, result.size());
        assertPosition(0, 0, 10, 0.0, Double.NaN, Double.NaN, result.get(0).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentWithEndingAShouldBeInterpolated() {
        Position startPosition = new Position(0, 0, 10,  UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 10, Double.NaN, Double.NaN, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(3, result.size());
        assertPosition(0, 0, 10, 0, Double.NaN, Double.NaN, result.get(0).getEnd());
        assertPosition(0, 0, 10, 5.0, Double.NaN, Double.NaN, result.get(1).getEnd());
        assertPosition(0, 0, 10, 10.0, Double.NaN, Double.NaN, result.get(2).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundAShouldInterpolateWithFiveDegreeSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 90, 0, 0, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(19, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0, 0, 10, 5, 0, 0, result.get(1).getEnd());
        assertPosition(0, 0, 10, 10, 0, 0, result.get(2).getEnd());
        assertPosition(0, 0, 10, 90, 0, 0, result.get(18).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundAAndXShouldInterpolateWithFiveDegreeSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(10, 0, 10, 90, 0, 0, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(19, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0.5, 0, 10, 5, 0, 0, result.get(1).getEnd());
        assertPosition(1.1, 0, 10, 10, 0, 0, result.get(2).getEnd());
        assertPosition(10, 0, 10, 90, 0, 0, result.get(18).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundBShouldInterpolateWithFiveDegreeSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 0, 90, 0, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(19, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0, 0, 10, 0, 5, 0, result.get(1).getEnd());
        assertPosition(0, 0, 10, 0, 10, 0, result.get(2).getEnd());
        assertPosition(0, 0, 10, 0, 90, 0, result.get(18).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundBAndYShouldInterpolateWithFiveDegreeSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 10, 10, 0, 90, 0, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(19, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0, 0.5, 10, 0, 5, 0, result.get(1).getEnd());
        assertPosition(0, 1.1, 10, 0, 10, 0, result.get(2).getEnd());
        assertPosition(0, 10, 10, 0, 90, 0, result.get(18).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundCShouldInterpolateWithFiveDegreeSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 0, 0, 90, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(19, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0, 0, 10, 0, 0, 5, result.get(1).getEnd());
        assertPosition(0, 0, 10, 0, 0, 10, result.get(2).getEnd());
        assertPosition(0, 0, 10, 0, 0, 90, result.get(18).getEnd());
    }

    @Test
    public void expandRotationalLineSegmentRotationAroundMultipleAxisesShouldInterpolateThemWithUnifiedSteps() {
        Position startPosition = new Position(0, 0, 10, 0, 0, 0, UnitUtils.Units.MM);
        Position endPosition = new Position(0, 0, 10, 10, 20, 30, UnitUtils.Units.MM);
        PointSegment endSegment = new PointSegment(endPosition, 0);

        List<LineSegment> result = new ArrayList<>();
        VisualizerUtils.expandRotationalLineSegment(startPosition, endSegment, result);

        assertEquals(7, result.size());
        assertPosition(0, 0, 10, 0, 0, 0, result.get(0).getEnd());
        assertPosition(0, 0, 10, 1.6, 3.3, 5, result.get(1).getEnd());
        assertPosition(0, 0, 10, 3.3, 6.6, 10, result.get(2).getEnd());
        assertPosition(0, 0, 10, 5, 10, 15, result.get(3).getEnd());
        assertPosition(0, 0, 10, 6.6, 13.3, 20, result.get(4).getEnd());
        assertPosition(0, 0, 10, 8.3, 16.6, 25, result.get(5).getEnd());
        assertPosition(0, 0, 10, 10, 20, 30, result.get(6).getEnd());
    }
}
