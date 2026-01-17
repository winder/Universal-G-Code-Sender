package com.willwinder.ugs.nbp.designer.utils;

import static com.willwinder.universalgcodesender.utils.MathUtils.liangBarskyClipLine;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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
}

