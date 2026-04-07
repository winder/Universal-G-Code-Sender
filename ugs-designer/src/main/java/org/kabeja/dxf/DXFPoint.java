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


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFPoint extends DXFEntity {
    protected Point p = new Point();

    public DXFPoint() {
    }

    public DXFPoint(double x, double y, double z) {
        this.p.setX(x);
        this.p.setY(y);
        this.p.setZ(z);
    }

    public DXFPoint(Point p) {
        this.p = p;
    }

    /**
     * @return Returns the x.
     */
    public double getX() {
        return this.p.getX();
    }

    /**
     * @param x
     *            The x to set.
     */
    public void setX(double x) {
        this.p.setX(x);
    }

    /**
     * @return Returns the y.
     */
    public double getY() {
        return this.p.getY();
    }

    /**
     * @param y
     *            The y to set.
     */
    public void setY(double y) {
        this.p.setY(y);
    }

    /**
     * @return Returns the z.
     */
    public double getZ() {
        return this.p.getZ();
    }

    /**
     * @param z
     *            The z to set.
     */
    public void setZ(double z) {
        this.p.setZ(z);
    }

    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        bounds.addToBounds(p);

        return bounds;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_POINT;
    }

    public Point getPoint() {
        return this.p;
    }

    public void setPoint(Point p) {
        this.p = p;
    }

    public double getLength() {
        // a point has no length
        return 0;
    }
}
