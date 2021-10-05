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

import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFSolid;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFSolidHandler extends AbstractEntityHandler {
    public final static int POINT2_X = 11;
    public final static int POINT2_Y = 21;
    public final static int POINT2_Z = 31;
    public final static int POINT3_X = 12;
    public final static int POINT3_Y = 22;
    public final static int POINT3_Z = 32;
    public final static int POINT4_X = 13;
    public final static int POINT4_Y = 23;
    public final static int POINT4_Z = 33;
    protected String ENTITY_NAME = "SOLID";
    protected DXFSolid solid;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#endDXFEntity()
     */
    public void endDXFEntity() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#getDXFEntity()
     */
    public DXFEntity getDXFEntity() {
        return solid;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#getDXFEntityName()
     */
    public String getDXFEntityName() {
        return ENTITY_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#parseGroup(int,
     *      de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        //point 1
        case GROUPCODE_START_X:
            solid.getPoint1().setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            solid.getPoint1().setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            solid.getPoint1().setZ(value.getDoubleValue());

            break;

        //point 2
        case POINT2_X:
            solid.getPoint2().setX(value.getDoubleValue());

            break;

        case POINT2_Y:
            solid.getPoint2().setY(value.getDoubleValue());

            break;

        case POINT2_Z:
            solid.getPoint2().setZ(value.getDoubleValue());

            break;

        //point 3
        case POINT3_X:
            solid.getPoint3().setX(value.getDoubleValue());

            break;

        case POINT3_Y:
            solid.getPoint3().setY(value.getDoubleValue());

            break;

        case POINT3_Z:
            solid.getPoint3().setZ(value.getDoubleValue());

            break;

        //point 4
        case POINT4_X:
            solid.getPoint4().setX(value.getDoubleValue());

            break;

        case POINT4_Y:
            solid.getPoint4().setY(value.getDoubleValue());

            break;

        case POINT4_Z:
            solid.getPoint4().setZ(value.getDoubleValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, solid);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#startDXFEntity()
     */
    public void startDXFEntity() {
        solid = new DXFSolid();
    }
}
