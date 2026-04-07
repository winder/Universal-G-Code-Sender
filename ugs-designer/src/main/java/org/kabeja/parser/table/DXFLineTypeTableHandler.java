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

import org.kabeja.dxf.DXFLineType;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 */
public class DXFLineTypeTableHandler extends AbstractTableHandler {
    public final static String TABLE_KEY = "LTYPE";
    public final static int GROUPCODE_LTYPE_NAME = 2;
    public final static int GROUPCODE_LTYPE_DESCRIPTION = 3;
    public final static int GROUPCODE_LTYPE_ALIGNMENT = 72;
    public final static int GROUPCODE_LTYPE_SEGMENT = 49;
    public final static int GROUPCODE_LTYPE_LENGTH = 40;
    public final static int GROUPCODE_LTYPE_SEGMENT_COUNT = 73;
    public final static int GROUPCODE_LTYPE_SCALE = 46;
    private DXFLineType ltype;
    private int segmentCount = 0;
    private double[] pattern;

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#endParsing()
     */
    public void endParsing() {
        ltype.setPattern(pattern);
        doc.addDXFLineType(ltype);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#getTableKey()
     */
    public String getTableKey() {
        // TODO Auto-generated method stub
        return TABLE_KEY;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#parseGroup(int,
     *      de.miethxml.kabeja.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_LTYPE_NAME:
            ltype.setName(value.getValue());

            break;

        case GROUPCODE_LTYPE_DESCRIPTION:
            ltype.setDescritpion(value.getValue());

            break;

        case GROUPCODE_LTYPE_SEGMENT_COUNT:

            int count = value.getIntegerValue();
            pattern = new double[count];
            segmentCount = 0;

            break;

        case GROUPCODE_LTYPE_SEGMENT:
            pattern[segmentCount] = value.getDoubleValue();
            segmentCount++;

            break;

        case GROUPCODE_LTYPE_LENGTH:
            ltype.setPatternLength(value.getDoubleValue());

            break;

        case GROUPCODE_LTYPE_ALIGNMENT:
            ltype.setAlignment(value.getIntegerValue());

            break;

        case GROUPCODE_LTYPE_SCALE:
            ltype.setScale(value.getDoubleValue());

            break;

        default:
            break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.table.DXFTableHandler#startParsing()
     */
    public void startParsing() {
        ltype = new DXFLineType();
        segmentCount = 0;
        pattern = null;
    }
}
