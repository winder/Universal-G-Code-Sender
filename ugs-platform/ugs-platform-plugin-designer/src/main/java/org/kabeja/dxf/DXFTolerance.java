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
public class DXFTolerance extends DXFEntity {
    protected Point insertionPoint = new Point();
    protected String styleNameID = "";
    protected String text;
    protected Vector xaxisDirection = new Vector();

    /* (non-Javadoc)
     * @see org.kabeja.dxf.DXFEntity#getBounds()
     */
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        bounds.setValid(false);

        return bounds;
    }

    /* (non-Javadoc)
     * @see org.kabeja.dxf.DXFEntity#getType()
     */
    public String getType() {
        // TODO Auto-generated method stub
        return DXFConstants.ENTITY_TYPE_TOLERANCE;
    }

    /**
     * @return Returns the insertionPoint.
     */
    public Point getInsertionPoint() {
        return insertionPoint;
    }

    /**
     * @param insertionPoint The insertionPoint to set.
     */
    public void setInsertionPoint(Point insertionPoint) {
        this.insertionPoint = insertionPoint;
    }

    /**
     * @return Returns the styleID.
     */
    public String getStyleID() {
        return styleNameID;
    }

    /**
     * @param styleNameID The styleID to set.
     */
    public void setStyleID(String styleNameID) {
        this.styleNameID = styleNameID;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }

    /**
     * @param text The text to set.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return Returns the xaxisDirection.
     */
    public Vector getXaxisDirection() {
        return xaxisDirection;
    }

    /**
     * @param xaxisDirection The xaxisDirection to set.
     */
    public void setXaxisDirection(Vector xaxisDirection) {
        this.xaxisDirection = xaxisDirection;
    }

    public double getLength() {
        return 0;
    }
}
