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
import org.kabeja.dxf.DXFLeader;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFLeaderHandler extends AbstractEntityHandler {
    public static final int GROUPCODE_ARROW_HEAD_FLAG = 71;
    public static final int GROUPCODE_LEADER_PATH_TYPE = 72;
    public static final int GROUPCODE_LEADER_CREATION_FLAG = 73;
    public static final int GROUPCODE_HOOKLINE_DIRECTION_FLAG = 74;
    public static final int GROUPCODE_HOOKLINE_FLAG = 75;
    public static final int GROUPCODE_TEXT_HEIGHT = 40;
    public static final int GROUPCODE_TEXT_WIDTH = 41;
    public static final int GROUPCODE_COLOR_LEADER = 77;
    public static final int GROUPCODE_TEXT_ENTITY_REFERENCE = 340;
    public static final int GROUPCODE_HORIZONTAL_DIRECTION_X = 211;
    public static final int GROUPCODE_HORIZONTAL_DIRECTION_Y = 221;
    public static final int GROUPCODE_HORIZONTAL_DIRECTION_Z = 231;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_X = 212;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_Y = 222;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_Z = 232;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_X = 213;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_Y = 223;
    public static final int GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_Z = 233;
    protected DXFLeader leader;
    protected Point vertex;

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.AbstractEntityHandler#getDXFEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_LEADER;
    }

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.DXFEntityHandler#startDXFEntity()
     */
    public void startDXFEntity() {
        leader = new DXFLeader();
    }

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.DXFEntityHandler#parseGroup(int, org.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_START_X:
            vertex = new Point();
            leader.addCoordinate(vertex);
            vertex.setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            vertex.setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            vertex.setZ(value.getDoubleValue());

            break;

        case GROUPCODE_STYLENAME:
            leader.setStyleNameID(value.getValue());

            break;

        case GROUPCODE_ARROW_HEAD_FLAG:
            leader.setArrowEnabled(value.getBooleanValue());

            break;

        case GROUPCODE_LEADER_PATH_TYPE:
            leader.setPathType(value.getIntegerValue());

            break;

        case GROUPCODE_LEADER_CREATION_FLAG:
            leader.setCreationType(value.getIntegerValue());

            break;

        case GROUPCODE_HOOKLINE_DIRECTION_FLAG:
            leader.setHooklineDirecton(value.getIntegerValue());

            break;

        case GROUPCODE_HOOKLINE_FLAG:
            leader.setHookline(value.getBooleanValue());

            break;

        case GROUPCODE_TEXT_HEIGHT:
            leader.setTextHeight(value.getDoubleValue());

            break;

        case GROUPCODE_TEXT_WIDTH:
            leader.setTextWidth(value.getDoubleValue());

            break;

        case GROUPCODE_HORIZONTAL_DIRECTION_X:
            leader.getHorizontalDirection().setX(value.getDoubleValue());

            break;

        case GROUPCODE_HORIZONTAL_DIRECTION_Y:
            leader.getHorizontalDirection().setY(value.getDoubleValue());

            break;

        case GROUPCODE_HORIZONTAL_DIRECTION_Z:
            leader.getHorizontalDirection().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_X:
            leader.getLastOffsetInsertion().setX(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_Y:
            leader.getLastOffsetInsertion().setY(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_INSERTPOINT_Z:
            leader.getLastOffsetInsertion().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_X:
            leader.getLastOffsetText().setX(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_Y:
            leader.getLastOffsetText().setY(value.getDoubleValue());

            break;

        case GROUPCODE_OFFSET_LAST_VERTEX_PLACEMENT_POINT_Z:
            leader.getLastOffsetText().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_TEXT_ENTITY_REFERENCE:
            leader.setTextID(value.getValue());

            break;

        case GROUPCODE_COLOR_LEADER:
            leader.setColor(value.getIntegerValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, leader);
        }
    }

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.DXFEntityHandler#getDXFEntity()
     */
    public DXFEntity getDXFEntity() {
        return leader;
    }

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.DXFEntityHandler#endDXFEntity()
     */
    public void endDXFEntity() {
    }

    /* (non-Javadoc)
     * @see org.kabeja.parser.entities.DXFEntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        return false;
    }
}
