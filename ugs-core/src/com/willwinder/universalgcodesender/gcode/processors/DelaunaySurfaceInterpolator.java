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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interpolates Z across an arbitrary set of surface points using Delaunay triangles.
 * Point location uses the JTS STRtree, and Z comes from {@code Triangle.interpolateZ}.
 * A straight move can also be split where it leaves one triangle and enters the next.
 */
public class DelaunaySurfaceInterpolator {
    public static final String ERROR_NOT_ENOUGH_POINTS = "Need at least three non-collinear surface points.";
    public static final String ERROR_DUPLICATE_POINT = "Surface contains duplicate XY coordinates.";
    public static final String ERROR_INVALID_POINT = "Surface contains an invalid coordinate.";
    private static final double AREA_TOLERANCE = 1e-9d;
    /** Ignore crossings closer than this, in the surface's linear units. */
    private static final double SPLIT_SPACING = 1e-6d;
    private static final double ORIENT_EPS = 1e-9d;
    private static final double VERTEX_U = 1e-8d;
    private static final double PARALLEL_EPS = 1e-12d;

    private final Units units;
    private final List<Position> points;
    private final List<Position[]> triangles;
    private final Map<VertexKey, List<Tri>> vertices;
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

        List<Tri> builtMesh = new ArrayList<>();
        List<Position[]> builtTriangles = new ArrayList<>();
        Map<VertexKey, List<Tri>> builtVertices = new HashMap<>();
        Map<EdgeKey, EdgeLink> edgeLinks = new HashMap<>();
        STRtree builtIndex = new STRtree();
        for (int i = 0; i < triangulated.getNumGeometries(); i++) {
            Coordinate[] triangleCoordinates = triangulated.getGeometryN(i).getCoordinates();
            if (triangleCoordinates.length < 4) {
                continue;
            }
            Tri tri = new Tri(
                    toPosition(triangleCoordinates[0], sourceUnits),
                    toPosition(triangleCoordinates[1], sourceUnits),
                    toPosition(triangleCoordinates[2], sourceUnits));
            if (Triangle.area(coordinate(tri.a), coordinate(tri.b), coordinate(tri.c)) <= AREA_TOLERANCE) {
                continue;
            }
            builtMesh.add(tri);
            builtTriangles.add(tri.asArray());
            Envelope envelope = new Envelope(tri.a.getX(), tri.b.getX(), tri.a.getY(), tri.b.getY());
            envelope.expandToInclude(tri.c.getX(), tri.c.getY());
            builtIndex.insert(envelope, tri);
            addVertex(builtVertices, tri.a, tri);
            addVertex(builtVertices, tri.b, tri);
            addVertex(builtVertices, tri.c, tri);
            linkEdge(edgeLinks, tri, 0);
            linkEdge(edgeLinks, tri, 1);
            linkEdge(edgeLinks, tri, 2);
        }
        if (builtMesh.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NOT_ENOUGH_POINTS);
        }

        this.units = sourceUnits;
        this.points = List.copyOf(normalized);
        this.triangles = List.copyOf(builtTriangles);
        this.vertices = builtVertices;
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

        for (Tri triangle : trianglesAt(x, y)) {
            return Triangle.interpolateZ(
                    new Coordinate(x, y),
                    new Coordinate(triangle.a.getX(), triangle.a.getY(), triangle.a.getZ()),
                    new Coordinate(triangle.b.getX(), triangle.b.getY(), triangle.b.getZ()),
                    new Coordinate(triangle.c.getX(), triangle.c.getY(), triangle.c.getZ()));
        }
        return 0;
    }

    /**
     * Parameters along the segment from {@code (x0, y0)} to {@code (x1, y1)} where the
     * segment leaves one triangle and enters another. Empty when the whole segment lies
     * in one triangle, or misses the surface. Each value is {@code t} in {@code (0, 1)}
     * for the point {@code start + t * (end - start)}.
     */
    public List<Double> crossingParameters(double x0, double y0, double x1, double y1) {
        if (!Double.isFinite(x0) || !Double.isFinite(y0) || !Double.isFinite(x1) || !Double.isFinite(y1)) {
            return List.of();
        }
        double dx = x1 - x0;
        double dy = y1 - y0;
        double span = Math.hypot(dx, dy);
        if (span == 0d) {
            return List.of();
        }
        List<Tri> startHits = trianglesAt(x0, y0);
        for (Tri triangle : startHits) {
            if (contains(triangle, x1, y1)) {
                return List.of();
            }
        }
        if (startHits.isEmpty() && missesMesh(x0, y0, x1, y1)) {
            return List.of();
        }

        double tEps = SPLIT_SPACING / span;
        Tri triangle = triangleContainingRay(startHits, x0, y0, dx, dy);
        List<Double> splits = new ArrayList<>();
        double t = 0d;
        int guard = triangles.size() + 2;
        while (guard-- > 0 && t < 1d - tEps) {
            Hit hit = triangle == null
                    ? nearestEntry(x0, y0, dx, dy, t, tEps)
                    : earliestExit(triangle, x0, y0, dx, dy, t, tEps);
            if (hit == null || hit.t <= t) {
                break;
            }
            addSplit(splits, hit.t, tEps);
            t = hit.t;
            triangle = hit.next;
        }
        return splits.isEmpty() ? List.of() : List.copyOf(splits);
    }

    private List<Tri> trianglesAt(double x, double y) {
        if (x < minX || x > maxX || y < minY || y > maxY) {
            return List.of();
        }
        List<Tri> hits = new ArrayList<>();
        for (Tri triangle : query(new Envelope(x, x, y, y))) {
            if (contains(triangle, x, y)) {
                hits.add(triangle);
            }
        }
        return hits;
    }

    @SuppressWarnings("unchecked")
    private List<Tri> query(Envelope envelope) {
        return index.query(envelope);
    }

    private static boolean contains(Tri triangle, double x, double y) {
        return Triangle.intersects(coordinate(triangle.a), coordinate(triangle.b), coordinate(triangle.c), new Coordinate(x, y));
    }

    private boolean missesMesh(double x0, double y0, double x1, double y1) {
        return Math.max(x0, x1) < minX
                || Math.min(x0, x1) > maxX
                || Math.max(y0, y1) < minY
                || Math.min(y0, y1) > maxY;
    }

    private Tri triangleContainingRay(List<Tri> hits, double x, double y, double dx, double dy) {
        Tri alongBoundary = null;
        for (Tri triangle : hits) {
            int kind = containsRay(triangle, x, y, dx, dy);
            if (kind == 2) {
                return triangle;
            }
            if (kind == 1 && alongBoundary == null) {
                alongBoundary = triangle;
            }
        }
        return alongBoundary;
    }

    /**
     * First place the remaining segment enters a triangle, when the current position is outside.
     */
    private Hit nearestEntry(double x0, double y0, double dx, double dy, double tMin, double tEps) {
        double sx = x0 + tMin * dx;
        double sy = y0 + tMin * dy;
        Hit best = null;
        for (Tri triangle : query(new Envelope(sx, x0 + dx, sy, y0 + dy))) {
            for (int edge = 0; edge < 3; edge++) {
                Crossing crossing = intersect(triangle, edge, x0, y0, dx, dy);
                if (crossing == null || crossing.t <= tMin + tEps || crossing.t >= 1d - tEps) {
                    continue;
                }
                if (best != null && crossing.t >= best.t - tEps) {
                    continue;
                }
                Tri next = enteredTriangle(triangle, crossing, dx, dy);
                if (next == null) {
                    continue;
                }
                best = new Hit(crossing.t, next);
            }
        }
        return best;
    }

    /**
     * First place the segment leaves {@code triangle}. {@code next} is the triangle it enters,
     * or null when it leaves the surface.
     */
    private Hit earliestExit(Tri triangle, double x0, double y0, double dx, double dy, double tMin, double tEps) {
        Crossing first = null;
        Crossing second = null;
        for (int edge = 0; edge < 3; edge++) {
            Crossing crossing = intersect(triangle, edge, x0, y0, dx, dy);
            if (crossing == null || crossing.t <= tMin + tEps || crossing.t >= 1d - tEps) {
                continue;
            }
            if (first == null || crossing.t < first.t - tEps) {
                second = null;
                first = crossing;
            } else if (Math.abs(crossing.t - first.t) <= tEps) {
                second = crossing;
            }
        }
        if (first == null) {
            return null;
        }
        if (second != null || first.atVertex()) {
            Position vertex = second != null
                    ? triangle.vertex(sharedVertex(first.edge, second.edge))
                    : first.vertex(triangle);
            return new Hit(first.t, triangleInFront(vertex, dx, dy, triangle));
        }
        return new Hit(first.t, triangle.neighbors[first.edge]);
    }

    private Tri enteredTriangle(Tri triangle, Crossing crossing, double dx, double dy) {
        if (crossing.atVertex()) {
            return triangleInFront(crossing.vertex(triangle), dx, dy, null);
        }
        Position a = triangle.vertex(crossing.edge);
        Position b = triangle.vertex((crossing.edge + 1) % 3);
        Position c = triangle.vertex((crossing.edge + 2) % 3);
        double forward = (b.getX() - a.getX()) * dy - (b.getY() - a.getY()) * dx;
        if (forward * orient(a, b, c) < -ORIENT_EPS) {
            return triangle.neighbors[crossing.edge];
        }
        return triangle;
    }

    private Tri triangleInFront(Position vertex, double dx, double dy, Tri exclude) {
        List<Tri> incident = vertices.get(new VertexKey(vertex.getX(), vertex.getY()));
        if (incident == null) {
            return null;
        }
        Tri alongBoundary = null;
        for (Tri candidate : incident) {
            if (candidate == exclude) {
                continue;
            }
            int kind = containsRay(candidate, vertex.getX(), vertex.getY(), dx, dy);
            if (kind == 2) {
                return candidate;
            }
            if (kind == 1 && alongBoundary == null) {
                alongBoundary = candidate;
            }
        }
        return alongBoundary;
    }

    /**
     * @return 2 when the ray enters the triangle interior, 1 when it only runs along the boundary, 0 otherwise
     */
    private static int containsRay(Tri triangle, double x, double y, double dx, double dy) {
        boolean boundary = false;
        boolean intoInterior = false;
        for (int i = 0; i < 3; i++) {
            Position a = triangle.vertex(i);
            Position b = triangle.vertex((i + 1) % 3);
            Position c = triangle.vertex((i + 2) % 3);
            double interior = orient(a, b, c);
            double side = orient(a, b, x, y);
            if (side * interior < -ORIENT_EPS) {
                return 0;
            }
            if (Math.abs(side) <= ORIENT_EPS) {
                boundary = true;
                double forward = (b.getX() - a.getX()) * dy - (b.getY() - a.getY()) * dx;
                if (forward * interior < -ORIENT_EPS) {
                    return 0;
                }
                if (forward * interior > ORIENT_EPS) {
                    intoInterior = true;
                }
            }
        }
        if (!boundary) {
            return 2;
        }
        return intoInterior ? 2 : 1;
    }

    private static Crossing intersect(Tri triangle, int edge, double x0, double y0, double dx, double dy) {
        Position a = triangle.vertex(edge);
        Position b = triangle.vertex((edge + 1) % 3);
        double ex = b.getX() - a.getX();
        double ey = b.getY() - a.getY();
        double denom = dx * ey - dy * ex;
        double scale = Math.max(1d, Math.hypot(dx, dy) * Math.hypot(ex, ey));
        if (Math.abs(denom) <= PARALLEL_EPS * scale) {
            return null;
        }
        double rx = a.getX() - x0;
        double ry = a.getY() - y0;
        double t = (rx * ey - ry * ex) / denom;
        double u = (rx * dy - ry * dx) / denom;
        if (u < -VERTEX_U || u > 1d + VERTEX_U) {
            return null;
        }
        return new Crossing(t, Math.min(1d, Math.max(0d, u)), edge);
    }

    private static int sharedVertex(int edgeA, int edgeB) {
        int start = edgeA;
        int end = (edgeA + 1) % 3;
        int otherStart = edgeB;
        int otherEnd = (edgeB + 1) % 3;
        if (start == otherStart || start == otherEnd) {
            return start;
        }
        return end;
    }

    private static void addSplit(List<Double> splits, double t, double tEps) {
        if (t <= tEps || t >= 1d - tEps) {
            return;
        }
        if (!splits.isEmpty() && t - splits.get(splits.size() - 1) <= tEps) {
            return;
        }
        splits.add(t);
    }

    private static void addVertex(Map<VertexKey, List<Tri>> index, Position vertex, Tri triangle) {
        index.computeIfAbsent(new VertexKey(vertex.getX(), vertex.getY()), key -> new ArrayList<>()).add(triangle);
    }

    private static void linkEdge(Map<EdgeKey, EdgeLink> links, Tri triangle, int edge) {
        EdgeKey key = new EdgeKey(triangle.vertex(edge), triangle.vertex((edge + 1) % 3));
        EdgeLink existing = links.get(key);
        if (existing == null) {
            links.put(key, new EdgeLink(triangle, edge));
            return;
        }
        triangle.neighbors[edge] = existing.triangle;
        existing.triangle.neighbors[existing.edge] = triangle;
    }

    private static double orient(Position a, Position b, Position c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    private static double orient(Position a, Position b, double x, double y) {
        return (b.getX() - a.getX()) * (y - a.getY()) - (b.getY() - a.getY()) * (x - a.getX());
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

    private static long bits(double value) {
        return value == 0d ? 0L : Double.doubleToLongBits(value);
    }

    private static final class Tri {
        final Position a;
        final Position b;
        final Position c;
        final Tri[] neighbors = new Tri[3];

        Tri(Position a, Position b, Position c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        Position vertex(int index) {
            if (index == 0) {
                return a;
            }
            if (index == 1) {
                return b;
            }
            return c;
        }

        Position[] asArray() {
            return new Position[]{a, b, c};
        }
    }

    private static final class Crossing {
        final double t;
        final double u;
        final int edge;

        Crossing(double t, double u, int edge) {
            this.t = t;
            this.u = u;
            this.edge = edge;
        }

        boolean atVertex() {
            return u <= VERTEX_U || u >= 1d - VERTEX_U;
        }

        Position vertex(Tri owner) {
            return u < 0.5d ? owner.vertex(edge) : owner.vertex((edge + 1) % 3);
        }
    }

    private static final class Hit {
        final double t;
        final Tri next;

        Hit(double t, Tri next) {
            this.t = t;
            this.next = next;
        }
    }

    private static final class EdgeLink {
        final Tri triangle;
        final int edge;

        EdgeLink(Tri triangle, int edge) {
            this.triangle = triangle;
            this.edge = edge;
        }
    }

    private static final class EdgeKey {
        final long x1;
        final long y1;
        final long x2;
        final long y2;

        EdgeKey(Position p, Position q) {
            boolean swap = p.getX() > q.getX() || (p.getX() == q.getX() && p.getY() > q.getY());
            Position first = swap ? q : p;
            Position second = swap ? p : q;
            x1 = bits(first.getX());
            y1 = bits(first.getY());
            x2 = bits(second.getX());
            y2 = bits(second.getY());
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof EdgeKey)) {
                return false;
            }
            EdgeKey key = (EdgeKey) other;
            return x1 == key.x1 && y1 == key.y1 && x2 == key.x2 && y2 == key.y2;
        }

        @Override
        public int hashCode() {
            int hash = Long.hashCode(x1);
            hash = 31 * hash + Long.hashCode(y1);
            hash = 31 * hash + Long.hashCode(x2);
            return 31 * hash + Long.hashCode(y2);
        }
    }

    private static final class VertexKey {
        final long x;
        final long y;

        VertexKey(double x, double y) {
            this.x = bits(x);
            this.y = bits(y);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof VertexKey)) {
                return false;
            }
            VertexKey key = (VertexKey) other;
            return x == key.x && y == key.y;
        }

        @Override
        public int hashCode() {
            return 31 * Long.hashCode(x) + Long.hashCode(y);
        }
    }
}
