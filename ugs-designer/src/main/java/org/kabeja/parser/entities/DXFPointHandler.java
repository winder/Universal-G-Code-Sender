package org.kabeja.parser.entities;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFValue;

public class DXFPointHandler extends AbstractEntityHandler {
    DXFPoint point;

    @Override
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_POINT;
    }

    @Override
    public void startDXFEntity() {
        point = new DXFPoint();
        point.setPoint(new Point());
    }

    @Override
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
            case GROUPCODE_START_X:
                point.getPoint().setX(value.getDoubleValue());
                break;

            case GROUPCODE_START_Y:
                point.getPoint().setY(value.getDoubleValue());
                break;
        }

        super.parseCommonProperty(groupCode, value, point);
    }

    @Override
    public DXFEntity getDXFEntity() {
        return point;
    }

    @Override
    public void endDXFEntity() {

    }

    @Override
    public boolean isFollowSequence() {
        return false;
    }
}
