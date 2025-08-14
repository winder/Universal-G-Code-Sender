/*
    Copyright 2025 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPoint;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class SurfaceToolPath extends AbstractToolPath {
    private final Cuttable source;

    public SurfaceToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private List<Geometry> getGeometries() {
        Rectangle2D bounds = source.getShape().getBounds2D();
        double toolRadius = settings.getToolDiameter() / 2.0;
        double leadInMm = (settings.getToolDiameter() * (source.getLeadInPercent() / 100d)) - toolRadius;
        double leadOutMm = (settings.getToolDiameter() * (source.getLeadOutPercent() / 100d)) - toolRadius;
        LinearRing linearRing = getGeometryFactory().createLinearRing(new Coordinate[]{
                new Coordinate(bounds.getX() - leadInMm, bounds.getY() + toolRadius),
                new Coordinate(bounds.getX() - leadInMm, bounds.getY() + bounds.getHeight() - toolRadius),
                new Coordinate(bounds.getX() + bounds.getWidth() + leadOutMm, bounds.getY() + bounds.getHeight() - toolRadius),
                new Coordinate(bounds.getX() + bounds.getWidth() + leadOutMm, bounds.getY() + toolRadius),
                new Coordinate(bounds.getX() - leadInMm, bounds.getY() + toolRadius),
        });
        return List.of(linearRing.getEnvelope());
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        double stepOver = settings.getToolDiameter() * Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);


        List<Geometry> geometries = getGeometries();
        geometries.forEach(g -> {
            Envelope envelope = g.getEnvelopeInternal();

            double currentDepth = getStartDepth();
            addGeometriesToGcodePath(gcodePath, settings, g, envelope, currentDepth, stepOver);

            while (currentDepth < getTargetDepth()) {
                currentDepth += settings.getDepthPerPass();
                if (currentDepth > getTargetDepth()) {
                    currentDepth = getTargetDepth();
                }

                addGeometriesToGcodePath(gcodePath, settings, g, envelope, currentDepth, stepOver);
            }
        });
        addSafeHeightSegment(gcodePath, null, true);
    }

    private void addGeometriesToGcodePath(GcodePath gcodePath, Settings settings, Geometry g, Envelope envelope, double currentDepth, double stepOver) {
        double currentY = envelope.getMinY();
        double minX = envelope.getMinX();
        double maxX = envelope.getMaxX();

        while (currentY < envelope.getMaxY()) {
            addLineSegment(gcodePath, settings, g, currentDepth, minX, maxX, currentY);
            currentY += stepOver;
        }

        // Add the last line
        if (currentY - stepOver < envelope.getMaxY()) {
            addLineSegment(gcodePath, settings, g, currentDepth, minX, maxX, envelope.getMaxY());
        }
    }

    private void addLineSegment(GcodePath gcodePath, Settings settings, Geometry g, double currentDepth, double minX, double maxX, double currentY) {
        LineString lineString = getGeometryFactory().createLineString(new Coordinate[]{
                new CoordinateXY(minX, currentY),
                new CoordinateXY(maxX, currentY),
        });

        addLineIntersectionSegments(gcodePath, g, lineString, currentDepth, (-getStartDepth()) + settings.getSafeHeight());
    }

    private void addLineIntersectionSegments(GcodePath gcodePath, Geometry geometry, LineString lineString, double currentDepth, double safeHeight) {
        Geometry intersection = geometry.intersection(lineString);

        // If the intersection is a multipoint we should not connect the points with a line
        if (intersection instanceof MultiPoint) {
            return;
        }

        List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(intersection);
        List<PartialPosition> partialPosition = geometryCoordinates.stream()
                .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

        if (partialPosition.size() > 1) {
            for (int i = 0; i + 1 < partialPosition.size(); i += 2) {
                PartialPosition startPosition = partialPosition.get(i);
                PartialPosition endPosition = partialPosition.get(i + 1);

                // Make sure we are working from left to right
                if (startPosition.getX() > endPosition.getX()) {
                    startPosition = partialPosition.get(i + 1);
                    endPosition = partialPosition.get(i);
                }

                gcodePath.addSegment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(safeHeight).build());
                gcodePath.addSegment(SegmentType.MOVE, startPosition);
                gcodePath.addSegment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(-currentDepth).build());
                gcodePath.addSegment(SegmentType.LINE, endPosition, source.getFeedRate());
            }
        }
    }
}
