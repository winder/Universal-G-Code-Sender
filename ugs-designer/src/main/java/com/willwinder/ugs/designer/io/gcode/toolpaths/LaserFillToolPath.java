package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.google.common.collect.Lists;
import com.willwinder.ugs.designer.entities.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import static com.willwinder.ugs.designer.utils.GeometryUtils.generateLineString;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;

import java.awt.geom.Area;
import java.util.Arrays;
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

        double toolPathAngle = source.getToolPathAngle();
        List<Geometry> geometries = getGeometries();
        geometries.forEach(g -> {
            Envelope envelope = g.getEnvelopeInternal();
            double[] offsetRange = getOffsetRange(envelope, toolPathAngle);

            int currentPass = 0;
            while (currentPass < source.getPasses()) {
                currentPass++;

                boolean reverse = false;
                double currentOffset = offsetRange[0];
                while (currentOffset <= offsetRange[1]) {
                    LineString lineString = generateLineString(envelope, currentOffset, toolPathAngle);
                    if (lineString != null) {
                        addLineIntersectionSegments(gcodePath, g, lineString, reverse);
                    }
                    currentOffset += settings.getLaserDiameter();
                    reverse = !reverse;
                }
            }
        });
    }

    /**
     * Returns the range of offsets along the pass normal that covers the whole envelope for the given angle.
     * The offset is measured from the envelope's minimum corner, matching {@link com.willwinder.ugs.designer.utils.GeometryUtils#generateLineString}.
     */
    private static double[] getOffsetRange(Envelope envelope, double angleInDegrees) {
        double radians = Math.toRadians(-angleInDegrees);
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);

        double normalX = -dy;
        double normalY = dx;

        double width = envelope.getWidth();
        double height = envelope.getHeight();

        double[] projections = {
                0,
                width * normalX,
                height * normalY,
                width * normalX + height * normalY
        };

        double min = Arrays.stream(projections).min().orElse(0);
        double max = Arrays.stream(projections).max().orElse(0);
        return new double[]{min, max};
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
