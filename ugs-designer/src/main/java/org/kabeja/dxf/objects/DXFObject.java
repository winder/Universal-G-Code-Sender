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
package org.kabeja.dxf.objects;

import org.kabeja.dxf.DXFDocument;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public abstract class DXFObject {
    protected DXFDocument doc;
    protected String softID;
    protected String hardID;
    protected String handleID;

    public String getSoftPointerID() {
        return softID;
    }

    public void setSoftPointerID(String id) {
        this.softID = id;
    }

    public String getHardOwnerID() {
        return hardID;
    }

    public void setHardOwnerID(String id) {
        this.hardID = id;
    }

    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;
    }

    public abstract String getObjectType();

    /**
     * @return Returns the handleID.
     */
    public String getID() {
        return handleID;
    }

    /**
     * @param handleID The handleID to set.
     */
    public void setID(String handleID) {
        this.handleID = handleID;
    }
}
