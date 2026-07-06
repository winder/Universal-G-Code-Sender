/*
    Copyright 2025-2026 Joacim Breiler

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
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import static com.willwinder.ugs.designer.utils.GeometryUtils.generateLineString;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SurfaceToolPath extends AbstractToolPath {
    private final Cuttable source;
    private final ToolPathDirection toolPathDirection;

    private enum PassDirection {
        FORWARD,
        REVERSE
    }

    public SurfaceToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
        this.toolPathDirection = source.getToolPathDirection();
    }

    private Envelope getEnvelope() {
        Rectangle2D bounds = source.getShape().getBounds2D();
        double toolRadius = settings.getToolDiameter() / 2.0;
        double leadMm = settings.getToolDiameter() * (source.getLeadInPercent() / 100d);

        if (toolPathDirection == ToolPathDirection.VERTICAL) {
            return new Envelope(
                    bounds.getMinX() + toolRadius,
                    bounds.getMaxX() - toolRadius,
                    bounds.getMinY() + toolRadius - leadMm,
                    bounds.getMaxY() - toolRadius + leadMm
            );
        }

        return new Envelope(
                bounds.getMinX() + toolRadius - leadMm,
                bounds.getMaxX() - toolRadius + leadMm,
                bounds.getMinY() + toolRadius,
                bounds.getMaxY() - toolRadius
        );
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        double maxStepOver = settings.getToolDiameter() * Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);
        Envelope envelope = getEnvelope();
        List<Double> offsets = getPassOffsets(maxStepOver);

        double currentDepth = getStartDepth();
        addGeometriesToGcodePath(gcodePath, settings, envelope, currentDepth, offsets);

        while (currentDepth < getTargetDepth()) {
            currentDepth += settings.getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            addGeometriesToGcodePath(gcodePath, settings, envelope, currentDepth, offsets);
        }

        addSafeHeightSegment(gcodePath, null, true);
    }

    private void addGeometriesToGcodePath(GcodePath gcodePath, Settings settings, Envelope envelope, double currentDepth, List<Double> offsets) {
        Direction direction = source.getDirection();
        if (direction == Direction.CLIMB) {
            generateClimbPath(gcodePath, settings, envelope, currentDepth, offsets);
        } else if (direction == Direction.CONVENTIONAL) {
            generateConventinalPath(gcodePath, settings, envelope, currentDepth, offsets);
        } else if (direction == Direction.BOTH) {
            generateBothPath(gcodePath, settings, envelope, currentDepth, offsets);
        }
    }

    private void generateBothPath(GcodePath gcodePath, Settings settings, Envelope envelope, double currentDepth, List<Double> offsets) {
        boolean reverse = false;
        boolean isFirstSegment = true;

        for (double offset : offsets) {
            if (addReversibleLineSegment(gcodePath, settings, envelope, currentDepth, offset, reverse ? PassDirection.REVERSE : PassDirection.FORWARD, isFirstSegment)) {
                isFirstSegment = false;
            }
            reverse = !reverse;
        }
    }

    private void generateConventinalPath(GcodePath gcodePath, Settings settings, Envelope envelope, double currentDepth, List<Double> offsets) {
        for (int i = offsets.size() - 1; i >= 0; i--) {
            addSingleLineSegment(gcodePath, settings, envelope, currentDepth, offsets.get(i));
        }
    }

    private void generateClimbPath(GcodePath gcodePath, Settings settings, Envelope envelope, double currentDepth, List<Double> offsets) {
        for (double offset : offsets) {
            addSingleLineSegment(gcodePath, settings, envelope, currentDepth, offset);
        }
    }

    private List<Double> getPassOffsets(double maxStepOver) {
        Rectangle2D bounds = source.getShape().getBounds2D();
        double toolDiameter = settings.getToolDiameter();
        double perpendicularDimension = toolPathDirection == ToolPathDirection.VERTICAL ? bounds.getWidth() : bounds.getHeight();

        int passCount = Math.max(1, (int) Math.ceil(perpendicularDimension / maxStepOver));
        double centerlineSpan = Math.max(0, perpendicularDimension - toolDiameter);

        List<Double> offsets = new ArrayList<>();
        if (passCount == 1) {
            offsets.add(centerlineSpan / 2.0);
            return offsets;
        }

        double spacing = centerlineSpan / (passCount - 1);
        for (int i = 0; i < passCount; i++) {
            offsets.add(i * spacing);
        }

        return offsets;
    }

    private void addSingleLineSegment(
            GcodePath gcodePath,
            Settings settings,
            Envelope envelope,
            double currentDepth,
            double offsetAlongNormal) {

        LineString lineString = generateLineString(envelope, offsetAlongNormal, getToolPathAngle());
        if (lineString == null) return;

        double safeHeight = calculateSafeHeight(settings);
        Coordinate startCoord = lineString.getCoordinateN(0);
        Coordinate endCoord = lineString.getCoordinateN(1);

        PartialPosition start = PartialPosition.builder(UnitUtils.Units.MM)
                .setX(startCoord.x).setY(startCoord.y).build();
        PartialPosition end = PartialPosition.builder(UnitUtils.Units.MM)
                .setX(endCoord.x).setY(endCoord.y).build();

        gcodePath.addSegment(
                SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setZ(safeHeight).build()
        );
        gcodePath.addSegment(SegmentType.MOVE, start);
        gcodePath.addSegment(
                SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setZ(-currentDepth).build()
        );
        gcodePath.addSegment(SegmentType.LINE, end, source.getFeedRate());
    }

    private double getToolPathAngle() {
        return toolPathDirection == ToolPathDirection.VERTICAL ? 90d : 0d;
    }

    private double calculateSafeHeight(Settings settings) {
        return (-getStartDepth()) + settings.getSafeHeight();
    }

    private boolean addReversibleLineSegment(
            GcodePath gcodePath,
            Settings settings,
            Envelope envelope,
            double currentDepth,
            double offsetAlongNormal,
            PassDirection passDirection,
            boolean isFirstSegment) {

        LineString lineString = generateLineString(envelope, offsetAlongNormal, getToolPathAngle());
        if (lineString == null) return false;

        Coordinate startCoord = lineString.getCoordinateN(0);
        Coordinate endCoord = lineString.getCoordinateN(1);
        if (passDirection == PassDirection.REVERSE) {
            startCoord = lineString.getCoordinateN(1);
            endCoord = lineString.getCoordinateN(0);
        }

        PartialPosition start = PartialPosition.builder(UnitUtils.Units.MM)
                .setX(startCoord.x).setY(startCoord.y).build();
        PartialPosition end = PartialPosition.builder(UnitUtils.Units.MM)
                .setX(endCoord.x).setY(endCoord.y).build();

        if (isFirstSegment) {
            gcodePath.addSegment(
                    SegmentType.MOVE,
                    PartialPosition.builder(UnitUtils.Units.MM).setZ(calculateSafeHeight(settings)).build()
            );
        }

        gcodePath.addSegment(isFirstSegment ? SegmentType.MOVE : SegmentType.LINE, start);
        gcodePath.addSegment(
                isFirstSegment ? SegmentType.MOVE : SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setZ(-currentDepth).build()
        );
        gcodePath.addSegment(SegmentType.LINE, end, source.getFeedRate());
        return true;
    }
}