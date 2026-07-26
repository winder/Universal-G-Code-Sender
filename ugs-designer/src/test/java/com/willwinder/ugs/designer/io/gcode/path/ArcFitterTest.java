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
package com.willwinder.ugs.designer.io.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ArcFitterTest {

    private static final double TOLERANCE = 0.01;
    private static final double FLATNESS = 0.1;

    @Test
    public void constructor_shouldFailForToleranceThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new ArcFitter(0, FLATNESS));
        assertThrows(IllegalArgumentException.class, () -> new ArcFitter(-0.1, FLATNESS));
    }

    @Test
    public void constructor_shouldFailForFlatnessPrecisionThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new ArcFitter(TOLERANCE, 0));
        assertThrows(IllegalArgumentException.class, () -> new ArcFitter(TOLERANCE, -0.1));
    }

    @Test
    public void fit_shouldCreateLinesForPointsSpacedTooFarApartForTheirRadius() {
        List<PartialPosition> points = arcPoints(0, 0, 200, 0, 0.4, 5);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(4, segments.size());
        assertTrue(segments.stream().allMatch(s -> s.getType() == SegmentType.LINE));
    }

    @Test
    public void fit_shouldCreateCounterClockwiseArcForCounterClockwisePoints() {
        List<PartialPosition> points = arcPoints(0, 0, 10, 0, Math.PI / 2, 20);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(1, segments.size());
        assertEquals(SegmentType.CCWARC, segments.get(0).getType());
        assertEquals(0d, segments.get(0).getArcCenter().getX(), 1e-6);
        assertEquals(0d, segments.get(0).getArcCenter().getY(), 1e-6);
    }

    @Test
    public void fit_shouldCreateClockwiseArcForClockwisePoints() {
        List<PartialPosition> points = arcPoints(0, 0, 10, 0, -Math.PI / 2, 20);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(1, segments.size());
        assertEquals(SegmentType.CWARC, segments.get(0).getType());
    }

    @Test
    public void fit_shouldCreateArcEndingAtTheLastPoint() {
        List<PartialPosition> points = arcPoints(5, 5, 10, 0, Math.PI / 2, 20);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        PartialPosition end = segments.get(segments.size() - 1).getPoint();
        assertEquals(points.get(points.size() - 1).getX(), end.getX(), 1e-6);
        assertEquals(points.get(points.size() - 1).getY(), end.getY(), 1e-6);
    }

    @Test
    public void fit_shouldCreateLinesForStraightPoints() {
        List<PartialPosition> points = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            points.add(position(i, i * 2));
        }

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(19, segments.size());
        assertTrue(segments.stream().allMatch(s -> s.getType() == SegmentType.LINE));
    }

    @Test
    public void fit_shouldCreateLinesForPointsThatDoNotFollowACircle() {
        List<PartialPosition> points = List.of(
                position(0, 0), position(1, 1), position(2, 0),
                position(3, 1), position(4, 0), position(5, 1));

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(5, segments.size());
        assertTrue(segments.stream().allMatch(s -> s.getType() == SegmentType.LINE));
    }

    @Test
    public void fit_shouldCreateLinesForFewerPointsThanNeededForAnArc() {
        List<PartialPosition> points = arcPoints(0, 0, 10, 0, Math.PI / 2, 4);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(3, segments.size());
        assertTrue(segments.stream().allMatch(s -> s.getType() == SegmentType.LINE));
    }

    @Test
    public void fit_shouldCreateLinesForPointsOnCircleAtChangingDepths() {
        List<PartialPosition> points = new ArrayList<>();
        List<PartialPosition> circle = arcPoints(0, 0, 10, 0, Math.PI / 2, 20);
        for (int i = 0; i < circle.size(); i++) {
            points.add(PartialPosition.builder(circle.get(i)).setZ(-i * 0.1).build());
        }

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(19, segments.size());
        assertTrue(segments.stream().allMatch(s -> s.getType() == SegmentType.LINE));
    }

    @Test
    public void fit_shouldCreateLinesForPointsDeviatingMoreThanTheTolerance() {
        List<PartialPosition> points = new ArrayList<>(arcPoints(0, 0, 10, 0, Math.PI / 2, 20));
        points.set(2, position(points.get(2).getX() + 1, points.get(2).getY()));

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(SegmentType.LINE, segments.get(0).getType());
    }

    @Test
    public void fit_shouldCreateSeparateArcsForPointsChangingDirection() {
        List<PartialPosition> points = new ArrayList<>(arcPoints(0, 0, 10, 0, Math.PI / 2, 10));
        List<PartialPosition> back = arcPoints(0, 0, 10, Math.PI / 2, -Math.PI / 2, 10);
        points.addAll(back.subList(1, back.size()));

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(2, segments.size());
        assertEquals(SegmentType.CCWARC, segments.get(0).getType());
        assertEquals(SegmentType.CWARC, segments.get(1).getType());
    }

    @Test
    public void fit_shouldSplitClosedCircleIntoArcsThatEndWhereTheyStarted() {
        List<PartialPosition> points = arcPoints(0, 0, 10, 0, 2 * Math.PI, 33);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertTrue(segments.size() > 1);
        assertTrue(segments.stream().allMatch(s -> s.getType().isArc()));
        segments.forEach(s -> {
            assertEquals(0d, s.getArcCenter().getX(), 1e-6);
            assertEquals(0d, s.getArcCenter().getY(), 1e-6);
        });
    }

    @Test
    public void fit_shouldNotCreateArcSweepingMoreThanHalfACircle() {
        // A controller reads an arc ending where it started as a full circle, so arcs are kept
        // well short of that to stay unambiguous once the coordinates have been rounded
        List<PartialPosition> points = arcPoints(0, 0, 10, 0, 2 * Math.PI, 129);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        PartialPosition start = points.get(0);
        for (Segment segment : segments) {
            assertTrue("An arc swept more than half a circle", sweepOf(start, segment) <= Math.PI + 1e-6);
            start = segment.getPoint();
        }
    }

    private static double sweepOf(PartialPosition start, Segment arc) {
        double startAngle = Math.atan2(start.getY() - arc.getArcCenter().getY(), start.getX() - arc.getArcCenter().getX());
        double endAngle = Math.atan2(arc.getPoint().getY() - arc.getArcCenter().getY(), arc.getPoint().getX() - arc.getArcCenter().getX());
        double sweep = arc.getType() == SegmentType.CCWARC ? endAngle - startAngle : startAngle - endAngle;
        return sweep < 0 ? sweep + (2 * Math.PI) : sweep;
    }

    @Test
    public void fit_shouldCreateArcWithAccurateCenterForCircleFarFromOrigin() {
        List<PartialPosition> points = arcPoints(1_200.5, -843.25, 60, 1.1, Math.PI / 2, 30);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(1, segments.size());
        assertEquals(1_200.5, segments.get(0).getArcCenter().getX(), 1e-6);
        assertEquals(-843.25, segments.get(0).getArcCenter().getY(), 1e-6);
    }

    @Test
    public void fit_shouldCreateLineFollowedByArc() {
        List<PartialPosition> points = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            points.add(position(-20 + i, 0));
        }
        points.addAll(arcPoints(0, 0, 10, Math.PI, -Math.PI / 2, 20));

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertTrue(segments.stream().anyMatch(s -> s.getType() == SegmentType.LINE));
        assertEquals(SegmentType.CWARC, segments.get(segments.size() - 1).getType());
    }

    @Test
    public void fit_shouldKeepTheFeedRateOnEverySegment() {
        List<PartialPosition> points = new ArrayList<>(arcPoints(0, 0, 10, 0, Math.PI / 2, 20));
        points.add(position(0, 30));

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_234);

        assertTrue(segments.stream().allMatch(s -> Integer.valueOf(1_234).equals(s.getFeedSpeed())));
    }

    @Test
    public void fit_shouldKeepArcWithinToleranceOfTheOriginalPoints() {
        List<PartialPosition> points = arcPoints(3, -7, 25, 0.4, Math.PI / 3, 40);

        List<Segment> segments = new ArcFitter(TOLERANCE, FLATNESS).fit(points, 1_000);

        assertEquals(1, segments.size());
        double radius = segments.get(0).getArcCenter().distance(points.get(0).getX(), points.get(0).getY());
        for (PartialPosition point : points) {
            double distance = segments.get(0).getArcCenter().distance(point.getX(), point.getY());
            assertEquals(radius, distance, TOLERANCE);
        }
    }

    private static List<PartialPosition> arcPoints(double centerX, double centerY, double radius, double startAngle, double sweep, int count) {
        List<PartialPosition> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double angle = startAngle + ((sweep * i) / (count - 1));
            points.add(position(centerX + (radius * Math.cos(angle)), centerY + (radius * Math.sin(angle))));
        }

        return points;
    }

    private static PartialPosition position(double x, double y) {
        return PartialPosition.builder(UnitUtils.Units.MM).setX(x).setY(y).setZ(-1d).build();
    }
}
