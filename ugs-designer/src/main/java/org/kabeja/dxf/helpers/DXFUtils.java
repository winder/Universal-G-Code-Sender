/*
 Copyright 2005 Simon Mieth

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.kabeja.dxf.helpers;

import java.util.ArrayList;

import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.math.MathUtils;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFUtils {
    /**
     *
     */
    public DXFUtils() {
        super();
    }

    public static double distance(Point start, Point end) {
        double length;
        length = Math.sqrt(Math.pow((end.getX() - start.getX()), 2) +
                Math.pow((end.getY() - start.getY()), 2));

        return length;
    }

    public static double rotateAngleX(Point start, Point end) {
        if (end.getY() == start.getY()) {
            return 0.0;
        }

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();

        return Math.toDegrees(Math.atan(1 / (dy / dx)));
    }

    public static double vectorValue(double[] x) {
        double v = 0.0;

        for (int i = 0; i < x.length; i++) {
            v += (x[i] * x[i]);
        }

        return Math.sqrt(v);
    }

    public static Point scalePoint(Point p, double scale) {
        Point r = new Point();
        r.setX(p.getX() * scale);
        r.setY(p.getY() * scale);
        r.setZ(p.getZ() * scale);

        return r;
    }

    public static Point getPointFromParameterizedLine(Point basePoint,
        Vector direction, double parameter) {
        Point r = scalePoint(direction, parameter);

        r.setX(r.getX() + basePoint.getX());
        r.setY(r.getY() + basePoint.getY());
        r.setZ(r.getZ() + basePoint.getZ());

        return r;
    }

    public static void reverseDXFLine(DXFLine line) {
        Point start = line.getStartPoint();
        line.setStartPoint(line.getEndPoint());
        line.setEndPoint(start);
    }

    public static void reverseDXFPolyline(DXFPolyline pline) {
        ArrayList list = new ArrayList();
        double bulge = 0;
        int size = pline.getVertexCount();

        for (int i = 0; i < size; i++) {
            DXFVertex v = pline.getVertex(0);
            double b = v.getBulge();

            if (b != 0) {
                v.setBulge(0);
            }

            //the predecessor becomes the reversed bulge
            if (bulge != 0.0) {
                v.setBulge(bulge * (-1.0));
            }

            bulge = b;

            list.add(v);
            pline.removeVertex(0);
        }

        // reverse now
        for (int i = 1; i <= size; i++) {
            pline.addVertex((DXFVertex) list.get(size - i));
        }
    }

    public static double getArcRadius(DXFVertex start, DXFVertex end) {
        double alpha = 4 * Math.atan(Math.abs(start.getBulge()));
        double l = MathUtils.distance(start.getPoint(), end.getPoint());
        double r = l / (2 * Math.sin(alpha / 2));

        return r;
    }

    /**
     * Tests if the two points are the same for a given radius. In other words
     * the distance between the two points is lower then the radius.
     *
     * @param p1
     * @param p2
     * @param radius
     * @return
     */
    public static boolean equals(Point p1, Point p2, double radius) {
        return distance(p1, p2) < radius;

        // if (Math.abs(p1.getX() - p2.getX()) <= radius
        // && Math.abs(p1.getY() - p2.getY()) <= radius)
        // return Math.abs(p1.getZ() - p2.getZ()) <= radius;

        // return false;
    }
}
