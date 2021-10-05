/*
   Copyright 2007 Simon Mieth

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
package org.kabeja.parser.objects;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.objects.DXFLayout;
import org.kabeja.dxf.objects.DXFObject;
import org.kabeja.parser.DXFValue;


public class DXFLayoutHandler extends DXFPlotsettingsHandler {
    public final static int GROUPCODE_MINIMUM_LIMITS_X = 10;
    public final static int GROUPCODE_MINIMUM_LIMITS_Y = 20;
    public final static int GROUPCODE_MAXIMUM_LIMITS_X = 11;
    public final static int GROUPCODE_MAXIMUM_LIMITS_Y = 21;
    public final static int GROUPCODE_INSERT_POINT_X = 12;
    public final static int GROUPCODE_INSERT_POINT_Y = 22;
    public final static int GROUPCODE_INSERT_POINT_Z = 32;
    public final static int GROUPCODE_MINIMUM_EXTENTS_X = 14;
    public final static int GROUPCODE_MINIMUM_EXTENTS_Y = 24;
    public final static int GROUPCODE_MINIMUM_EXTENTS_Z = 34;
    public final static int GROUPCODE_MAXIMUM_EXTENTS_X = 15;
    public final static int GROUPCODE_MAXIMUM_EXTENTS_Y = 25;
    public final static int GROUPCODE_MAXIMUM_EXTENTS_Z = 35;
    public final static int GROUPCODE_ELEVATION = 146;
    public final static int GROUPCODE_UCS_ORIGIN_X = 13;
    public final static int GROUPCODE_UCS_ORIGIN_Y = 23;
    public final static int GROUPCODE_UCS_ORIGIN_Z = 33;
    public final static int GROUPCODE_UCS_AXIS_X_X = 16;
    public final static int GROUPCODE_UCS_AXIS_X_Y = 26;
    public final static int GROUPCODE_UCS_AXIS_X_Z = 36;
    public final static int GROUPCODE_UCS_AXIS_Y_X = 17;
    public final static int GROUPCODE_UCS_AXIS_Y_Y = 27;
    public final static int GROUPCODE_UCS_AXIS_Y_Z = 37;
    public final static int GROUPCODE_UCS_ORTHOGRAPHIC_TYPE = 76;
    public final static int GROUPCODE_PAPER_SPACE_BLOCK_RECORD_ID = 330;
    public final static int GROUPCODE_LAST_ACTIVE_VIEWPORT_ID = 331;
    public final static int GROUPCODE_UCS_ID = 345;
    public final static int GROUPCODE_UCS_BASE_ID = 346;
    protected DXFLayout layout;

    public void endObject() {
    }

    public DXFObject getDXFObject() {
        return this.layout;
    }

    public String getObjectType() {
        return DXFConstants.OBJECT_TYPE_LAYOUT;
    }

    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_ELEVATION:
            this.layout.setElevation(value.getDoubleValue());

            break;

        case GROUPCODE_INSERT_POINT_X:
            this.layout.getInsertPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_INSERT_POINT_Y:
            this.layout.getInsertPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_INSERT_POINT_Z:
            this.layout.getInsertPoint().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_LAST_ACTIVE_VIEWPORT_ID:
            this.layout.setLastActiveViewportID(value.getValue());

            break;

        case GROUPCODE_MAXIMUM_EXTENTS_X:
            this.layout.getExtent().setMaximumX(value.getDoubleValue());

            break;

        case GROUPCODE_MAXIMUM_EXTENTS_Y:
            this.layout.getExtent().setMaximumY(value.getDoubleValue());

            break;

        case GROUPCODE_MAXIMUM_EXTENTS_Z:
            this.layout.getExtent().setMaximumZ(value.getDoubleValue());

            break;

        case GROUPCODE_MAXIMUM_LIMITS_X:
            this.layout.getLimits().setMaximumX(value.getDoubleValue());

            break;

        case GROUPCODE_MAXIMUM_LIMITS_Y:
            this.layout.getLimits().setMaximumY(value.getDoubleValue());

            break;

        case GROUPCODE_MINIMUM_EXTENTS_X:
            this.layout.getExtent().setMinimumX(value.getDoubleValue());

            break;

        case GROUPCODE_MINIMUM_EXTENTS_Y:
            this.layout.getExtent().setMinimumY(value.getDoubleValue());

            break;

        case GROUPCODE_MINIMUM_EXTENTS_Z:
            this.layout.getExtent().setMinimumZ(value.getDoubleValue());

            break;

        case GROUPCODE_MINIMUM_LIMITS_X:
            this.layout.getLimits().setMinimumX(value.getDoubleValue());

            break;

        case GROUPCODE_MINIMUM_LIMITS_Y:
            this.layout.getLimits().setMinimumY(value.getDoubleValue());

            break;

        case GROUPCODE_PAPER_SPACE_BLOCK_RECORD_ID:
            this.layout.setPaperSpaceBlockID(value.getValue());

            break;

        case GROUPCODE_UCS_AXIS_X_X:
            this.layout.getXAxisUCS().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_AXIS_X_Y:
            this.layout.getXAxisUCS().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_AXIS_X_Z:
            this.layout.getXAxisUCS().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_AXIS_Y_X:
            this.layout.getYAxisUCS().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_AXIS_Y_Y:
            this.layout.getYAxisUCS().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_AXIS_Y_Z:
            this.layout.getYAxisUCS().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_BASE_ID:
            this.layout.setBaseUCSID(value.getValue());

            break;

        case GROUPCODE_UCS_ID:
            this.layout.setNamedUCSID(value.getValue());

            break;

        case GROUPCODE_UCS_ORIGIN_X:
            this.layout.getOriginUCS().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORIGIN_Y:
            this.layout.getOriginUCS().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORIGIN_Z:
            this.layout.getOriginUCS().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORTHOGRAPHIC_TYPE:
            this.layout.setOrthographicTypeOfUCS(value.getIntegerValue());

            break;

        default:
            super.parseGroup(groupCode, value);
        }
    }

    public void startObject() {
        this.layout = new DXFLayout();
        this.plotSettings = this.layout;
    }
}
