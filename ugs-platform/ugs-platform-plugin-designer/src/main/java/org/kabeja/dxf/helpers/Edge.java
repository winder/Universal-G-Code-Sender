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
package org.kabeja.dxf.helpers;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class Edge {
    protected Point startPoint = new Point();
    protected Point endPoint = new Point();

    /**
     * @return Returns the endPoint.
     */
    public Point getEndPoint() {
        return endPoint;
    }

    /**
     * @param endPoint The endPoint to set.
     */
    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }

    /**
     * @return Returns the startPoint.
     */
    public Point getStartPoint() {
        return startPoint;
    }

    /**
     * @param startPoint The startPoint to set.
     */
    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public Point getIntersectionPoint(Edge e) {
        return null;
    }
}
