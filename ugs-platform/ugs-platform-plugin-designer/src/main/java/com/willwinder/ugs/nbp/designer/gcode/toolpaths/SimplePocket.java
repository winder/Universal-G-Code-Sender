package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class SimplePocket implements PathGenerator {
    private final PathGenerator source;
    GeometryFactory geometryFactory = new GeometryFactory();
    private double depth;
    private double passDepth = 1;
    private double passOver = 10;

    public SimplePocket(PathGenerator source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        LinearRing linearRing = pathToLinearRing(source.toGcodePath());
        Polygon polygon = new Polygon(linearRing, new LinearRing[0], geometryFactory);

        List<List<NumericCoordinate>> coordinateList = new ArrayList<>();
        double buffer = 0;
        while (true) {
            DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(polygon.buffer(-buffer));
            simplifier.setDistanceTolerance(0.5);
            List<NumericCoordinate> numericCoordinates = geometryToCoordinates(simplifier.getResultGeometry());
            if (numericCoordinates.isEmpty() || numericCoordinates.size() <= 1) {
                break;
            }
            coordinateList.add(numericCoordinates);
            buffer = buffer + passOver;
        }

        Collections.reverse(coordinateList);
        GcodePath gcodePath = new GcodePath();
        coordinateList.forEach(cl -> {
            gcodePath.addSegment(SegmentType.LINE, cl.get(0));
            cl.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
        });
        return gcodePath;
    }

    private LinearRing pathToLinearRing(GcodePath gcodePath) {
        List<Coordinate> coordinateList = gcodePath.getSegments().stream()
                .map(segment -> pointToCoordinate(segment.getPoint()))
                .collect(Collectors.toList());

        coordinateList.add(pointToCoordinate(gcodePath.getSegments().get(0).getPoint()));

        CoordinateSequence points = new CoordinateArraySequence(coordinateList.toArray(new Coordinate[]{}));
        GeometryFactory factory = new GeometryFactory();
        return new LinearRing(points, factory);
    }

    private Coordinate pointToCoordinate(com.willwinder.ugs.nbp.designer.gcode.path.Coordinate point) {
        return new Coordinate(point.get(Axis.X), point.get(Axis.Y), point.get(Axis.Z));
    }

    private List<NumericCoordinate> geometryToCoordinates(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> new NumericCoordinate(c.getX(), c.getY(), c.getZ()))
                .collect(Collectors.toList());

    }
}
