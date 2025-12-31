package com.willwinder.ugs.nbp.designer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class GeometryUtils {

    /**
     * Clips a LineString (with exactly 2 coordinates) to the given envelope.
     * Returns a new LineString or null if completely outside.
     */
    public static LineString clipLineToEnvelope(LineString line, Envelope envelope) {
        if (line.getNumPoints() != 2) {
            throw new IllegalArgumentException("LineString must have exactly 2 points.");
        }

        Coordinate p0 = line.getCoordinateN(0);
        Coordinate p1 = line.getCoordinateN(1);

        double[] clipped = clipLine(p0.x, p0.y, p1.x, p1.y, envelope);
        if (clipped == null) {
            return null;
        }

        Coordinate start = new Coordinate(clipped[0], clipped[1]);
        Coordinate end = new Coordinate(clipped[2], clipped[3]);

        GeometryFactory gf = line.getFactory();
        return gf.createLineString(new Coordinate[]{start, end});
    }

    // Internal Liang–Barsky clipper
    private static double[] clipLine(double x0, double y0, double x1, double y1, Envelope env) {
        double dx = x1 - x0;
        double dy = y1 - y0;

        T t = new T(0.0, 1.0);

        if (!clipTest(-dx, x0 - env.getMinX(), t)) return null; // x >= minX
        if (!clipTest(dx, env.getMaxX() - x0, t)) return null; // x <= maxX
        if (!clipTest(-dy, y0 - env.getMinY(), t)) return null; // y >= minY
        if (!clipTest(dy, env.getMaxY() - y0, t)) return null; // y <= maxY

        if (t.t0 > t.t1) return null;

        double sx = x0 + t.t0 * dx;
        double sy = y0 + t.t0 * dy;
        double ex = x0 + t.t1 * dx;
        double ey = y0 + t.t1 * dy;

        return new double[]{sx, sy, ex, ey};
    }

    private static boolean clipTest(double p, double q, T t) {
        if (p == 0) {
            return q >= 0;
        }

        double r = q / p;

        if (p < 0) {
            if (r > t.t1) return false;
            if (r > t.t0) t.t0 = r;
        } else { // p > 0
            if (r < t.t0) return false;
            if (r < t.t1) t.t1 = r;
        }

        return true;
    }

    // Helper mutable holder for t0/t1
    private static class T {
        double t0, t1;

        T(double t0, double t1) {
            this.t0 = t0;
            this.t1 = t1;
        }
    }
}

