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

import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 */
public class DXFLine extends DXFEntity {
    private Point start;
    private Point end;

    public DXFLine() {
        start = new Point();
        end = new Point();
    }

    public void setProperty(int groupcode, String value) {
    }

    public void setStartPoint(Point start) {
        this.start = start;
    }

    /**
     * @return Returns the end.
     */
    public Point getEndPoint() {
        return end;
    }

    /**
     * @param end
     *            The end to set.
     */
    public void setEndPoint(Point end) {
        this.end = end;
    }

    /**
     * @return Returns the start.
     */
    public Point getStartPoint() {
        return start;
    }

    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        bounds.addToBounds(this.end);
        bounds.addToBounds(this.start);

        return bounds;
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_LINE;
    }

    public double getLength() {
        return MathUtils.distance(this.start, this.end);
    }
}
