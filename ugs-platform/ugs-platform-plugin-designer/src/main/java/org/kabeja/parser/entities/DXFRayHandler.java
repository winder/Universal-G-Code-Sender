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
import org.kabeja.dxf.DXFRay;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFRayHandler extends AbstractEntityHandler {
    protected DXFRay ray;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#getDXFEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_RAY;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#endDXFEntity()
     */
    public void endDXFEntity() {
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#getDXFEntity()
     */
    public DXFEntity getDXFEntity() {
        return this.ray;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
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
        case GROUPCODE_START_X:
            this.ray.getBasePoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            this.ray.getBasePoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            this.ray.getBasePoint().setY(value.getDoubleValue());

            break;

        case END_X:
            this.ray.getDirection().setX(value.getDoubleValue());

            break;

        case END_Y:
            this.ray.getDirection().setY(value.getDoubleValue());

            break;

        case END_Z:
            this.ray.getDirection().setZ(value.getDoubleValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, ray);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.entities.DXFEntityHandler#startDXFEntity()
     */
    public void startDXFEntity() {
        this.ray = new DXFRay();
    }
}
