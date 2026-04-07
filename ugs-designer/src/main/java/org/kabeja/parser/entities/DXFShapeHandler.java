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
package org.kabeja.parser.entities;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFShape;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFShapeHandler extends AbstractEntityHandler {
    public final static int GROUPCODE_NAME = 2;
    public final static int GROUPCODE_SIZE = 40;
    public final static int GROUPCODE_SCALE_X = 41;
    public final static int GROUPCODE_OBLIQUE_ANGLE = 51;
    protected DXFShape shape;

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#getDXFEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_SHAPE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#endDXFEntity()
     */
    public void endDXFEntity() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#getDXFEntity()
     */
    public DXFEntity getDXFEntity() {
        return shape;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#parseGroup(int,
     *      org.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_START_X:
            shape.getInsertPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            shape.getInsertPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            shape.getInsertPoint().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_OBLIQUE_ANGLE:
            shape.setObliqueAngle(value.getDoubleValue());

            break;

        case GROUPCODE_ROTATION_ANGLE:
            shape.setRotation(value.getDoubleValue());

            break;

        case GROUPCODE_NAME:
            shape.setName(value.getValue());

            break;

        case GROUPCODE_SIZE:
            shape.setHeight(value.getDoubleValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, shape);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.entities.DXFEntityHandler#startDXFEntity()
     */
    public void startDXFEntity() {
        shape = new DXFShape();
    }
}
