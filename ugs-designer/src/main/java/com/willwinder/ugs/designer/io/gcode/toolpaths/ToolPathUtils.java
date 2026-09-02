/*
    Copyright 2023 Will Winder

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

import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.universalgcodesender.model.CNCPoint;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.geom.util.PolygonExtracter;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ToolPathUtils {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private ToolPathUtils() {
    }

    public static List<Geometry> toGeometryList(Geometry geometry) {
        List<Geometry> geometryList = new ArrayList<>();
        recursivlyCollectGeometries(geometry, geometryList);
        geometryList.sort(new HilbertPositionComparator(geometry.getEnvelopeInternal()));
        return geometryList;
    }

    public static void recursivlyCollectGeometries(Geometry geometry, List<Geometry> result) {
        if (geometry == null || geometry.isEmpty()) {
            return;
        }

        if (geometry.getNumGeometries() > 1) {
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                recursivlyCollectGeometries(geometry.getGeometryN(i), result);
            }
        } else if (geometry instanceof Polygon polygon) {
            result.add((polygon).getExteriorRing());
            for (int i = 0; i < (polygon).getNumInteriorRing(); i++) {
                result.add((polygon).getInteriorRingN(i));
            }
        } else {
            result.add(geometry);
        }
    }

    public static List<PartialPosition> geometryToCoordinates(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> new PartialPosition(c.getX(), c.getY(), c.getZ(), UnitUtils.Units.MM))
                .toList();
    }

    public static List<PartialPosition> geometryToCoordinates(Geometry geometry, double depth, boolean clockwise) {
        Coordinate[] coordinates = geometry.getCoordinates().clone();
        if (!clockwise) {
            ArrayUtils.reverse(coordinates);
        }

        return Arrays.stream(coordinates)
                .map(c -> toPartialPosition(c, depth))
                .toList();
    }

    /**
     * Converts the area to polygons with the same holes and islands. The rings of an area bound
     * its material by the even-odd rule: a ring inside another ring is a hole, a ring inside a hole
     * is an island. Each ring is classified by how many rings enclose it, which keeps the rings'
     * own vertices and direction so the tool paths follow them as drawn. A ring that the
     * flattening left self intersecting is repaired first, or it would corrupt the classification.
     */
    public static Geometry convertAreaToGeometry(final Area area, final GeometryFactory factory, double flatnessPrecision) {
        PathIterator iter = area.getPathIterator(null, flatnessPrecision);
        PrecisionModel precisionModel = factory.getPrecisionModel();

        List<Coordinate[]> coords = ShapeReader.toCoordinates(iter);
        List<Polygon> rings = new ArrayList<>();
        for (Coordinate[] array : coords) {
            for (Coordinate c : array) {
                precisionModel.makePrecise(c);
            }
            if (array.length < 4) {
                continue;
            }
            Polygon ring = factory.createPolygon(array);
            if (ring.isValid()) {
                rings.add(ring);
            } else {
                rings.addAll(ringsOf(GeometryFixer.fix(ring), factory));
            }
        }
        return assembleByNesting(rings, factory);
    }

    /**
     * The rings of a repaired ring, each as a polygon of its own; repairing a ring that crosses
     * itself splits it into the loops it was made of.
     */
    private static List<Polygon> ringsOf(Geometry repaired, GeometryFactory factory) {
        List<Polygon> rings = new ArrayList<>();
        for (Object part : PolygonExtracter.getPolygons(repaired)) {
            Polygon polygon = (Polygon) part;
            rings.add(factory.createPolygon(polygon.getExteriorRing().getCoordinates()));
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                rings.add(factory.createPolygon(polygon.getInteriorRingN(i).getCoordinates()));
            }
        }
        return rings;
    }

    private static Geometry assembleByNesting(List<Polygon> rings, GeometryFactory factory) {
        List<PreparedGeometry> prepared = rings.stream().map(PreparedGeometryFactory::prepare).toList();
        List<Point> interiorPoints = rings.stream().map(Polygon::getInteriorPoint).toList();

        // The rings enclosing each ring, innermost last; a ring at even depth is a shell.
        int[] depth = new int[rings.size()];
        int[] parent = new int[rings.size()];
        Arrays.fill(parent, -1);
        for (int i = 0; i < rings.size(); i++) {
            double parentArea = Double.MAX_VALUE;
            for (int j = 0; j < rings.size(); j++) {
                if (i != j && rings.get(j).getArea() > rings.get(i).getArea() && prepared.get(j).contains(interiorPoints.get(i))) {
                    depth[i]++;
                    if (rings.get(j).getArea() < parentArea) {
                        parentArea = rings.get(j).getArea();
                        parent[i] = j;
                    }
                }
            }
        }

        List<List<LinearRing>> holesOfShell = new ArrayList<>();
        for (int i = 0; i < rings.size(); i++) {
            holesOfShell.add(new ArrayList<>());
        }
        for (int i = 0; i < rings.size(); i++) {
            if (depth[i] % 2 == 1) {
                holesOfShell.get(parent[i]).add(rings.get(i).getExteriorRing());
            }
        }

        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < rings.size(); i++) {
            if (depth[i] % 2 == 0) {
                polygons.add(factory.createPolygon(rings.get(i).getExteriorRing(), holesOfShell.get(i).toArray(new LinearRing[0])));
            }
        }
        List<Geometry> merged = dissolveSeams(polygons, factory);
        return merged.size() == 1 ? merged.get(0) : factory.buildGeometry(merged);
    }

    /**
     * An area emits a region whose outline meets itself as several rings sharing an edge, split
     * along horizontal seams. Cut as separate pockets those seams would be left standing, so the
     * polygons sharing an edge are unioned back into one. Polygons that only meet at a point or
     * not at all are left as they are.
     */
    private static List<Geometry> dissolveSeams(List<Polygon> polygons, GeometryFactory factory) {
        int[] group = new int[polygons.size()];
        Arrays.fill(group, -1);
        int groups = 0;
        for (int i = 0; i < polygons.size(); i++) {
            if (group[i] < 0) {
                group[i] = groups++;
            }
            for (int j = i + 1; j < polygons.size(); j++) {
                if (sharesAnEdge(polygons.get(i), polygons.get(j))) {
                    if (group[j] < 0) {
                        group[j] = group[i];
                    } else if (group[j] != group[i]) {
                        int from = group[j];
                        for (int k = 0; k < group.length; k++) {
                            if (group[k] == from) {
                                group[k] = group[i];
                            }
                        }
                    }
                }
            }
        }

        List<Geometry> result = new ArrayList<>();
        for (int g = 0; g < groups; g++) {
            List<Geometry> members = new ArrayList<>();
            for (int i = 0; i < polygons.size(); i++) {
                if (group[i] == g) {
                    members.add(polygons.get(i));
                }
            }
            if (members.size() == 1) {
                result.add(members.get(0));
            } else if (members.size() > 1) {
                Geometry union = OverlayNGRobust.union(factory.buildGeometry(members));
                for (int i = 0; i < union.getNumGeometries(); i++) {
                    result.add(union.getGeometryN(i));
                }
            }
        }
        return result;
    }

    private static boolean sharesAnEdge(Polygon a, Polygon b) {
        return a.getEnvelopeInternal().intersects(b.getEnvelopeInternal()) && a.relate(b, "****1****");
    }

    public static List<Geometry> convertShapeToGeometry(Shape shape, GeometryFactory factory, double flatnessPrecision) {
        PathIterator iter = shape.getPathIterator(null, flatnessPrecision);
        PrecisionModel precisionModel = factory.getPrecisionModel();

        List<Coordinate[]> coords = ShapeReader.toCoordinates(iter);
        List<Geometry> geometries = new ArrayList<>();
        for (Coordinate[] array : coords) {
            for (Coordinate c : array)
                precisionModel.makePrecise(c);

            LineString lineString = factory.createLineString(array);
            geometries.add(lineString);
        }
        return geometries;
    }

    public static boolean isClosedGeometry(Shape shape) {
        final PathIterator path = shape.getPathIterator(null);
        final double[] crd = new double[6];

        while (!path.isDone()) {
            if (path.currentSegment(crd) == PathIterator.SEG_CLOSE)
                return true;

            path.next();
        }

        return false;
    }

    public static PartialPosition toPartialPosition(Coordinate coordinate, double depth) {
        return new PartialPosition(coordinate.getX(), coordinate.getY(), -depth, UnitUtils.Units.MM);
    }

    public static Coordinate toCoordinate(PartialPosition position) {
        return new Coordinate(position.getX(), position.getY(), position.getZ());
    }

    public static LineString createLineString(PartialPosition fromPosition, PartialPosition toPosition) {
        return GEOMETRY_FACTORY.createLineString(new Coordinate[]{toCoordinate(fromPosition), toCoordinate(toPosition)});
    }

    public static LinearRing createLinearRing(Coordinate[] points) {
        return GEOMETRY_FACTORY.createLinearRing(points);
    }

    public static int findNearestCoordinateIndex(Coordinate[] coordinates, Coordinate coordinate) {
        int index = 0;
        double shortestDistance = Double.MAX_VALUE;
        for (int i = 0; i < coordinates.length; i++) {
            double distance = coordinates[i].distance(coordinate);
            if (distance < shortestDistance) {
                index = i;
                shortestDistance = distance;
            }
        }
        return index;
    }


    private static double distanceBetween(PartialPosition position, PartialPosition point) {
        CNCPoint point1 = new CNCPoint(position.getX(), position.getY(), position.getZ(), 0, 0, 0);
        CNCPoint point2 = new CNCPoint(point.hasX() ? point.getX() : position.getX(), point.hasY() ? point.getY() : position.getY(), point.hasZ() ? point.getZ() : position.getZ(), 0, 0, 0);
        return point1.distanceXYZ(point2);
    }

    public static ToolPathStats getToolPathStats(GcodePath gcodePath) {
        PartialPosition position = new PartialPosition(0d, 0d, 0d, UnitUtils.Units.MM);
        double totalRapidLength = 0;
        double totalFeedLength = 0;
        for (Segment segment : gcodePath.getSegments()) {
            // Seam and pen segments carry no position, so they can not move the tool anywhere
            if (segment.point == null) {
                continue;
            }

            if (segment.getType() == SegmentType.MOVE) {
                totalRapidLength += distanceBetween(position, segment.getPoint());
            } else {
                totalFeedLength += distanceBetween(position, segment.getPoint());
            }
        }

        return new ToolPathStats(totalFeedLength, totalRapidLength);
    }

    /**
     * Offsets the geometry inwards repeatedly to produce the rings clearing out a pocket.
     *
     * @param geometry          the geometry to clear out
     * @param toolDiameter      the diameter of the tool doing the clearing
     * @param stepOver          how much of the tool diameter to move between each ring
     * @param simplifyTolerance how far the rings may be moved when dropping points that are not
     *                          needed to describe them
     */
    public static List<Geometry> bufferAndCollectGeometries(Geometry geometry, double toolDiameter, double stepOver, double simplifyTolerance) {
        double buffer = toolDiameter / 2d;
        List<Geometry> geometries = ToolPathUtils.bufferAndCollectGeometries(geometry, buffer, toolDiameter, stepOver, simplifyTolerance);
        geometries.sort(new GeometrySizeComparator());
        return geometries;
    }

    public static List<Geometry> bufferAndCollectGeometries(Geometry geometry, double buffer, double toolDiameter, double stepOver, double simplifyTolerance) {
        Geometry bufferedGeometry = geometry.buffer(-buffer);
        if (bufferedGeometry.getNumGeometries() <= 0 || bufferedGeometry.isEmpty()) {
            return Collections.emptyList();
        }

        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < bufferedGeometry.getNumGeometries(); i++) {
            Geometry geom = bufferedGeometry.getGeometryN(i);
            result.addAll(bufferAndCollectGeometries(geom, toolDiameter * stepOver, toolDiameter, stepOver, simplifyTolerance));

            if (geom instanceof Polygon polygon) {
                result.add(DouglasPeuckerSimplifier.simplify(polygon.getExteriorRing(), simplifyTolerance));
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    result.add(DouglasPeuckerSimplifier.simplify(polygon.getInteriorRingN(j), simplifyTolerance));
                }
            } else {
                result.add(DouglasPeuckerSimplifier.simplify(geom, simplifyTolerance));
            }
        }

        return result;
    }

    public static LinearRing rotateCoordinates(LinearRing nextGeometry, int newStartIndex) {
        Coordinate[] geomCoordinates = nextGeometry.getCoordinates();
        Coordinate[] newCoordinates = new Coordinate[geomCoordinates.length];
        int newIndex = 0;
        for (int coordIndex = newStartIndex; coordIndex < newCoordinates.length; coordIndex++) {
            newCoordinates[newIndex] = geomCoordinates[coordIndex];
            newIndex++;
        }

        for (int coordIndex = 1; coordIndex < newStartIndex; coordIndex++) {
            newCoordinates[newIndex] = geomCoordinates[coordIndex];
            newIndex++;
        }

        newCoordinates[newCoordinates.length - 1] = geomCoordinates[newStartIndex];
        nextGeometry = ToolPathUtils.createLinearRing(newCoordinates);
        return nextGeometry;
    }

    public static void addGeometriesToCoordinatesList(Geometry shell, List<Geometry> geometries, List<List<PartialPosition>> coordinateList, double currentDepth) {
        addGeometriesToCoordinatesList(shell, geometries, coordinateList, currentDepth, true);
    }

    public static void addGeometriesToCoordinatesList(Geometry shell, List<Geometry> geometries, List<List<PartialPosition>> coordinateList, double currentDepth, boolean clockwise) {
        Geometry previousGeometry = null;
        List<PartialPosition> geometryLine = new ArrayList<>();
        for (int x = 0; x < geometries.size(); x++) {
            Geometry geometry = geometries.get(x);

            if (x > 0) {
                PartialPosition fromPosition = ToolPathUtils.toPartialPosition(getLastPosition(previousGeometry), currentDepth);
                int newStartIndex = ToolPathUtils.findNearestCoordinateIndex(geometry.getCoordinates(), new Coordinate(fromPosition.getX(), fromPosition.getY(), fromPosition.getZ()));

                if (geometry instanceof LinearRing linearRing) {
                    geometry = rotateCoordinates(linearRing, newStartIndex);
                }

                Coordinate[] coordinates = geometry.getCoordinates();

                Coordinate firstCoordinate = coordinates[0];
                PartialPosition nextPosition = toPartialPosition(firstCoordinate, currentDepth);

                LineString lineString = ToolPathUtils.createLineString(fromPosition, nextPosition);
                if (shell.crosses(lineString) || geometry.getClass().equals(LineString.class)) {
                    coordinateList.add(geometryLine);
                    geometryLine = new ArrayList<>();
                }
            }

            geometryLine.addAll(geometryToCoordinates(geometry, currentDepth, clockwise));
            previousGeometry = geometry;
        }

        if (!geometryLine.isEmpty()) {
            coordinateList.add(geometryLine);
        }
    }

    private static Coordinate getLastPosition(Geometry geometry) {
        return geometry.getCoordinates()[geometry.getCoordinates().length - 1];
    }
}
