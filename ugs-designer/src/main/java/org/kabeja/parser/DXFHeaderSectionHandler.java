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

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFHeader;
import org.kabeja.dxf.DXFVariable;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFHeaderSectionHandler implements DXFSectionHandler {
    public final static int VARIABLE_CODE = 9;
    private final String sectionKey = "HEADER";
    private DXFDocument doc;
    private DXFVariable variable = null;
    private String mode;

    /* (non-Javadoc)
     * @see org.dxf2svg.parser.SectionHandler#getSectionKey()
     */
    public String getSectionKey() {
        return sectionKey;
    }

    /* (non-Javadoc)
     * @see org.dxf2svg.parser.SectionHandler#setDXFDocument(org.dxf2svg.xml.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;
    }

    /* (non-Javadoc)
     * @see org.dxf2svg.parser.SectionHandler#parseGroup(int, java.lang.String)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        if (groupCode == VARIABLE_CODE) {
            variable = new DXFVariable(value.getValue());
            doc.getDXFHeader().setVariable(variable);
        } else {
            //handle the current mode
            parse(groupCode, value);
        }
    }

    private void parse(int code, DXFValue value) {
        variable.setValue("" + code, value.getValue());
    }

    /* (non-Javadoc)
     * @see org.dxf2svg.parser.SectionHandler#endParsing()
     */
    public void endSection() {
    }

    /* (non-Javadoc)
     * @see org.dxf2svg.parser.SectionHandler#startParsing()
     */
    public void startSection() {
        doc.setDXFHeader(new DXFHeader());
    }

    /* (non-Javadoc)
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        this.doc = null;
    }
}
