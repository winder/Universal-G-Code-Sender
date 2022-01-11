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
import org.kabeja.dxf.DXFViewport;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFViewportHandler extends AbstractEntityHandler {
    public static final int GROUPCODE_CENTER_X = 10;
    public static final int GROUPCODE_CENTER_Y = 20;
    public static final int GROUPCODE_CENTER_Z = 30;
    public static final int GROUPCODE_HEIGHT = 41;
    public static final int GROUPCODE_WIDTH = 40;
    public static final int GROUPCODE_VIEW_DIRECTION_X = 16;
    public static final int GROUPCODE_VIEW_DIRECTION_Y = 26;
    public static final int GROUPCODE_VIEW_DIRECTION_Z = 36;
    public static final int GROUPCODE_VIEW_CENTER_X = 12;
    public static final int GROUPCODE_VIEW_CENTER_Y = 22;
    public static final int GROUPCODE_VIEW_CENTER_Z = 32;
    public static final int GROUPCODE_VIEW_TARGET_X = 17;
    public static final int GROUPCODE_VIEW_TARGET_Y = 27;
    public static final int GROUPCODE_VIEW_TARGET_Z = 37;
    public static final int GROUPCODE_SNAP_BASE_POINT_X = 13;
    public static final int GROUPCODE_SNAP_BASE_POINT_Y = 23;
    public static final int GROUPCODE_SNAP_SPACING_X = 14;
    public static final int GROUPCODE_SNAP_SPACING_Y = 24;
    public static final int GROUPCODE_GRID_SPACING_X = 15;
    public static final int GROUPCODE_GRID_SPACING_Y = 25;
    public static final int GROUPCODE_LENS_LENGTH = 42;
    public static final int GROUPCODE_FRONT_CLIPPING = 43;
    public static final int GROUPCODE_BACK_CLIPPING = 44;
    public static final int GROUPCODE_VIEW_HEIGHT = 45;
    public static final int GROUPCODE_SNAP_ANGLE = 50;
    public static final int GROUPCODE_TWIST_ANGLE = 51;
    public static final int GROUPCODE_RENDER_MODE = 281;
    public static final int GROUPCODE_UCS_ORIGIN_X = 110;
    public static final int GROUPCODE_UCS_ORIGIN_Y = 120;
    public static final int GROUPCODE_UCS_ORIGIN_Z = 130;
    public static final int GROUPCODE_UCS_X_AXIS_X = 111;
    public static final int GROUPCODE_UCS_X_AXIS_Y = 121;
    public static final int GROUPCODE_UCS_X_AXIS_Z = 131;
    public static final int GROUPCODE_UCS_Y_AXIS_X = 112;
    public static final int GROUPCODE_UCS_Y_AXIS_Y = 122;
    public static final int GROUPCODE_UCS_Y_AXIS_Z = 132;
    public static final int GROUPCODE_UCS_TYPE = 79;
    public static final int GROUPCODE_UCS_ELEVATION = 146;
    public static final int GROUPCODE_CIRCLE_ZOOM_PERCENT = 72;
    public static final int GROUPCODE_VIEWPORT_ID = 69;
    public static final int GROUPCODE_VIEWPORT_STATUS = 68;
    public static final int GROUPCODE_PLOTSTYLE_NAME = 1;
    public static final int GROUPCODE_FROZEN_LAYER = 341;
    public static final int GROUPCODE_FROZEN_LAYER_XDATA = 1003;
    private int[] xDataConvert = new int[] {
            1000, 1002, 1070, 17, 27, 37, 16, 26, 36, 51, 45, 12, 22, 42, 43, 44,
            90, 72, 90, 90, 90, 90, 90, 90, 50, 13, 23, 14, 24, 15, 25, 90, 1002
        };
    private DXFViewport viewport;
    private boolean convertXDATA = false;
    private int pos = 0;

    public void parseGroup(int groupCode, DXFValue value) {
        //check for XDATA
        if (convertXDATA && (pos < xDataConvert.length)) {
            groupCode = xDataConvert[pos];
            pos++;
        } else if ((groupCode == 1001) && value.getValue().equals("ACAD")) {
            convertXDATA = true;
            pos = 0;
        } else {
            convertXDATA = false;
        }

        switch (groupCode) {
        case GROUPCODE_CENTER_X:
            viewport.getCenterPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_CENTER_Y:
            viewport.getCenterPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_CENTER_Z:
            viewport.getCenterPoint().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_CENTER_X:
            viewport.getViewCenterPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_CENTER_Y:
            viewport.getViewCenterPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_CENTER_Z:
            viewport.getViewCenterPoint().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_DIRECTION_X:
            viewport.getViewDirectionVector().setX(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_DIRECTION_Y:
            viewport.getViewDirectionVector().setY(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_DIRECTION_Z:
            viewport.getViewDirectionVector().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_TARGET_X:
            viewport.getViewTargetPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_TARGET_Y:
            viewport.getViewTargetPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_VIEW_TARGET_Z:
            viewport.getViewTargetPoint().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_SNAP_BASE_POINT_X:
            viewport.getSnapBasePoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_SNAP_BASE_POINT_Y:
            viewport.getSnapBasePoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_SNAP_SPACING_X:
            viewport.getSnapSpacingPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_SNAP_SPACING_Y:
            viewport.getSnapSpacingPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_GRID_SPACING_X:
            viewport.getGridSpacingPoint().setX(value.getDoubleValue());

            break;

        case GROUPCODE_GRID_SPACING_Y:
            viewport.getGridSpacingPoint().setY(value.getDoubleValue());

            break;

        case GROUPCODE_VIEWPORT_ID:
            viewport.setViewportID(value.getValue());

            break;

        case GROUPCODE_WIDTH:
            viewport.setWidth(value.getDoubleValue());

            break;

        case GROUPCODE_HEIGHT:
            viewport.setHeight(value.getDoubleValue());

            break;

        case GROUPCODE_FRONT_CLIPPING:
            viewport.setFrontClippingPlane(value.getDoubleValue());

            break;

        case GROUPCODE_BACK_CLIPPING:
            viewport.setBackClippingPlane(value.getDoubleValue());

            break;

        case GROUPCODE_LENS_LENGTH:
            viewport.setLensLength(value.getDoubleValue());

            break;

        case GROUPCODE_RENDER_MODE:
            viewport.setRenderMode(value.getIntegerValue());

            break;

        case GROUPCODE_TWIST_ANGLE:
            viewport.setTwistAngle(value.getDoubleValue());

            break;

        case GROUPCODE_SNAP_ANGLE:
            viewport.setSnapAngle(value.getDoubleValue());

            break;

        case GROUPCODE_CIRCLE_ZOOM_PERCENT:
            viewport.setCircleZoom(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ELEVATION:
            viewport.setUcsElevation(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORIGIN_X:
            viewport.getUcsOrigin().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORIGIN_Y:
            viewport.getUcsOrigin().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_ORIGIN_Z:
            viewport.getUcsOrigin().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_X_AXIS_X:
            viewport.getUcsXAxis().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_X_AXIS_Y:
            viewport.getUcsXAxis().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_X_AXIS_Z:
            viewport.getUcsXAxis().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_Y_AXIS_X:
            viewport.getUcsYAxis().setX(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_Y_AXIS_Y:
            viewport.getUcsYAxis().setY(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_Y_AXIS_Z:
            viewport.getUcsYAxis().setZ(value.getDoubleValue());

            break;

        case GROUPCODE_UCS_TYPE:
            viewport.setUcsType(value.getIntegerValue());

            break;

        case GROUPCODE_VIEW_HEIGHT:
            viewport.setViewHeight(value.getDoubleValue());

            break;

        case GROUPCODE_VIEWPORT_STATUS:
            viewport.setViewportStatus(value.getIntegerValue());

            break;

        case GROUPCODE_PLOTSTYLE_NAME:
            viewport.setPlotStyleName(value.getValue());

            break;

        case GROUPCODE_FROZEN_LAYER:
            viewport.addFrozenLayer(value.getValue());

            break;

        case GROUPCODE_FROZEN_LAYER_XDATA:
            viewport.addFrozenLayer(value.getValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, viewport);

            break;
        }
    }

    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_VIEWPORT;
    }

    public void endDXFEntity() {
        // nothing to do
    }

    public DXFEntity getDXFEntity() {
        return this.viewport;
    }

    public boolean isFollowSequence() {
        return false;
    }

    public void startDXFEntity() {
        this.viewport = new DXFViewport();
        this.viewport.setModelSpace(false);
    }
}
