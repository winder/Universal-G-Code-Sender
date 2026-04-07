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
package org.kabeja.parser;

import java.util.HashMap;

import org.kabeja.parser.objects.DXFObjectHandler;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFObjectsSectionHandler extends AbstractSectionHandler
    implements HandlerManager {
    private static String SECTION_KEY = "OBJECTS";
    public static final int OBJECT_START = 0;
    private HashMap handlers = new HashMap();
    private DXFObjectHandler handler;
    private boolean parseObject = false;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.DXFSectionHandler#endSection()
     */
    public void endSection() {
        this.endObject();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.DXFSectionHandler#getSectionKey()
     */
    public String getSectionKey() {
        return SECTION_KEY;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.DXFSectionHandler#parseGroup(int,
     *      de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        if (groupCode == OBJECT_START) {
            this.endObject();

            if (this.handlers.containsKey(value.getValue())) {
                this.parseObject = true;
                this.handler = (DXFObjectHandler) handlers.get(value.getValue());
                this.handler.setDXFDocument(this.doc);
                this.handler.startObject();
            } else {
                this.parseObject = false;
            }
        } else if (this.parseObject) {
            this.handler.parseGroup(groupCode, value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.DXFSectionHandler#startSection()
     */
    public void startSection() {
        this.parseObject = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        this.doc = null;
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.HandlerManager#addHandler(de.miethxml.kabeja.parser.Handler)
     */
    public void addHandler(Handler handler) {
        DXFObjectHandler h = (DXFObjectHandler) handler;
        h.setDXFDocument(this.doc);
        this.handlers.put(h.getObjectType(), h);
    }

    protected void endObject() {
        if (this.parseObject) {
            //finish the old parsing object
            this.handler.endObject();
            this.doc.addDXFObject(handler.getDXFObject());
        }
    }
}
