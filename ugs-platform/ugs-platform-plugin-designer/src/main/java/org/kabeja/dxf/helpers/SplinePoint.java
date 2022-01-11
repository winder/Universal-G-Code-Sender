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
public class SplinePoint extends Point {
    public static final int TYPE_CONTROLPOINT = 0;
    public static final int TYPE_FITPOINT = 1;
    public static final int TYPE_STARTTANGENT = 2;
    public static final int TYPE_ENDTANGENT = 3;
    protected int type = 0;

    /**
     * @return Returns the controlPoint.
     */
    public boolean isControlPoint() {
        return this.type == TYPE_CONTROLPOINT;
    }

    /**
     * @return Returns the endTangent.
     */
    public boolean isEndTangent() {
        return this.type == TYPE_ENDTANGENT;
    }

    /**
     * @return Returns the fitPoint.
     */
    public boolean isFitPoint() {
        return this.type == TYPE_FITPOINT;
    }

    /**
     * @return Returns the startTangent.
     */
    public boolean isStartTangent() {
        return this.type == TYPE_STARTTANGENT;
    }

    /**
     * Sets the type of the point
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * gets the type of the point
     * @return
     */
    public int getType() {
        return this.type;
    }
}
