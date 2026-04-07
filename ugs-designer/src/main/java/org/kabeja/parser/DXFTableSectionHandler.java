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

import java.util.Hashtable;
import java.util.Iterator;

import org.kabeja.parser.table.DXFTableHandler;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFTableSectionHandler extends AbstractSectionHandler
    implements HandlerManager {
    public final static String sectionKey = "TABLES";
    public final static String TABLE_START = "TABLE";
    public final static String TABLE_END = "ENDTAB";
    public final int TABLE_CODE = 0;
    private String table = "";
    private DXFTableHandler handler;
    private Hashtable handlers = new Hashtable();
    private boolean parse = false;

    public DXFTableSectionHandler() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.SectionHandler#getSectionKey()
     */
    public String getSectionKey() {
        return sectionKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.SectionHandler#parseGroup(int, java.lang.String)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        if (groupCode == TABLE_CODE) {
            //switch table
            if (TABLE_END.equals(value)) {
                table = "";

                if (parse) {
                    handler.endParsing();
                    parse = false;
                }
            } else if (TABLE_START.equals(value)) {
            } else {
                if (parse) {
                    handler.endParsing();
                }

                table = value.getValue();

                if (handlers.containsKey(table)) {
                    handler = (DXFTableHandler) handlers.get(table);
                    handler.setDXFDocument(this.doc);
                    handler.startParsing();
                    parse = true;
                } else {
                    parse = false;
                }
            }
        } else {
            if (parse) {
                handler.parseGroup(groupCode, value);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.SectionHandler#endParsing()
     */
    public void endSection() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.SectionHandler#startParsing()
     */
    public void startSection() {
        parse = false;
    }

    public void addHandler(Handler handler) {
        addDXFTableHandler((DXFTableHandler) handler);
    }

    public void addDXFTableHandler(DXFTableHandler handler) {
        handlers.put(handler.getTableKey(), handler);
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        this.doc = null;

        Iterator i = handlers.values().iterator();

        while (i.hasNext()) {
            Handler handler = (Handler) i.next();
            handler.releaseDXFDocument();
        }
    }
}
