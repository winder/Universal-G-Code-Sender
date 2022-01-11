/*
 * Created on 29.09.2005
 *
 */
package org.kabeja.dxf;

import org.kabeja.math.MathUtils;


/**
 * @author simon
 *
 */
public class DXF3DFace extends DXFSolid {
    public String getType() {
        return DXFConstants.ENTITY_TYPE_3DFACE;
    }

    public double getLength() {
        double length = 0.0;
        int flag = this.getFlags();

        if ((flag & 1) == 0) {
            length += MathUtils.distance(this.getPoint1(), this.getPoint2());
        }

        if ((flag & 2) == 0) {
            length += MathUtils.distance(this.getPoint2(), this.getPoint3());
        }

        if ((flag & 4) == 0) {
            length += MathUtils.distance(this.getPoint3(), this.getPoint4());
        }

        if ((flag & 8) == 0) {
            length += MathUtils.distance(this.getPoint4(), this.getPoint1());
        }

        return length;
    }
}
