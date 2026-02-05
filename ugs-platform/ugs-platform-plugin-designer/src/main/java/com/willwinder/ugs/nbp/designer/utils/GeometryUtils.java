package com.willwinder.ugs.nbp.designer.utils;

import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.GEOMETRY_FACTORY;
import static com.willwinder.universalgcodesender.utils.MathUtils.liangBarskyClipLine;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.awt.geom.Point2D;
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

        Point2D[] clipped = liangBarskyClipLine(new Point2D.Double(p0.x, p0.y), new Point2D.Double(p1.x, p1.y), new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight()));
        if (clipped == null) {
            return null;
        }

        Coordinate start = new Coordinate(clipped[0].getX(), clipped[0].getY());
        Coordinate end = new Coordinate(clipped[1].getX(), clipped[1].getY());

        GeometryFactory gf = line.getFactory();
        return gf.createLineString(new Coordinate[]{start, end});
    }


    public static LineString generateLineString(Envelope envelope, double offsetAlongNormal, double angleInDegrees) {
        // Convert angle to radians
        double radians = Math.toRadians(-angleInDegrees);

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
        return lineString;
    }
}

