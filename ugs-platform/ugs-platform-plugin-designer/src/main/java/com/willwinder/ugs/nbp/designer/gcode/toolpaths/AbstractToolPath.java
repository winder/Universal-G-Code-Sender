package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;
import com.willwinder.ugs.nbp.designer.gcode.path.Coordinate;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    protected GcodePath toGcodePath(List<List<NumericCoordinate>> coordinateList) {
        GcodePath gcodePath = new GcodePath();
        if (!coordinateList.isEmpty()) {
            coordinateList.forEach(cl -> {
                if (!cl.isEmpty()) {
                    addSafeHeightSegmentTo(gcodePath, cl.get(0));
                    gcodePath.addSegment(SegmentType.POINT, cl.get(0));
                    cl.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
                }
            });

            addSafeHeightSegment(gcodePath);
        }
        return gcodePath;
    }
}
