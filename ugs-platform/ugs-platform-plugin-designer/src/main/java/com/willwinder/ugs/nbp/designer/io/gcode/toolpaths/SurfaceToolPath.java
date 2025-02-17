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
import org.locationtech.jts.geom.MultiPoint;

import java.awt.geom.Area;
import java.util.List;

public class SurfaceToolPath extends AbstractToolPath {
    private final Cuttable source;

    public SurfaceToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private List<Geometry> getGeometries() {
        Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
        Geometry shell = geometry.buffer((settings.getToolDiameter() * (source.getOffsetToolPercent() / 100d)));
        return List.of(shell.getEnvelope());
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
    }

    private void addGeometriesToGcodePath(GcodePath gcodePath, Settings settings, Geometry g, Envelope envelope, double currentDepth, double stepOver) {
        double currentY = envelope.getMinY();
        while (currentY <= envelope.getMaxY()) {
            LineString lineString = getGeometryFactory().createLineString(new Coordinate[]{
                    new CoordinateXY(envelope.getMinX(), currentY),
                    new CoordinateXY(envelope.getMaxX(), currentY),
            });

            addLineIntersectionSegments(gcodePath, g, lineString, currentDepth, settings.getSafeHeight());
            currentY += stepOver;
        }
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
                gcodePath.addSegment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setZ(0d).build());
                gcodePath.addSegment(SegmentType.POINT, PartialPosition.builder(UnitUtils.Units.MM).setZ(-currentDepth).build());
                gcodePath.addSegment(SegmentType.LINE, endPosition, source.getFeedRate());
            }
        }
    }
}
