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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.universalgcodesender.model.CNCPoint;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ToolPathUtils {
    public static final double DISTANCE_TOLERANCE = 0.1;

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private ToolPathUtils() {
    }

    public static List<Geometry> toGeometryList(Geometry geometry) {
        List<Geometry> geometryList = new ArrayList<>();
        recursivlyCollectGeometries(geometry, geometryList);
        geometryList.sort(new GeometryPositionComparator(geometry.getEnvelopeInternal()));
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

    public static List<PartialPosition> geometryToCoordinates(Geometry geometry, double depth) {
        org.locationtech.jts.geom.Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> toPartialPosition(c, depth))
                .toList();
    }

    public static Geometry convertAreaToGeometry(final Area area, final GeometryFactory factory, double flatnessPrecision) {

        PathIterator iter = area.getPathIterator(null, flatnessPrecision);

        PrecisionModel precisionModel = factory.getPrecisionModel();
        Polygonizer polygonizer = new Polygonizer(true);

        List<Coordinate[]> coords = ShapeReader.toCoordinates(iter);
        List<Geometry> geometries = new ArrayList<>();
        for (Coordinate[] array : coords) {
            for (Coordinate c : array)
                precisionModel.makePrecise(c);

            LineString lineString = factory.createLineString(array);
            geometries.add(lineString);
        }
        polygonizer.add(factory.buildGeometry(geometries).union());
        return polygonizer.getGeometry();
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
            if (segment.getType() == SegmentType.SEAM) {
                // Do nothing
            } else if (segment.getType() == SegmentType.MOVE) {
                totalRapidLength += distanceBetween(position, segment.getPoint());
            } else {
                totalFeedLength += distanceBetween(position, segment.getPoint());
            }
        }

        return new ToolPathStats(totalFeedLength, totalRapidLength);
    }

    public static List<Geometry> bufferAndCollectGeometries(Geometry geometry, double toolDiameter, double stepOver) {
        double buffer = toolDiameter / 2d;
        List<Geometry> geometries = ToolPathUtils.bufferAndCollectGeometries(geometry, buffer, toolDiameter, stepOver);
        geometries.sort(new GeometrySizeComparator());
        return geometries;
    }

    public static List<Geometry> bufferAndCollectGeometries(Geometry geometry, double buffer, double toolDiameter, double stepOver) {
        Geometry bufferedGeometry = geometry.buffer(-buffer);
        if (bufferedGeometry.getNumGeometries() <= 0 || bufferedGeometry.isEmpty()) {
            return Collections.emptyList();
        }

        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < bufferedGeometry.getNumGeometries(); i++) {
            Geometry geom = bufferedGeometry.getGeometryN(i);
            result.addAll(bufferAndCollectGeometries(geom, toolDiameter * stepOver, toolDiameter, stepOver));

            if (geom instanceof Polygon polygon) {
                result.add(DouglasPeuckerSimplifier.simplify(polygon.getExteriorRing(), DISTANCE_TOLERANCE));
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    result.add(DouglasPeuckerSimplifier.simplify(polygon.getInteriorRingN(j), DISTANCE_TOLERANCE));
                }
            } else {
                result.add(DouglasPeuckerSimplifier.simplify(geom, DISTANCE_TOLERANCE));
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

                Coordinate firstCoordinate = geometry.getCoordinates()[0];
                PartialPosition nextPosition = toPartialPosition(firstCoordinate, currentDepth);

                LineString lineString = ToolPathUtils.createLineString(fromPosition, nextPosition);
                if (shell.crosses(lineString) || geometry.getClass().equals(LineString.class)) {
                    coordinateList.add(geometryLine);
                    geometryLine = new ArrayList<>();
                }
            }

            geometryLine.addAll(geometryToCoordinates(geometry, currentDepth));
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
