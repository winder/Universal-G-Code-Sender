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
package org.kabeja.parser.table;

import org.kabeja.dxf.DXFDimensionStyle;
import org.kabeja.parser.DXFValue;
import org.kabeja.parser.entities.AbstractEntityHandler;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFDimensionStyleTableHandler extends AbstractTableHandler {
    public final static int GROUPCODE_NAME = 2;
    private DXFDimensionStyle style;
    private String key = "DIMSTYLE";

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#endParsing()
     */
    public void endParsing() {
        doc.addDXFDimensionStyle(style);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#getTableKey()
     */
    public String getTableKey() {
        return key;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#parseGroup(int,
     *      de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case AbstractEntityHandler.FLAGS:
            style.setFlags(value.getIntegerValue());

            break;

        case GROUPCODE_NAME:
            style.setName(value.getValue());

            break;

        default:
            style.setProperty("" + groupCode, value.getValue());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#startParsing()
     */
    public void startParsing() {
        style = new DXFDimensionStyle();
    }
}
