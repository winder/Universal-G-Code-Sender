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
import org.kabeja.dxf.DXFInsert;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFInsertHandler extends AbstractEntityHandler {
    public static final int SCALE_X = 41;
    public static final int SCALE_Y = 42;
    public static final int SCALE_Z = 43;
    public static final int ROTATE = 50;
    public static final int COLUMN_COUNT = 70;
    public static final int ROW_COUNT = 71;
    public static final int COLUMN_SPACING = 44;
    public static final int ROW_SPACING = 45;
    public static final int BLOCK_NAME = 2;
    private DXFInsert insert;

    /**
     *
     */
    public DXFInsertHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#endDXFEntity()
     */
    public void endDXFEntity() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#getDXFEntity()
     */
    public DXFEntity getDXFEntity() {
        return insert;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#getDXFEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#parseGroup(int,
     *      org.dxf2svg.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_START_X:
            insert.getPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            insert.getPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            insert.getPoint().setZ(value.getDoubleValue());

            break;

        case SCALE_X:
            insert.setScaleX(value.getDoubleValue());

            break;

        case SCALE_Y:
            insert.setScaleY(value.getDoubleValue());

            break;

        case SCALE_Z:
            insert.setScaleZ(value.getDoubleValue());

            break;

        case ROTATE:
            insert.setRotate(value.getDoubleValue());

            break;

        case COLUMN_COUNT:
            insert.setColumns(value.getIntegerValue());

            break;

        case ROW_COUNT:
            insert.setRows(value.getIntegerValue());

            break;

        case COLUMN_SPACING:
            insert.setColumnSpacing(value.getDoubleValue());

            break;

        case ROW_SPACING:
            insert.setRowSpacing(value.getDoubleValue());

            break;

        case BLOCK_NAME:
            insert.setBlockID(value.getValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, insert);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.DXFEntityHandler#startDXFEntity()
     */
    public void startDXFEntity() {
        insert = new DXFInsert();
    }
}
