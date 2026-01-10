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

import com.willwinder.ugs.nbp.designer.GeometryUtils;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.GEOMETRY_FACTORY;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.geom.Rectangle2D;

public class SurfaceToolPath extends AbstractToolPath {
    private final Cuttable source;
    private final double toolPathAngle;

    public SurfaceToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
        this.toolPathAngle = source.getToolPathAngle();
    }

    private Envelope getEnvelope() {
        Rectangle2D bounds = source.getShape().getBounds2D();
        double toolRadius = settings.getToolDiameter() / 2.0;
        double leadInMm = (settings.getToolDiameter() * (source.getLeadInPercent() / 100d)) - toolRadius;
        return new Envelope(bounds.getMinX() - leadInMm, bounds.getMaxX() + leadInMm, bounds.getMinY() - leadInMm, bounds.getMaxY() + leadInMm);
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        double stepOver = settings.getToolDiameter() * Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);

        Envelope envelope = getEnvelope();

        double currentDepth = getStartDepth();
        addGeometriesToGcodePath(gcodePath, settings, envelope, currentDepth, stepOver);

        while (currentDepth < getTargetDepth()) {
            currentDepth += settings.getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            addGeometriesToGcodePath(gcodePath, settings, envelope, currentDepth, stepOver);
        }

        addSafeHeightSegment(gcodePath, null, true);
    }

    private void addGeometriesToGcodePath(GcodePath gcodePath, Settings settings, Envelope envelope, double currentDepth, double stepOver) {
        double currentY = envelope.getMinY() - envelope.getHeight();

        while (currentY < envelope.getMaxY() + envelope.getHeight()) {
            addLineSegment(gcodePath, settings, envelope, currentDepth, currentY);
            currentY += stepOver;
        }
    }

    private void addLineSegment(
            GcodePath gcodePath,
            Settings settings,
            Envelope envelope,
            double currentDepth,
            double offsetAlongNormal) {

        // Convert angle to radians
        double radians = Math.toRadians(-this.toolPathAngle);

        // Direction vector for the toolpath line
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);

        // Normal vector (perpendicular) for stepping passes
        double nx = -dy;
        double ny = dx;

        // Create a point on the pass using the offset along the normal vector
        double px = envelope.getMinX() + nx * offsetAlongNormal;
        double py = envelope.getMinY() + ny * offsetAlongNormal;

        // Create a long segment centered at that point in direction (dx, dy)
        // The value is intentionally large so clipping will cut it down
        double far = envelope.getWidth() * envelope.getHeight();
        double sx = px - dx * far;
        double sy = py - dy * far;
        double ex = px + dx * far;
        double ey = py + dy * far;

        LineString lineString = new LineString(new CoordinateArraySequence(new Coordinate[]{new Coordinate(sx, sy), new Coordinate(ex, ey)}), GEOMETRY_FACTORY);
        lineString = GeometryUtils.clipLineToEnvelope(lineString, envelope);

        if (lineString == null) {
            return;
        }

        double safeHeight = (-getStartDepth()) + settings.getSafeHeight();
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
}
