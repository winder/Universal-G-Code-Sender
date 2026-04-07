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

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.parser.DXFValue;
import org.kabeja.parser.Handler;


/**
 *
 * This interface descripe an Entity jandler, which should
 * handle (parse) an DXF entity.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>setDXFDocument</li>
 * <li>startDXFEntity</li>
 * <li>parseGroup (multiple)</li>
 * <li>isFollowSequence (need for polylines, where multiple vertices follow)</li>
 * <li>endDXFEntity</li>
 * <li>getDXFEntity</li>
 * </lo>
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 *
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 */
public interface DXFEntityHandler extends Handler {
    /**
     *
     * @return the DXFEntity name (LINE,POLYLINE,TEXT,...)
     */
    public abstract String getDXFEntityName();

    public void setDXFDocument(DXFDocument doc);

    /**
     * Will called if the entity block starts.
     *
     */
    public abstract void startDXFEntity();

    public abstract void parseGroup(int groupCode, DXFValue value);

    /**
     * Called after endDXFEntity.
     * @return the parsed Entity
     */
    public abstract DXFEntity getDXFEntity();

    /**
     * Will called if the entity block ends.
     *
     */
    public abstract void endDXFEntity();

    /**
     *
     * @return true if the this DXFEntityHandler have to parse the following entities (like POLYLINE),
     *  otherwise false (like TEXT,LINE).
     */
    public abstract boolean isFollowSequence();
}
