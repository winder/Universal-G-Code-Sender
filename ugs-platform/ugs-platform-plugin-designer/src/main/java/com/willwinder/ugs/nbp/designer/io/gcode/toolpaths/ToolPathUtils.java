package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ToolPathUtils {

    public static final double FLATNESS_PRECISION = 0.1d;

    public static List<Geometry> toGeometryList(Geometry geometry) {
        List<Geometry> geometryList = new ArrayList<>();
        recursivlyCollectGeometries(geometry, geometryList);
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
        } else if (geometry instanceof Polygon) {
            result.add(((Polygon) geometry).getExteriorRing());
            for (int i = 0; i < ((Polygon) geometry).getNumInteriorRing(); i++) {
                result.add(((Polygon) geometry).getInteriorRingN(i));
            }
        } else {
            result.add(geometry);
        }
    }

    public static List<PartialPosition> geometryToCoordinates(Geometry geometry) {
        org.locationtech.jts.geom.Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> new PartialPosition(c.getX(), c.getY(), c.getZ(), UnitUtils.Units.MM))
                .collect(Collectors.toList());
    }

    public static List<List<PartialPosition>> geometriesToCoordinates(List<Geometry> geometries, double depth) {
        return geometries.stream()
                .map(geometry -> {
                    List<PartialPosition> bufferedCoordinates = geometryToCoordinates(geometry)
                            .stream()
                            .map(c -> new PartialPosition(c.getX(), c.getY(), -depth, UnitUtils.Units.MM))
                            .collect(Collectors.toList());

                    if (bufferedCoordinates.isEmpty() || bufferedCoordinates.size() <= 1) {
                        return null;
                    }

                    return bufferedCoordinates;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Geometry convertAreaToGeometry(final Area area, final GeometryFactory factory) {

        PathIterator iter = area.getPathIterator(null, FLATNESS_PRECISION);

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

    public static List<Geometry> convertShapeToGeometry(Shape shape, GeometryFactory factory) {
        PathIterator iter = shape.getPathIterator(null, FLATNESS_PRECISION);
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
}
