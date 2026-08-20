package com.willwinder.ugs.designer.utils;

import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.GEOMETRY_FACTORY;
import static com.willwinder.universalgcodesender.utils.MathUtils.liangBarskyClipLine;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.awt.geom.Rectangle2D;

public class GeometryUtils {

    /**
     * Clips a LineString (with exactly 2 coordinates) to the given envelope.
     * @return a new LineString or null if completely outside.
     */
    public static LineString clipLineToEnvelope(LineString line, Envelope envelope) {
        if (line.getNumPoints() != 2) {
            throw new IllegalArgumentException("LineString must have exactly 2 points.");
        }

        Coordinate p0 = line.getCoordinateN(0);
        Coordinate p1 = line.getCoordinateN(1);

        // Allow for tiny floating-point overshoots like 251.00000000000003
        final double epsilon = 1e-9;
        Rectangle2D expandedEnvelope = new Rectangle2D.Double(
                envelope.getMinX() - epsilon,
                envelope.getMinY() - epsilon,
                envelope.getWidth() + (2 * epsilon),
                envelope.getHeight() + (2 * epsilon)
        );

        Point2D[] clipped = liangBarskyClipLine(
                new Point2D.Double(p0.x, p0.y),
                new Point2D.Double(p1.x, p1.y),
                expandedEnvelope
        );

        if (clipped == null) {
            return null;
        }

        Coordinate start = new Coordinate(clipped[0].getX(), clipped[0].getY());
        Coordinate end = new Coordinate(clipped[1].getX(), clipped[1].getY());

        GeometryFactory gf = line.getFactory();
        return gf.createLineString(new Coordinate[]{start, end});
    }

    /**
     * Returns the range of offsets along the pass normal that covers the whole envelope for the
     * given angle. The offset is measured from the envelope's minimum corner, matching
     * {@link #generateLineString(Envelope, double, double)}.
     *
     * @param envelope       the area that the passes should cover
     * @param angleInDegrees the angle of the passes
     * @return the smallest and the largest offset, in that order
     */
    public static double[] getOffsetRange(Envelope envelope, double angleInDegrees) {
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

    public static LineString generateLineString(Envelope envelope, double offsetAlongNormal, double angleInDegrees) {
        // Convert angle to radians
        double radians = Math.toRadians(-angleInDegrees);

        // Direction vector for the toolpath line
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);

        double epsilon = 1e-12;
        if (Math.abs(dx) < epsilon) {
            dx = 0;
        }

        if (Math.abs(dy) < epsilon) {
            dy = 0;
        }

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
        return lineString;
    }
}
