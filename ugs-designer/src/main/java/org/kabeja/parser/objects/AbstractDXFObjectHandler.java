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
package org.kabeja.parser.objects;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.objects.DXFObject;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public abstract class AbstractDXFObjectHandler implements DXFObjectHandler {
    public final static int GROUPCODE_SOFTPOINTER_ID = 330;
    public final static int GROUPCODE_HARDOWNER_ID = 360;
    public final static int GROUPCODE_HANDLE_ID = 5;
    protected DXFDocument doc;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        doc = null;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#setDXFDocument(de.miethxml.kabeja.dxf.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;
    }

    protected void parseCommonGroupCode(int groupCode, DXFValue value,
        DXFObject obj) {
        switch (groupCode) {
        case GROUPCODE_HANDLE_ID:
            obj.setID(value.getValue());

            break;

        case GROUPCODE_HARDOWNER_ID:
            obj.setHardOwnerID(value.getValue());

            break;

        case GROUPCODE_SOFTPOINTER_ID:
            obj.setSoftPointerID(value.getValue());

            break;
        }
    }
}
