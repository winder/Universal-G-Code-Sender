package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.Axis;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gcode.path.NumericCoordinate;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ToolPathUtils {
    public static List<Geometry> toGeometryList(Geometry geometry) {
        if (geometry instanceof MultiPolygon) {
            List<Geometry> geometryList = new ArrayList<>();
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                geometryList.add(geometry.getGeometryN(i));
            }
            return geometryList;
        }

        return Collections.singletonList(geometry);
    }

    public static List<NumericCoordinate> geometryToCoordinates(Geometry geometry) {
        org.locationtech.jts.geom.Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> new NumericCoordinate(c.getX(), c.getY(), c.getZ()))
                .collect(Collectors.toList());
    }

    public static Geometry simplifyGeometry(Geometry bufferedGeometry) {
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(bufferedGeometry);
        simplifier.setDistanceTolerance(0.01);
        return simplifier.getResultGeometry();
    }

    public static LinearRing pathToLinearRing(GcodePath gcodePath) {
        List<org.locationtech.jts.geom.Coordinate> coordinateList = gcodePath.getSegments().stream()
                .map(segment -> pointToCoordinate(segment.getPoint()))
                .collect(Collectors.toList());

        coordinateList.add(pointToCoordinate(gcodePath.getSegments().get(0).getPoint()));

        CoordinateSequence points = new CoordinateArraySequence(coordinateList.toArray(new org.locationtech.jts.geom.Coordinate[]{}));
        GeometryFactory factory = new GeometryFactory();
        return new LinearRing(points, factory);
    }

    private static org.locationtech.jts.geom.Coordinate pointToCoordinate(com.willwinder.ugs.nbp.designer.gcode.path.Coordinate point) {
        return new org.locationtech.jts.geom.Coordinate(point.get(Axis.X), point.get(Axis.Y), point.get(Axis.Z));
    }
}
