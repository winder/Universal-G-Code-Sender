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
import org.kabeja.math.MathUtils;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFSolid extends DXFEntity {
    protected Point point1 = new Point();
    protected Point point2 = new Point();
    protected Point point3 = new Point();
    protected Point point4 = new Point();

    public DXFSolid() {
    }

    public Bounds getBounds() {
        Bounds bounds = new Bounds();

        bounds.addToBounds(point1);
        bounds.addToBounds(point2);
        bounds.addToBounds(point3);
        bounds.addToBounds(point4);

        return bounds;
    }

    /**
     * @return Returns the point1.
     */
    public Point getPoint1() {
        return point1;
    }

    /**
     * @param point1
     *            The point1 to set.
     */
    public void setPoint1(Point point1) {
        this.point1 = point1;
    }

    /**
     * @return Returns the point2.
     */
    public Point getPoint2() {
        return point2;
    }

    /**
     * @param point2
     *            The point2 to set.
     */
    public void setPoint2(Point point2) {
        this.point2 = point2;
    }

    /**
     * @return Returns the point3.
     */
    public Point getPoint3() {
        return point3;
    }

    /**
     * @param point3
     *            The point3 to set.
     */
    public void setPoint3(Point point3) {
        this.point3 = point3;
    }

    /**
     * @return Returns the point4.
     */
    public Point getPoint4() {
        return point4;
    }

    /**
     * @param point4
     *            The point4 to set.
     */
    public void setPoint4(Point point4) {
        this.point4 = point4;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_SOLID;
    }

    public double getLength() {
        double length = 0.0;
        length += MathUtils.distance(this.point1, this.point2);
        length += MathUtils.distance(this.point2, this.point4);
        length += MathUtils.distance(this.point4, this.point3);
        length += MathUtils.distance(this.point3, this.point1);

        return length;
    }
}
