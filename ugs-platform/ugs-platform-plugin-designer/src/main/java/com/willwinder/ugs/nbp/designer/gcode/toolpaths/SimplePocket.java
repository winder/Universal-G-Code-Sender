package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class SimplePocket implements PathGenerator {
    private final PathGenerator source;
    GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * The depth that we are targeting for
     */
    private double targetDepth;

    /**
     * The tool diameter in millimeters
     */
    private double toolDiameter = 3;

    /**
     * The depth to plunge for each pass
     */
    private double depthPerPass = 1;

    /**
     * How much should the tool cut for each pass. Should be larger than 0 and smaller than 1.
     * 0.1 would cut 10% of the tool diameter for each pass and 1 would cut 100% of the tool diameter.
     */
    private double stepOver = 0.3;

    public SimplePocket(PathGenerator source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        LinearRing linearRing = pathToLinearRing(source.toGcodePath());
        Polygon polygon = new Polygon(linearRing, new LinearRing[0], geometryFactory);

        List<List<NumericCoordinate>> coordinateList = new ArrayList<>();
        double currentDepth = 0;
        while (currentDepth < targetDepth) {

            currentDepth += depthPerPass;
            if(currentDepth > targetDepth) {
                currentDepth = targetDepth;
            }

            double buffer = 0;
            while (true) {
                DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(polygon.buffer(-buffer));
                simplifier.setDistanceTolerance(0.01);
                List<NumericCoordinate> numericCoordinates = new ArrayList<>();
                for (NumericCoordinate coordinate : geometryToCoordinates(simplifier.getResultGeometry())) {
                    numericCoordinates.add(coordinate.set(Axis.Z, -currentDepth));
                }

                if (numericCoordinates.isEmpty() || numericCoordinates.size() <= 1) {
                    break;
                }

                coordinateList.add(numericCoordinates);
                buffer = buffer + (toolDiameter * stepOver);
            }


        }

        GcodePath gcodePath = new GcodePath();

        // Rapid movement to first point
        if (!coordinateList.isEmpty()) {
            coordinateList.forEach(cl -> {
                gcodePath.addSegment(SegmentType.LINE, cl.get(0));
                cl.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
            });
        }
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

    public void setTargetDepth(double targetDepth) {
        this.targetDepth = Math.abs(targetDepth);
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = toolDiameter;
    }

    public void setStepOver(double stepOver) {
        this.stepOver = Math.min(Math.max(0.01, Math.abs(stepOver)), 1.0);
    }

    public void setDepthPerPass(double depthPerPass) {
        this.depthPerPass = Math.abs(depthPerPass);
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }
}
