package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.google.common.collect.Lists;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;

import java.awt.geom.Area;
import java.util.List;

public class LaserFillToolPath extends AbstractToolPath {
    private final Cuttable source;

    public LaserFillToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private List<Geometry> getGeometries() {
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
            return List.of(geometry);
        } else {
            return ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
        }
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        List<Geometry> geometries = getGeometries();
        geometries.forEach(g -> {
            Envelope envelope = g.getEnvelopeInternal();

            int currentPass = 0;
            while (currentPass < source.getPasses()) {
                currentPass++;

                boolean reverse = false;
                double currentY = envelope.getMinY();
                while (currentY <= envelope.getMaxY()) {
                    LineString lineString = getGeometryFactory().createLineString(new Coordinate[]{
                            new CoordinateXY(envelope.getMinX(), currentY),
                            new CoordinateXY(envelope.getMaxX(), currentY),
                    });

                    addLineIntersectionSegments(gcodePath, g, lineString, reverse);
                    currentY += settings.getLaserDiameter();
                    reverse = !reverse;
                }
            }
        });
    }

    private static void addLineIntersectionSegments(GcodePath gcodePath, Geometry geometry, LineString lineString, boolean reverse) {
        Geometry intersection = geometry.intersection(lineString);

        // If the intersection is a multipoint we should not connect the points with a line
        if (intersection instanceof MultiPoint) {
            return;
        }

        List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(intersection);
        List<PartialPosition> partialPosition = geometryCoordinates.stream()
                .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

        if (partialPosition.size() > 1) {
            if (reverse) {
                partialPosition = Lists.reverse(partialPosition);
            }

            for (int i = 0; i + 1 < partialPosition.size(); i += 2) {
                gcodePath.addSegment(SegmentType.MOVE, partialPosition.get(i));
                gcodePath.addSegment(SegmentType.LINE, partialPosition.get(i + 1));
            }
        }
    }
}
