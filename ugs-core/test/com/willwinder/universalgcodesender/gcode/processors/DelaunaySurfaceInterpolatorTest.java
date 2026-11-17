/*
    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.junit.Test;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DelaunaySurfaceInterpolatorTest {
    @Test
    public void segmentInsideOneTriangleIsNotSplit() {
        DelaunaySurfaceInterpolator surface = triangle();

        assertEquals(List.of(), surface.crossingParameters(1, 1, 4, 2));
        assertEquals(List.of(), surface.crossingParameters(0, 0, 10, 0));
    }

    @Test
    public void segmentMissesTheSurface() {
        assertEquals(List.of(), triangle().crossingParameters(-5, -5, -1, -2));
    }

    @Test
    public void segmentCrossingOneTriangleSplitsOnEntryAndExit() {
        List<Double> crossings = triangle().crossingParameters(-5, 1, 15, 1);

        assertEquals(2, crossings.size());
        assertEquals(0.25, crossings.get(0), 1e-9);
        assertEquals(0.7, crossings.get(1), 1e-9);
    }

    @Test
    public void segmentAlongTheBoundarySplitsWhereItMeetsTheEdge() {
        List<Double> crossings = triangle().crossingParameters(-5, 0, 15, 0);

        assertEquals(2, crossings.size());
        assertEquals(0.25, crossings.get(0), 1e-9);
        assertEquals(0.75, crossings.get(1), 1e-9);
    }

    @Test
    public void squareGridCrossesTheSharedDiagonalOnce() {
        DelaunaySurfaceInterpolator surface = new DelaunaySurfaceInterpolator(Arrays.asList(
                new Position(0, 0, 0, Units.MM),
                new Position(0, 10, 1, Units.MM),
                new Position(10, 0, 2, Units.MM),
                new Position(10, 10, 4, Units.MM)));

        List<Double> crossings = surface.crossingParameters(0, 5, 10, 5);

        assertEquals(1, crossings.size());
        assertEquals(0.5, crossings.get(0), 1e-9);
        double diagonalHeight = surface.surfaceHeightAt(5, 5);
        assertTrue(Math.abs(diagonalHeight - 2d) < 1e-6 || Math.abs(diagonalHeight - 1.5d) < 1e-6);
    }

    @Test
    public void gridCrossingsMatchEdgeIntersectionsAndStayLinearBetweenThem() {
        List<Position> points = new ArrayList<>();
        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                points.add(new Position(x * 10, y * 10, 0.3 * x + 0.1 * y * y, Units.MM));
            }
        }
        DelaunaySurfaceInterpolator surface = new DelaunaySurfaceInterpolator(points);
        double[][] lines = {
                {0, 0, 40, 40},
                {-10, 5, 50, 5},
                {5, -10, 5, 50},
                {0, 25, 40, 15},
                {1, 1, 9, 2},
                {0, 0, 40, 0},
                {-5, -5, -1, 20},
                {0, 15, 40, 25},
                {-10, 20, 50, 20}
        };
        for (double[] line : lines) {
            List<Double> crossings = surface.crossingParameters(line[0], line[1], line[2], line[3]);
            assertSameCrossings(surface, line, crossings);
            assertLinearBetweenCrossings(surface, line, crossings);
        }
    }

    private static void assertSameCrossings(DelaunaySurfaceInterpolator surface, double[] line, List<Double> actual) {
        List<Double> expected = edgeCrossings(surface, line[0], line[1], line[2], line[3]);
        assertEquals(line[0] + "," + line[1] + " -> " + line[2] + "," + line[3]
                        + " expected " + expected + " but was " + actual,
                expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), 1e-6);
        }
    }

    private static void assertLinearBetweenCrossings(DelaunaySurfaceInterpolator surface, double[] line, List<Double> crossings) {
        List<Double> samples = new ArrayList<>();
        samples.add(0d);
        samples.addAll(crossings);
        samples.add(1d);
        for (int i = 0; i < samples.size() - 1; i++) {
            double start = samples.get(i);
            double end = samples.get(i + 1);
            double left = start + 0.2 * (end - start);
            double right = start + 0.8 * (end - start);
            double leftHeight = heightAt(surface, line, left);
            double rightHeight = heightAt(surface, line, right);
            for (double fraction : new double[]{0.35, 0.5, 0.65}) {
                double t = start + fraction * (end - start);
                double interpolated = leftHeight + (rightHeight - leftHeight) * (t - left) / (right - left);
                assertEquals("surface is not linear between " + start + " and " + end, interpolated, heightAt(surface, line, t), 1e-5);
            }
        }
    }

    /**
     * Independent of the walk: every triangle edge the segment touches in its interior.
     * A collinear overlap contributes its endpoints, which is where the surface region changes.
     */
    private static List<Double> edgeCrossings(DelaunaySurfaceInterpolator surface, double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double span = Math.hypot(dx, dy);
        double tEps = span == 0d ? 1d : 1e-6 / span;
        List<Double> crossings = new ArrayList<>();
        RobustLineIntersector intersector = new RobustLineIntersector();
        for (Position[] triangle : surface.getTriangles()) {
            for (int edge = 0; edge < 3; edge++) {
                Position a = triangle[edge];
                Position b = triangle[(edge + 1) % 3];
                intersector.computeIntersection(
                        new Coordinate(x0, y0), new Coordinate(x1, y1),
                        new Coordinate(a.getX(), a.getY()), new Coordinate(b.getX(), b.getY()));
                if (!intersector.hasIntersection()) {
                    continue;
                }
                for (int i = 0; i < intersector.getIntersectionNum(); i++) {
                    Coordinate hit = intersector.getIntersection(i);
                    double t = span == 0d ? 0d : ((hit.x - x0) * dx + (hit.y - y0) * dy) / (span * span);
                    addUnique(crossings, t, tEps);
                }
            }
        }
        crossings.sort(Double::compare);
        return crossings;
    }

    private static void addUnique(List<Double> crossings, double t, double tEps) {
        if (t <= tEps || t >= 1d - tEps) {
            return;
        }
        for (double existing : crossings) {
            if (Math.abs(existing - t) <= tEps) {
                return;
            }
        }
        crossings.add(t);
    }

    private static double heightAt(DelaunaySurfaceInterpolator surface, double[] line, double t) {
        double x = line[0] + t * (line[2] - line[0]);
        double y = line[1] + t * (line[3] - line[1]);
        return surface.surfaceHeightAt(x, y);
    }

    private static DelaunaySurfaceInterpolator triangle() {
        return new DelaunaySurfaceInterpolator(Arrays.asList(
                new Position(0, 0, 1, Units.MM),
                new Position(10, 0, 1, Units.MM),
                new Position(0, 10, 3, Units.MM)));
    }
}
