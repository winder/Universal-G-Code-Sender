/*
   Copyright 2008 Simon Mieth

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

import org.kabeja.dxf.DXFExtrusion;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;


public class ParametricPlane {
    protected Point base;
    protected Vector directionX;
    protected Vector directionY;
    protected Vector normal;

    /**
     *
     * @param basePoint
     *            The base point of this plane
     * @param directionX
     *            the x direction of this plane
     * @param directionY
     *            the y direction of this plane
     * @param normal
     *            the normal direction of this plane
     */
    public ParametricPlane(Point basePoint, Vector directionX,
        Vector directionY, Vector normal) {
        this.base = basePoint;
        this.directionX = directionX;
        this.directionY = directionY;
        this.normal = normal;
    }

    /**
     *
     * @param basePoint
     *            The base point of this plane
     * @param directionX
     *            the x direction of this plane
     * @param directionY
     *            the y direction of this plane
     */
    public ParametricPlane(Point basePoint, Vector directionX, Vector directionY) {
        this(basePoint, directionX, directionY,
            MathUtils.normalize(MathUtils.crossProduct(directionX, directionY)));
    }

    /**
     * Generates a plane with the base point and uses the vector from base point
     * to b as x direction. The y direction is generated with the cross product
     * of the normal with the x direction.
     *
     * @param basePoint
     * @param b
     * @param normal
     */
    public ParametricPlane(Point basePoint, Point b, Vector normal) {
        this(basePoint, MathUtils.normalize(MathUtils.getVector(basePoint, b)),
            MathUtils.normalize(MathUtils.crossProduct(normal,
                    MathUtils.normalize(MathUtils.getVector(basePoint, b)))),
            normal);
    }

    public ParametricPlane(Point basePoint, Point b, Point c) {
        this(basePoint, MathUtils.normalize(MathUtils.getVector(basePoint, b)),
            MathUtils.normalize(MathUtils.getVector(basePoint, c)));
    }

    public ParametricPlane(DXFExtrusion e) {
        this(new Point(0.0, 0.0, 0.0), e.getDirectionX(), e.getDirectionY(),
            e.getNormal());
    }

    /**
     * Calculate the point in world coordinates for the given parameters
     *
     * @param x
     * @param y
     * @return
     */
    public Point getPoint(double x, double y) {
        Point p = new Point();
        p.setX(this.base.getX() + (this.directionX.getX() * x) +
            (this.directionY.getX() * y));
        p.setY(this.base.getY() + (this.directionX.getY() * x) +
            (this.directionY.getY() * y));
        p.setZ(this.base.getZ() + (this.directionX.getZ() * x) +
            (this.directionY.getZ() * y));

        return p;
    }

    public Point getPoint(Point point) {
        return getPoint(point.getX(), point.getY());
    }

    /**
     * Calculates the plane parameters of the given point relative to the base
     * point of the plane
     *
     * @param p
     * @return double[]{parameter x direction, parameter y direction}
     */
    public double[] getParameter(Point p) {
        double u = 0.0;
        double v = (this.directionX.getY() * this.directionY.getX()) -
            (this.directionX.getX() * this.directionY.getY());

        if (v != 0.0) {
            v = ((p.getY() * this.directionY.getX()) -
                (this.base.getY() * this.directionY.getX()) -
                (this.directionY.getY() * p.getX()) +
                (this.base.getX() * this.directionY.getY())) / v;
        }

        if (this.directionY.getX() != 0.0) {
            u = (p.getX() - this.base.getX() - (this.directionX.getX() * v)) / this.directionY.getX();
        } else if (this.directionY.getY() != 0.0) {
            u = (p.getY() - this.base.getY() - (this.directionX.getY() * v)) / this.directionY.getY();
        } else if (this.directionY.getY() != 0.0) {
            u = (p.getZ() - this.base.getZ() - (this.directionX.getZ() * v)) / this.directionY.getZ();
        }

        return new double[] { v, u };
    }

    /**
     * Determines if the given point lies on the plane.
     *
     * @param p
     *            the point to determine
     * @return true if the point lies on the plane, otherwise false.
     */
    public boolean isOnPlane(Point p) {
        double[] para = this.getParameter(p);
        double v = this.base.getZ() + (this.directionX.getZ() * para[0]) +
            (this.directionY.getZ() * para[1]);

        if (!(Math.abs((p.getZ() - v)) < MathUtils.DISTANCE_DELTA)) {
            return false;
        }

        v = this.base.getY() + (this.directionX.getY() * para[0]) +
            (this.directionY.getY() * para[1]);

        if (!(Math.abs((p.getY() - v)) < MathUtils.DISTANCE_DELTA)) {
            return false;
        }

        v = this.base.getX() + (this.directionX.getX() * para[0]) +
            (this.directionY.getX() * para[1]);

        if (!(Math.abs((p.getX() - v)) < MathUtils.DISTANCE_DELTA)) {
            return false;
        }

        return true;
    }

    public Point getBasePoint() {
        return base;
    }

    public void setBasePoint(Point base) {
        this.base = base;
    }

    public Vector getDirectionX() {
        return directionX;
    }

    public void setDirectionX(Vector directionX) {
        this.directionX = directionX;
        this.normal = MathUtils.crossProduct(this.directionX, this.directionY);
        this.normal.normalize();
    }

    public Vector getDirectionY() {
        return directionY;
    }

    public void setDirectionY(Vector directionY) {
        this.directionY = directionY;
        this.normal = MathUtils.crossProduct(this.directionX, this.directionY);
        this.normal.normalize();
    }

    public Vector getNormal() {
        return normal;
    }
}
