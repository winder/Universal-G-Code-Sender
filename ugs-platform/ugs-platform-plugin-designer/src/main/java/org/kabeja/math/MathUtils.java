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
package org.kabeja.math;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class MathUtils {
    public final static double DISTANCE_DELTA = 0.00001;

    /**
     * Calculate the scalar product of vector a and vector b
     *
     * @param a
     * @param b
     * @return the result of the scalar product
     */
    public static double scalarProduct(Vector a, Vector b) {
        double r = (a.getX() * b.getX()) + (a.getY() * b.getY()) +
            (a.getZ() * b.getZ());

        return r;
    }

    /**
     * Returns the absalute value (or length) of the vector
     *
     * @param v
     * @return
     */
    public static double absoluteValue(Vector v) {
        double r = Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getY(), 2) +
                Math.pow(v.getZ(), 2));

        return r;
    }

    /**
     * Calculate the cross product of 2 vectors.
     *
     * @param a
     * @param b
     * @return a new vector as result of the cross product of a and b
     */
    public static Vector crossProduct(Vector a, Vector b) {
        Vector r = new Vector();
        r.setX((a.getY() * b.getZ()) - (a.getZ() * b.getY()));
        r.setY((a.getZ() * b.getX()) - (a.getX() * b.getZ()));
        r.setZ((a.getX() * b.getY()) - (a.getY() * b.getX()));

        return r;
    }

    /**
     * Scale a vector with the given value
     *
     * @param a
     *            the vector
     * @param scale
     *            the value to scale
     * @return a new vector scaled with the given
     */
    public static Vector scaleVector(Vector a, double scale) {
        Vector v = new Vector();

        v.setX(a.getX() * scale);
        v.setY(a.getY() * scale);
        v.setZ(a.getZ() * scale);

        return v;
    }

    /**
     * Calculate a point of a straigt line.
     *
     * @param a
     *            the startpoint of the straight line
     * @param direction
     *            the direction vector of the straight line
     * @param parameter
     *            the parameter
     * @return a new point
     */
    public static Point getPointOfStraightLine(Point a, Vector direction,
        double parameter) {
        // p = a + v*scale
        Point p = new Point();
        Vector v = scaleVector(direction, parameter);

        p.setX(a.getX() + v.getX());
        p.setY(a.getY() + v.getY());
        p.setZ(a.getZ() + v.getZ());

        return p;
    }

    /**
     * Calculate the vector from point a to point b
     *
     * @param a
     * @param b
     * @return the vector from a to b
     */
    public static Vector getVector(Point a, Point b) {
        Vector v = new Vector();

        v.setX(b.getX() - a.getX());
        v.setY(b.getY() - a.getY());
        v.setZ(b.getZ() - a.getZ());

        return v;
    }

    public static Point getIntersection(Point a, Vector u, Point b, Vector v) {
        //u = normalize(u);
        //v = normalize(v);
        Vector n = crossProduct(u, v);
        Vector m = crossProduct(getVector(a, b), v);
        double s = 0;

        if (n.getZ() != 0.0) {
            s = m.getZ() / n.getZ();
        } else if (n.getY() != 0.0) {
            s = m.getY() / n.getY();
        } else if (n.getX() != 0.0) {
            s = m.getX() / n.getX();
        }

        Point p = getPointOfStraightLine(a, u, s);

        return p;
    }

    public static double distance(Point start, Point end) {
        double length = Math.sqrt(Math.pow((end.getX() - start.getX()), 2) +
                Math.pow((end.getY() - start.getY()), 2) +
                Math.pow((end.getZ() - start.getZ()), 2));

        return length;
    }

    /**
     * Calculate the angle between vector a vector b
     *
     * @param a
     * @param b
     * @return the angle in radian
     */
    public static double getAngle(Vector a, Vector b) {
        double cos = scalarProduct(a, b) / (absoluteValue(a) * absoluteValue(b));

        return Math.acos(cos);
    }

    /**
     * Rotate the given point around centerpoint with the given angle in X-Y
     * plane.
     *
     * @param p
     *            the point to rotate
     * @param center
     *            the centerpoint
     * @param angle
     *            in radian
     * @return the rotated point
     */
    public static Point rotatePointXY(Point p, Point center, double angle) {
        Point r = new Point();
        r.setX((center.getX() + (Math.cos(angle) * (p.getX() - center.getX()))) -
            ((p.getY() - center.getY()) * Math.sin(angle)));
        r.setY((center.getY() + (Math.cos(angle) * (p.getY() - center.getY()))) -
            ((p.getX() - center.getX()) * Math.sin(angle)));
        r.setZ(p.getZ());

        return r;
    }

    public static Vector normalize(Vector v) {
        double l = absoluteValue(v);

        return scaleVector(v, (1 / l));
    }

    /**
     * Returns the qaudrant:<br/>
     * 0,1,2 or 3
     * @param p
     * @param center
     * @return
     */
    public static int getQuadrant(Point p, Point center) {
        if (p.getX() < center.getX()) {
            if (p.getY() >= center.getY()) {
                return 1;
            } else {
                return 2;
            }
        } else {
            if (p.getY() >= center.getY()) {
                return 0;
            } else {
                return 3;
            }
        }
    }

    /**
     * Returns the qaudrant for the given angle:<br/>
     * 0,1,2 or 3
     * @param angle in degree
     * @return the quadrant
     */
    public static int getQuadrant(double angle) {
        if (angle > 360) {
            angle = angle - (Math.floor(angle / 360) * 360);
        }

        if ((angle >= 0) && (angle < 90)) {
            return 0;
        } else if ((angle >= 90) && (angle < 180)) {
            return 1;
        } else if ((angle >= 180) && (angle < 270)) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Returns the angle of the vector again the x-axis
     * @param v the vector
     * @return the angle in degree
     */
    public static double getDirectionAngle(Vector v) {
        double l = absoluteValue(v);

        if ((v.getX() < 0) && (v.getY() < 0)) {
            return (Math.toDegrees(Math.acos(Math.abs(v.getX() / l))) + 180);
        } else if ((v.getX() == 0.0) || (v.getY() < 0)) {
            return Math.toDegrees(Math.asin(v.getY() / l));
        } else {
            return Math.toDegrees(Math.acos(v.getX() / l));
        }
    }

    /**
     * Invert the direction of the given vector
     * @param the vector to invert
     * @return new inverted vector
     */
    public static Vector invertDirection(Vector v) {
        return scaleVector(v, -1);
    }

    public static double[][] multiplyMatrixByMatrix(double[][] a, double[][] b)
        throws IllegalArgumentException {
        if (a[0].length != b.length) {
            throw new IllegalArgumentException(
                "Cannot multiply a with b, columns of a != rows of b. ");
        }

        double[][] c = new double[a.length][b[0].length];

        for (int i = 0; i < a.length; i++) {
            for (int x = 0; x < b[0].length; x++) {
                for (int y = 0; y < b.length; y++) {
                    c[i][x] += (a[i][y] * b[x][y]);
                }
            }
        }

        return c;
    }

    public static double[] multiplyMatrixByVector(double[][] a, double[] v)
        throws IllegalArgumentException {
        if (a[0].length != v.length) {
            throw new IllegalArgumentException(
                "Cannot multiply a with v, columns of a != rows of v. ");
        }

        double[] r = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            for (int x = 0; x < v.length; x++) {
                r[i] += (a[i][x] * v[x]);
            }
        }

        return r;
    }

    /**
     * Substracts a vector from other vector.
     * @param a the minuend
     * @param b the subtrahend
     * @return the difference as vector
     */
    public static Vector subtractVectorByVector(Vector a, Vector b) {
        Vector result = new Vector();
        result.setX(a.getX() - b.getX());
        result.setY(a.getY() - b.getY());
        result.setZ(a.getZ() - b.getZ());

        return result;
    }

    public static Vector addVectorToVector(Vector a, Vector b) {
        Vector result = new Vector();
        result.setX(a.getX() + b.getX());
        result.setY(a.getY() + b.getY());
        result.setZ(a.getZ() + b.getZ());

        return result;
    }
}
