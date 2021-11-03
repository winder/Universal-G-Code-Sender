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

import java.util.Map;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.TransformContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFShape extends DXFEntity {
    protected Point insertPoint = new Point();
    protected double rotation = 0.0;
    protected double height = 0.0;
    protected double scaleFactor = 1.0;
    protected double obliqueAngle = 0.0;
    protected String name = "";

    /* (non-Javadoc)
     * @see org.kabeja.dxf.DXFEntity#toSAX(org.xml.sax.ContentHandler, java.util.Map)
     */
    public void toSAX(ContentHandler handler, Map svgContext, DXFEntity entity,
        TransformContext transformContext) throws SAXException {
    }

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
        return DXFConstants.ENTITY_TYPE_SHAPE;
    }

    /**
     * @return Returns the height.
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param height The height to set.
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return Returns the insertPoint.
     */
    public Point getInsertPoint() {
        return insertPoint;
    }

    /**
     * @param insertPoint The insertPoint to set.
     */
    public void setInsertPoint(Point insertPoint) {
        this.insertPoint = insertPoint;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the obliqueAngle.
     */
    public double getObliqueAngle() {
        return obliqueAngle;
    }

    /**
     * @param obliqueAngle The obliqueAngle to set.
     */
    public void setObliqueAngle(double obliqueAngle) {
        this.obliqueAngle = obliqueAngle;
    }

    /**
     * @return Returns the rotation.
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * @param rotation The rotation to set.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * @return Returns the scaleFactor.
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * @param scaleFactor The scaleFactor to set.
     */
    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public double getLength() {
        return 0;
    }
}
