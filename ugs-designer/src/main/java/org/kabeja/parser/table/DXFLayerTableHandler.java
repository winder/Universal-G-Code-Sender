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

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFLayerTableHandler extends AbstractTableHandler {
    public final static String TABLE_KEY = "LAYER";
    public final static int GROUPCODE_LAYER_NAME = 2;
    public final static int GROUPCODE_LAYER_LINETYPE = 6;
    public final static int GROUPCODE_LAYER_COLORNUMBER = 62;
    public final static int GROUPCODE_LAYER_PLOTTINGFLAG = 290;
    public final static int GROUPCODE_LAYER_LINEWEIGHT = 370;
    public final static int GROUPCODE_LAYER_PLOTSTYLENAME = 390;
    private DXFLayer layer;

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.table.TableHandler#getTableKey()
     */
    public String getTableKey() {
        return TABLE_KEY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.table.TableHandler#parseGroup(int,
     *      java.lang.String)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_LAYER_NAME:
            layer.setName(value.getValue());

            break;

        case GROUPCODE_LAYER_COLORNUMBER:
            layer.setColor(value.getIntegerValue());

            break;

        case GROUPCODE_LAYER_LINETYPE:
            layer.setLineType(value.getValue());

            break;

        case DXFConstants.GROUPCODE_STANDARD_FLAGS:
            layer.setFlags(value.getIntegerValue());

            break;

        case GROUPCODE_LAYER_LINEWEIGHT:
            layer.setLineWeight(value.getIntegerValue());

            break;

        case GROUPCODE_LAYER_PLOTSTYLENAME:
            layer.setPlotStyle(value.getValue());

            break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.table.TableHandler#endParsing()
     */
    public void endParsing() {
        doc.addDXFLayer(layer);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.table.TableHandler#startParsing()
     */
    public void startParsing() {
        layer = new DXFLayer();
        layer.setDXFDocument(doc);
    }
}
