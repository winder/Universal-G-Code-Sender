/*
 * Created on 17.10.2005
 *
 */
package org.kabeja.dxf;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFXLine extends DXFRay {
    public Bounds getBounds() {
        //the xline is a infinite straight line
        //so we omit the bounds
        Bounds bounds = new Bounds();
        bounds.setValid(false);

        return bounds;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_XLINE;
    }
}
