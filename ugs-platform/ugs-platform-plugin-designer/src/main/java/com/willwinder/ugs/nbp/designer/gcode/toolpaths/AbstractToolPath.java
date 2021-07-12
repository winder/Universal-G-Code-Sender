package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractToolPath implements PathGenerator {
    /**
     * The depth that we are targeting for in millimeters
     */
    private double targetDepth = 0;

    /**
     * The tool diameter in millimeters
     */
    private double toolDiameter = 3;

    /**
     * The depth to plunge for each pass in millimeters
     */
    private double depthPerPass = 1;

    /**
     * A safe height above the material in millimeters
     */
    private double safeHeight = 1;

    private GeometryFactory geometryFactory = new GeometryFactory();

    public void setTargetDepth(double targetDepth) {
        this.targetDepth = Math.abs(targetDepth);
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = Math.abs(toolDiameter);
    }

    public void setDepthPerPass(double depthPerPass) {
        this.depthPerPass = Math.abs(depthPerPass);
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }

    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
    }

    public double getSafeHeight() {
        return safeHeight;
    }

    public double getTargetDepth() {
        return targetDepth;
    }

    public double getToolDiameter() {
        return toolDiameter;
    }

    protected void addSafeHeightSegment(GcodePath gcodePath) {
        NumericCoordinate safeHeightCoordinate = new NumericCoordinate();
        safeHeightCoordinate.set(Axis.Z, getSafeHeight());
        gcodePath.addSegment(SegmentType.MOVE, safeHeightCoordinate);
    }

    protected void addSafeHeightSegmentTo(GcodePath gcodePath, NumericCoordinate coordinate) {
        addSafeHeightSegment(gcodePath);
        gcodePath.addSegment(SegmentType.MOVE, new NumericCoordinate(coordinate.get(Axis.X), coordinate.get(Axis.Y)));
        gcodePath.addSegment(SegmentType.MOVE, new NumericCoordinate(0d));
    }

    protected LinearRing pathToLinearRing(GcodePath gcodePath) {
        List<org.locationtech.jts.geom.Coordinate> coordinateList = gcodePath.getSegments().stream()
                .map(segment -> pointToCoordinate(segment.getPoint()))
                .collect(Collectors.toList());

        coordinateList.add(pointToCoordinate(gcodePath.getSegments().get(0).getPoint()));

        CoordinateSequence points = new CoordinateArraySequence(coordinateList.toArray(new org.locationtech.jts.geom.Coordinate[]{}));
        GeometryFactory factory = new GeometryFactory();
        return new LinearRing(points, factory);
    }

    private org.locationtech.jts.geom.Coordinate pointToCoordinate(com.willwinder.ugs.nbp.designer.gcode.path.Coordinate point) {
        return new org.locationtech.jts.geom.Coordinate(point.get(Axis.X), point.get(Axis.Y), point.get(Axis.Z));
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    protected List<NumericCoordinate> geometryToCoordinates(Geometry geometry) {
        org.locationtech.jts.geom.Coordinate[] coordinates = geometry.getCoordinates();
        return Arrays.stream(coordinates)
                .map(c -> new NumericCoordinate(c.getX(), c.getY(), c.getZ()))
                .collect(Collectors.toList());
    }

    protected Geometry simplifyGeometry(Geometry bufferedGeometry) {
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(bufferedGeometry);
        simplifier.setDistanceTolerance(0.01);
        return simplifier.getResultGeometry();
    }

    protected GcodePath toGcodePath(List<List<NumericCoordinate>> coordinateList) {
        GcodePath gcodePath = new GcodePath();
        if (!coordinateList.isEmpty()) {
            addSafeHeightSegmentTo(gcodePath, coordinateList.get(0).get(0));
            coordinateList.forEach(cl -> {
                gcodePath.addSegment(SegmentType.POINT, cl.get(0));
                cl.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
            });

            addSafeHeightSegment(gcodePath);
        }
        return gcodePath;
    }
}
