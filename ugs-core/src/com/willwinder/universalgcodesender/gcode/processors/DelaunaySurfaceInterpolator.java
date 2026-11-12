/*
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

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Triangle;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Interpolates Z across an arbitrary set of surface points using Delaunay triangles.
 * Point location uses the JTS STRtree, and Z comes from {@code Triangle.interpolateZ}.
 */
public class DelaunaySurfaceInterpolator {
    public static final String ERROR_NOT_ENOUGH_POINTS = "Need at least three non-collinear surface points.";
    public static final String ERROR_DUPLICATE_POINT = "Surface contains duplicate XY coordinates.";
    public static final String ERROR_INVALID_POINT = "Surface contains an invalid coordinate.";
    private static final double AREA_TOLERANCE = 1e-9d;

    private final Units units;
    private final List<Position> points;
    private final List<Position[]> triangles;
    private final STRtree index;
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    public DelaunaySurfaceInterpolator(List<Position> surfacePoints) {
        if (surfacePoints == null || surfacePoints.size() < 3) {
            throw new IllegalArgumentException(ERROR_NOT_ENOUGH_POINTS);
        }

        Units sourceUnits = surfacePoints.get(0).getUnits();
        if (sourceUnits == null || sourceUnits == Units.UNKNOWN) {
            throw new IllegalArgumentException(ERROR_INVALID_POINT);
        }

        List<Position> normalized = new ArrayList<>(surfacePoints.size());
        double lowestX = Double.POSITIVE_INFINITY;
        double highestX = Double.NEGATIVE_INFINITY;
        double lowestY = Double.POSITIVE_INFINITY;
        double highestY = Double.NEGATIVE_INFINITY;
        for (Position point : surfacePoints) {
            if (point == null || point.getUnits() == null || point.getUnits() == Units.UNKNOWN) {
                throw new IllegalArgumentException(ERROR_INVALID_POINT);
            }
            Position converted = point.getPositionIn(sourceUnits);
            if (!isFinite(converted)) {
                throw new IllegalArgumentException(ERROR_INVALID_POINT);
            }
            for (Position existing : normalized) {
                if (existing.getX() == converted.getX() && existing.getY() == converted.getY()) {
                    throw new IllegalArgumentException(ERROR_DUPLICATE_POINT);
                }
            }
            normalized.add(converted);
            lowestX = Math.min(lowestX, converted.getX());
            highestX = Math.max(highestX, converted.getX());
            lowestY = Math.min(lowestY, converted.getY());
            highestY = Math.max(highestY, converted.getY());
        }
        if (normalized.size() < 3 || areCollinear(normalized)) {
            throw new IllegalArgumentException(ERROR_NOT_ENOUGH_POINTS);
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = normalized.stream()
                .map(point -> new Coordinate(point.getX(), point.getY(), point.getZ()))
                .toArray(Coordinate[]::new);
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        builder.setSites(geometryFactory.createMultiPointFromCoords(coordinates));
        Geometry triangulated = builder.getTriangles(geometryFactory);

        List<Position[]> builtTriangles = new ArrayList<>();
        STRtree builtIndex = new STRtree();
        for (int i = 0; i < triangulated.getNumGeometries(); i++) {
            Coordinate[] triangleCoordinates = triangulated.getGeometryN(i).getCoordinates();
            if (triangleCoordinates.length < 4) {
                continue;
            }
            Position[] triangle = new Position[]{
                    toPosition(triangleCoordinates[0], sourceUnits),
                    toPosition(triangleCoordinates[1], sourceUnits),
                    toPosition(triangleCoordinates[2], sourceUnits)
            };
            if (Triangle.area(coordinate(triangle[0]), coordinate(triangle[1]), coordinate(triangle[2])) <= AREA_TOLERANCE) {
                continue;
            }
            builtTriangles.add(triangle);
            builtIndex.insert(triangulated.getGeometryN(i).getEnvelopeInternal(), triangle);
        }
        if (builtTriangles.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NOT_ENOUGH_POINTS);
        }

        this.units = sourceUnits;
        this.points = List.copyOf(normalized);
        this.triangles = List.copyOf(builtTriangles);
        this.index = builtIndex;
        this.index.build();
        this.minX = lowestX;
        this.maxX = highestX;
        this.minY = lowestY;
        this.maxY = highestY;
    }

    public Units getUnits() {
        return units;
    }

    public List<Position> getPoints() {
        return points;
    }

    public List<Position[]> getTriangles() {
        return triangles;
    }

    public double surfaceHeightAt(double x, double y) {
        if (x < minX || x > maxX || y < minY || y > maxY) {
            return 0;
        }

        @SuppressWarnings("unchecked")
        List<Position[]> candidates = index.query(new Envelope(x, x, y, y));
        for (Position[] triangle : candidates) {
            if (Triangle.intersects(
                    coordinate(triangle[0]), coordinate(triangle[1]), coordinate(triangle[2]), new Coordinate(x, y))) {
                return Triangle.interpolateZ(
                        new Coordinate(x, y),
                        new Coordinate(triangle[0].getX(), triangle[0].getY(), triangle[0].getZ()),
                        new Coordinate(triangle[1].getX(), triangle[1].getY(), triangle[1].getZ()),
                        new Coordinate(triangle[2].getX(), triangle[2].getY(), triangle[2].getZ()));
            }
        }
        return 0;
    }

    private static Coordinate coordinate(Position point) {
        return new Coordinate(point.getX(), point.getY(), point.getZ());
    }

    private static Position toPosition(Coordinate coordinate, Units units) {
        return new Position(coordinate.getX(), coordinate.getY(), coordinate.getZ(), units);
    }

    private static boolean isFinite(Position point) {
        return Double.isFinite(point.getX()) && Double.isFinite(point.getY()) && Double.isFinite(point.getZ());
    }

    private static boolean areCollinear(List<Position> points) {
        Position origin = points.get(0);
        Position direction = null;
        for (int i = 1; i < points.size(); i++) {
            Position point = points.get(i);
            double dx = point.getX() - origin.getX();
            double dy = point.getY() - origin.getY();
            if (direction == null) {
                if (dx * dx + dy * dy > AREA_TOLERANCE) {
                    direction = new Position(dx, dy, 0, origin.getUnits());
                }
                continue;
            }
            if (Math.abs(direction.getX() * dy - direction.getY() * dx) > AREA_TOLERANCE) {
                return false;
            }
        }
        return true;
    }
}
