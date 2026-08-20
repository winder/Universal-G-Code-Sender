/*
    Copyright 2021-2024 Will Winder

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

import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.PlungeType;
import com.willwinder.ugs.designer.io.gcode.path.ArcFitter;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.PathGenerator;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.io.gcode.toolpaths.LinearRamp.RampedPath;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;
import java.util.Optional;

public abstract class AbstractToolPath implements PathGenerator {
    /**
     * Positions in millimeters closer than this are treated as the tool already being there
     */
    private static final double POSITION_TOLERANCE = 0.0001;

    protected final Settings settings;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    /**
     * The depth to start from in millimeters
     */
    private double startDepth = 0;
    /**
     * The depth that we are targeting for in millimeters
     */
    private double targetDepth = 0;

    /**
     * Where the tool was left by the previous run, used to tell if it needs to be lifted clear of the
     * material before it can be moved to the next one.
     */
    private PartialPosition lastPosition;

    protected AbstractToolPath(Settings settings) {
        this.settings = settings;
    }

    public double getStartDepth() {
        return startDepth;
    }

    public void setStartDepth(double startDepth) {
        this.startDepth = startDepth;
    }

    public double getTargetDepth() {
        return targetDepth;
    }

    public void setTargetDepth(double targetDepth) {
        this.targetDepth = targetDepth;
    }

    protected void addSafeHeightSegment(GcodePath gcodePath, PartialPosition coordinate, boolean isFirst) {
        double safeHeight = settings.getSafeHeight();

        // If the start depth is negative we need to add it to the safe height to clear the material
        if (startDepth < 0) {
            safeHeight = safeHeight - startDepth;
        }

        PartialPosition safeHeightCoordinate = PartialPosition.from(Axis.Z, safeHeight, UnitUtils.Units.MM);
        gcodePath.addSegment(SegmentType.MOVE, safeHeightCoordinate);
    }

    protected void addSafeHeightSegmentTo(GcodePath gcodePath, PartialPosition coordinate, boolean isFirst) {
        addSafeHeightSegment(gcodePath,coordinate, isFirst);
        gcodePath.addSegment(SegmentType.MOVE, new PartialPosition(coordinate.getX(), coordinate.getY(), UnitUtils.Units.MM));
        if (!isFirst) {
            gcodePath.addSegment(SegmentType.MOVE, PartialPosition.from(Axis.Z, -getStartDepth(), UnitUtils.Units.MM));
        } else {
            addSafeHeightSegment(gcodePath,coordinate, isFirst);
        }
    }

    /**
     * If reaching the given coordinate takes the tool somewhere else in the XY plane, moving it over
     * material that has not been cut away yet.
     */
    protected boolean isMovingOverMaterial(PartialPosition coordinate) {
        if (lastPosition == null || coordinate == null || !coordinate.hasX() || !coordinate.hasY()) {
            return true;
        }

        return Math.abs(lastPosition.getX() - coordinate.getX()) > POSITION_TOLERANCE
                || Math.abs(lastPosition.getY() - coordinate.getY()) > POSITION_TOLERANCE;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    protected void addToGcodePath(GcodePath gcodePath, List<List<PartialPosition>> coordinateList, Cuttable source) {
        lastPosition = null;
        if (!coordinateList.isEmpty()) {
            if (source.getSpindleSpeed() > 0) {
                gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), null));
            }

            coordinateList.forEach(cl -> {
                if (!cl.isEmpty()) {
                    addRunToGcodePath(gcodePath, cl, source, coordinateList.get(0) == cl);
                }
            });

            addSafeHeightSegment(gcodePath, null,true);
        }
    }

    /**
     * Adds the segments moving the tool down to the depth of one run of the tool path and cutting it,
     * either by plunging straight down at the start of the run or by ramping down into it.
     */
    private void addRunToGcodePath(GcodePath gcodePath, List<PartialPosition> coordinates, Cuttable source, boolean isFirst) {
        Optional<RampedPath> rampedPath = toRampedPath(coordinates, source);
        List<PartialPosition> cutPath = coordinates;

        if (rampedPath.isPresent()) {
            RampedPath ramp = rampedPath.get();
            ramp.entry().ifPresent(entry -> addSafeHeightSegmentTo(gcodePath, entry, isFirst));
            ramp.segments().forEach(gcodePath::addSegment);
            cutPath = ramp.cutPath();
        } else {
            addSafeHeightSegmentTo(gcodePath, coordinates.get(0), isFirst);
            gcodePath.addSegment(SegmentType.POINT, coordinates.get(0));
        }

        toMotionSegments(cutPath, source.getFeedRate()).forEach(gcodePath::addSegment);
        lastPosition = cutPath.get(cutPath.size() - 1);
    }

    /**
     * Whether the runs of this tool path are cut at a constant depth, which is what makes it
     * possible to ramp down into them.
     */
    protected boolean supportsRamping() {
        return true;
    }

    private Optional<RampedPath> toRampedPath(List<PartialPosition> coordinates, Cuttable source) {
        PartialPosition start = coordinates.get(0);
        if (!supportsRamping() || source.getPlungeType() != PlungeType.LINEAR_RAMP || !start.hasZ()) {
            return Optional.empty();
        }

        double depth = -start.getZ();
        return LinearRamp.create(coordinates, lastPosition, previousDepth(depth), depth, source.getFeedRate());
    }

    /**
     * The depth that the pass above the given depth has already cleared the material down to, which
     * is where the tool starts engaging the material again. A last pass that only covers part of a
     * depth per pass makes this end up in already cleared material, which only costs a bit of time.
     */
    private double previousDepth(double depth) {
        return Math.max(getStartDepth(), depth - settings.getDepthPerPass());
    }

    /**
     * Converts the coordinates of a single run into the segments cutting it. The first coordinate is
     * left out, since it has already been reached by plunging down to it.
     */
    protected List<Segment> toMotionSegments(List<PartialPosition> coordinates, int feedRate) {
        if (settings.getArcFitting() && settings.getFlatnessPrecision() > 0) {
            // Arcs are held to the same precision the geometry was flattened with, so that a single
            // setting describes how far the tool path may stray from the design
            double precision = settings.getFlatnessPrecision();
            return new ArcFitter(precision, precision).fit(coordinates, feedRate);
        }

        return coordinates.stream()
                .skip(1)
                .map(coordinate -> new Segment(SegmentType.LINE, coordinate, null, null, feedRate))
                .toList();
    }


    public GcodePath toGcodePath() {
        GcodePath gcodePath = new GcodePath();
        appendGcodePath(gcodePath, settings);
        return gcodePath;
    }
}
