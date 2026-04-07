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


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFRay extends DXFEntity {
    protected Point basePoint = new Point();
    protected Vector direction = new Vector();

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.dxf.DXFEntity#getBounds()
     */
    public Bounds getBounds() {
        // we will only add the base point
        //the end is infinite
        Bounds bounds = new Bounds();
        bounds.addToBounds(basePoint);

        return bounds;
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.dxf.DXFEntity#getType()
     */
    public String getType() {
        return DXFConstants.ENTITY_TYPE_RAY;
    }

    /**
     * @return Returns the basePoint.
     */
    public Point getBasePoint() {
        return basePoint;
    }

    /**
     * @param basePoint The basePoint to set.
     */
    public void setBasePoint(Point basePoint) {
        this.basePoint = basePoint;
    }

    /**
     * @return Returns the direction.
     */
    public Vector getDirection() {
        return direction;
    }

    /**
     * @param direction The direction to set.
     */
    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    public double getLength() {
        return 0;
    }
}
