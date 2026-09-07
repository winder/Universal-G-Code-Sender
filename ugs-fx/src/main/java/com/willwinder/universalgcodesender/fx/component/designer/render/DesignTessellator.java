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
package com.willwinder.universalgcodesender.fx.component.designer.render;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneMeshes;
import javafx.scene.paint.Color;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.triangulate.polygon.PolygonTriangulator;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Turns the designer's {@link Shape}s into vertex arrays: outlines as line lists and fills as
 * triangles on the work plane. Everything is flattened to {@link #FLATNESS} millimeters, which
 * is far below what a pixel covers at any zoom the visualizer allows.
 */
public final class DesignTessellator {
    public static final double FLATNESS = 0.1;
    private static final Logger LOGGER = Logger.getLogger(DesignTessellator.class.getName());
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final double MIN_RING_AREA = 1e-6;
    private static final double COINCIDENT = 1e-6;

    private DesignTessellator() {
    }

    /**
     * Every edge of the flattened shape as a line segment.
     */
    public static float[] outline(Shape shape, Color color) {
        LineMeshBuilder builder = new LineMeshBuilder();
        for (List<Point2D> polyline : polylines(shape)) {
            for (int i = 1; i < polyline.size(); i++) {
                Point2D a = polyline.get(i - 1);
                Point2D b = polyline.get(i);
                builder.add(a.getX(), a.getY(), 0, b.getX(), b.getY(), 0, color);
            }
        }
        return builder.build();
    }
    
    /**
     * Two triangles covering the rectangle, with texture coordinates placing the image's top
     * row at the rectangle's top edge (its maximum Y, as Y grows upwards in the design).
     */
    public static float[] texturedQuad(Rectangle2D bounds) {
        float x1 = (float) bounds.getMinX();
        float y1 = (float) bounds.getMinY();
        float x2 = (float) bounds.getMaxX();
        float y2 = (float) bounds.getMaxY();
        return new float[]{
                x1, y1, 0, 0, 1,
                x2, y1, 0, 1, 1,
                x2, y2, 0, 1, 0,
                x1, y1, 0, 0, 1,
                x2, y2, 0, 1, 0,
                x1, y2, 0, 0, 0,
        };
    }

    /**
     * The outline as dashes of equal on and off length, running continuously around corners.
     */
    public static float[] dashedOutline(Shape shape, Color color, double dashLength) {
        LineMeshBuilder builder = new LineMeshBuilder();
        for (List<Point2D> polyline : polylines(shape)) {
            double phase = 0;
            for (int i = 1; i < polyline.size(); i++) {
                phase = dashSegment(builder, polyline.get(i - 1), polyline.get(i), dashLength, phase, color);
            }
        }
        return builder.build();
    }

    /**
     * The area enclosed by the shape as triangles, using the even-odd rule so that nested
     * rings become holes. Self intersecting outlines are repaired first.
     */
    public static float[] fill(Shape shape) {
        Geometry filled = null;
        for (Coordinate[] ring : closedRings(shape)) {
            Geometry polygon = toValidPolygon(ring);
            if (polygon == null || polygon.isEmpty()) {
                continue;
            }
            filled = filled == null ? polygon : filled.symDifference(polygon);
        }
        if (filled == null || filled.isEmpty()) {
            return new float[0];
        }

        List<Coordinate[]> triangles = new ArrayList<>();
        for (int i = 0; i < filled.getNumGeometries(); i++) {
            if (filled.getGeometryN(i) instanceof Polygon polygon) {
                collectTriangles(polygon, triangles);
            }
        }
        float[] vertices = new float[triangles.size() * 3 * SceneMeshes.FLOATS_PER_VERTEX];
        int offset = 0;
        for (Coordinate[] triangle : triangles) {
            for (int corner = 0; corner < 3; corner++) {
                vertices[offset++] = (float) triangle[corner].x;
                vertices[offset++] = (float) triangle[corner].y;
                vertices[offset++] = 0;
                vertices[offset++] = 0;
                vertices[offset++] = 0;
                vertices[offset++] = 1;
            }
        }
        return vertices;
    }

    private static void collectTriangles(Polygon polygon, List<Coordinate[]> triangles) {
        Geometry triangulated;
        try {
            triangulated = PolygonTriangulator.triangulate(polygon);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Could not triangulate a design shape", e);
            return;
        }
        for (int i = 0; i < triangulated.getNumGeometries(); i++) {
            Coordinate[] coordinates = triangulated.getGeometryN(i).getCoordinates();
            if (coordinates.length >= 3) {
                triangles.add(new Coordinate[]{coordinates[0], coordinates[1], coordinates[2]});
            }
        }
    }

    /**
     * Builds a polygon from a ring, repairing it into valid geometry when the raw outline self
     * intersects. The result may be a multi polygon for a figure of eight outline.
     */
    private static Geometry toValidPolygon(Coordinate[] ring) {
        Geometry polygon = GEOMETRY_FACTORY.createPolygon(ring);
        return polygon.isValid() ? polygon : GeometryFixer.fix(polygon);
    }

    /**
     * The closed sub paths of the shape as rings with the first coordinate repeated last, as
     * JTS wants them. A sub path counts as closed when it ends with a close segment or when it
     * returns to its starting point, which is how imported and traced paths usually close.
     * Degenerate rings are dropped.
     */
    private static List<Coordinate[]> closedRings(Shape shape) {
        List<Coordinate[]> rings = new ArrayList<>();
        List<Point2D> current = new ArrayList<>();
        PathIterator iterator = shape.getPathIterator(null, FLATNESS);
        double[] coords = new double[6];
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> {
                    addRingIfReturningToStart(rings, current);
                    current = new ArrayList<>();
                    current.add(new Point2D.Double(coords[0], coords[1]));
                }
                case PathIterator.SEG_LINETO -> current.add(new Point2D.Double(coords[0], coords[1]));
                case PathIterator.SEG_CLOSE -> {
                    addRing(rings, current);
                    current = new ArrayList<>();
                }
                default -> {
                }
            }
            iterator.next();
        }
        addRingIfReturningToStart(rings, current);
        return rings;
    }

    private static void addRingIfReturningToStart(List<Coordinate[]> rings, List<Point2D> points) {
        if (points.size() >= 3 && points.get(0).distance(points.get(points.size() - 1)) <= FLATNESS) {
            addRing(rings, points);
        }
    }

    private static void addRing(List<Coordinate[]> rings, List<Point2D> points) {
        List<Point2D> ring = withoutDuplicates(points);
        if (ring.size() >= 2 && ring.get(0).distance(ring.get(ring.size() - 1)) <= FLATNESS) {
            ring = ring.subList(0, ring.size() - 1);
        }
        if (ring.size() < 3 || Math.abs(signedArea(ring)) <= MIN_RING_AREA) {
            return;
        }
        Coordinate[] coordinates = new Coordinate[ring.size() + 1];
        for (int i = 0; i < ring.size(); i++) {
            coordinates[i] = new Coordinate(ring.get(i).getX(), ring.get(i).getY());
        }
        coordinates[ring.size()] = coordinates[0].copy();
        rings.add(coordinates);
    }

    /**
     * The sub paths of the flattened shape as point lists, with closed ones ending on their
     * starting point.
     */
    private static List<List<Point2D>> polylines(Shape shape) {
        List<List<Point2D>> polylines = new ArrayList<>();
        List<Point2D> current = new ArrayList<>();
        PathIterator iterator = shape.getPathIterator(null, FLATNESS);
        double[] coords = new double[6];
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> {
                    addPolyline(polylines, current);
                    current = new ArrayList<>();
                    current.add(new Point2D.Double(coords[0], coords[1]));
                }
                case PathIterator.SEG_LINETO -> current.add(new Point2D.Double(coords[0], coords[1]));
                case PathIterator.SEG_CLOSE -> {
                    if (!current.isEmpty()) {
                        current.add(current.get(0));
                    }
                    addPolyline(polylines, current);
                    current = new ArrayList<>();
                }
                default -> {
                }
            }
            iterator.next();
        }
        addPolyline(polylines, current);
        return polylines;
    }

    private static void addPolyline(List<List<Point2D>> polylines, List<Point2D> points) {
        List<Point2D> cleaned = withoutDuplicates(points);
        if (cleaned.size() >= 2) {
            polylines.add(cleaned);
        }
    }

    /**
     * Emits the dashes of one segment and returns the distance into the on/off cycle at its
     * end, so the next segment continues the pattern.
     */
    private static double dashSegment(LineMeshBuilder builder, Point2D a, Point2D b, double dashLength,
                                      double phase, Color color) {
        double length = a.distance(b);
        if (length < COINCIDENT) {
            return phase;
        }
        double directionX = (b.getX() - a.getX()) / length;
        double directionY = (b.getY() - a.getY()) / length;
        double cycle = 2 * dashLength;
        double travelled = 0;
        while (travelled < length) {
            boolean on = phase < dashLength;
            double remainingInPart = (on ? dashLength : cycle) - phase;
            double step = Math.min(remainingInPart, length - travelled);
            if (on) {
                builder.add(a.getX() + directionX * travelled, a.getY() + directionY * travelled, 0,
                        a.getX() + directionX * (travelled + step), a.getY() + directionY * (travelled + step), 0,
                        color);
            }
            travelled += step;
            phase = (phase + step) % cycle;
        }
        return phase;
    }

    /**
     * Drops zero length edges, which the flattening routinely produces as a final line back onto
     * the starting point, and which break triangulation.
     */
    private static List<Point2D> withoutDuplicates(List<Point2D> points) {
        List<Point2D> result = new ArrayList<>(points.size());
        for (Point2D point : points) {
            if (result.isEmpty() || result.get(result.size() - 1).distance(point) > COINCIDENT) {
                result.add(point);
            }
        }
        return result;
    }

    private static double signedArea(List<Point2D> ring) {
        double area = 0;
        for (int i = 0; i < ring.size(); i++) {
            Point2D a = ring.get(i);
            Point2D b = ring.get((i + 1) % ring.size());
            area += a.getX() * b.getY() - b.getX() * a.getY();
        }
        return area / 2;
    }
}
