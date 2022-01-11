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
package org.kabeja.parser.table;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFViewport;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFVPortTableHandler extends AbstractTableHandler {
    public final static int GROUPCODE_VPORT_NAME = 2;
    public final static int GROUPCODE_VPORT_LOWER_LEFT_X = 10;
    public final static int GROUPCODE_VPORT_LOWER_LEFT_Y = 20;
    public final static int GROUPCODE_VPORT_UPPER_RIGHT_X = 11;
    public final static int GROUPCODE_VPORT_UPPER_RIGHT_Y = 21;
    public final static int GROUPCODE_VPORT_CENTER_POINT_X = 12;
    public final static int GROUPCODE_VPORT_CENTER_POINT_Y = 22;
    public final static int GROUPCODE_VPORT_SNAP_BASE_POINT_X = 13;
    public final static int GROUPCODE_VPORT_SNAP_BASE_POINT_Y = 23;
    public final static int GROUPCODE_HEIGHT = 40;
    public final static int GROUPCODE_ASPECT_RATIO = 41;
    private DXFViewport viewport;

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#endParsing()
     */
    public void endParsing() {
        doc.addDXFViewport(viewport);
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#getTableKey()
     */
    public String getTableKey() {
        return DXFConstants.TABLE_KEY_VPORT;
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#parseGroup(int, de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_VPORT_NAME:
            viewport.setViewportID(value.getValue());

            if ("*active".equals(value.getValue().toLowerCase())) {
                viewport.setActive(true);
            }

            break;

        case GROUPCODE_VPORT_CENTER_POINT_X:
            viewport.getCenterPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_VPORT_CENTER_POINT_Y:
            viewport.getCenterPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_VPORT_LOWER_LEFT_X:
            viewport.getLowerLeftCorner().setX(value.getDoubleValue());

            break;

        case GROUPCODE_VPORT_LOWER_LEFT_Y:
            viewport.getLowerLeftCorner().setY(value.getDoubleValue());

            break;

        case GROUPCODE_HEIGHT:
            viewport.setHeight(value.getDoubleValue());

            break;

        case GROUPCODE_ASPECT_RATIO:
            viewport.setAspectRatio(value.getDoubleValue());

            break;
        }
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#startParsing()
     */
    public void startParsing() {
        viewport = new DXFViewport();
    }
}
