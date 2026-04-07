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
package org.kabeja.dxf;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;
import org.kabeja.math.MathUtils;


/**
 * This class implements the arbitrary axis algorithm to extract the
 * direction x,y,z of the plane defined by the extrusion.
 *
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 */
public class DXFExtrusion {
    private final static double v = 1.0 / 64.0;
    protected Vector n = new Vector(0.0, 0.0, 1.0);
    protected Vector x;
    protected Vector y;

    /**
     *
     * @return the x value of the extrusion direction.
     */
    public double getX() {
        return n.getX();
    }

    /**
     *
     * Set the x value of the extrusion direction.
     */
    public void setX(double x) {
        n.setX(x);
    }

    /**
     *
     * @return the y value of the extrusion direction.
     */
    public double getY() {
        return n.getY();
    }

    /**
     *
     * Set the x value of the extrusion direction.
     */
    public void setY(double y) {
        n.setY(y);
    }

    /**
     *
     * @return the z value of the extrusion direction.
     */
    public double getZ() {
        return n.getZ();
    }

    /**
     *
     * Set the x value of the extrusion direction.
     */
    public void setZ(double z) {
        n.setZ(z);
    }

    /**
     * Calculate and returns the x direction of the plane.
     * @return
     */
    public Vector getDirectionX() {
        if ((Math.abs(n.getX()) < v) && (Math.abs(n.getY()) < v)) {
            return MathUtils.crossProduct(DXFConstants.DEFAULT_Y_AXIS_VECTOR, n);
        } else {
            return MathUtils.crossProduct(DXFConstants.DEFAULT_Z_AXIS_VECTOR, n);
        }
    }

    /**
     * Calculate the y direction of the plane.
     * @return the calculate y direction
     */
    public Vector getDirectionY() {
        return MathUtils.crossProduct(n, getDirectionX());
    }

    public Point extrudePoint(Point basePoint, double elevation) {
        return MathUtils.getPointOfStraightLine(basePoint, this.n, elevation);
    }

    /**
     * Return the normal direction of the plane.
     * @return
     */
    public Vector getNormal() {
        return n;
    }

    /**
     * @see getNormal()
     * @return
     */
    public Vector getDirectionZ() {
        return n;
    }
}
