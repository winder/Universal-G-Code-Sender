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
package org.kabeja.dxf;

import java.util.ArrayList;
import java.util.List;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFRegion extends DXFEntity {
    protected List acisData = new ArrayList();

    /**
     *
     *
     * @return always invalid bounds
     */
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        bounds.setValid(false);

        return bounds;
    }

    /**
     *
     *
     * @see org.kabeja.dxf.DXFEntity#getType()
     */
    public String getType() {
        return DXFConstants.ENTITY_TYPE_REGION;
    }

    /**
     * The ACIS commands as a list of lines
     *
     * @return the list
     */
    public List getACISDATA() {
        return acisData;
    }

    public void appendACISDATA(String data) {
        acisData.add(data);
    }

    /**
     * This entity is only a container of ACIS data.
     *
     * @return always 0
     */
    public double getLength() {
        return 0;
    }
}
