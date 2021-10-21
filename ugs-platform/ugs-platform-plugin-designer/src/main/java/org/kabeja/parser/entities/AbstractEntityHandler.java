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
package org.kabeja.parser.entities;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth </a>
 *
 */
public abstract class AbstractEntityHandler implements DXFEntityHandler {
    public final static int ELEMENT_REFERENCE = 5;
    public final static int GROUPCODE_START_X = 10;
    public final static int GROUPCODE_START_Y = 20;
    public final static int GROUPCODE_START_Z = 30;
    public final static int END_X = 11;
    public final static int END_Y = 21;
    public final static int END_Z = 31;
    public final static int LAYER_NAME = 8;
    public final static int TRANSPARENCY = 440;
    public final static int COLOR_CODE = 62;
    public final static int COLORNAME = 430;
    public final static int COLOR_24BIT = 420;
    public final static int COLOR_TRANSPARENCY = 440;
    public final static int FLAGS = 70;
    public final static int EXTRUSION_X = 210;
    public final static int EXTRUSION_Y = 220;
    public final static int EXTRUSION_Z = 230;
    public final static int VISIBILITY = 60;
    public final static int LINE_TYPE = 6;
    public final static int LINE_TYPE_SCALE = 48;
    public final static int LINE_WEIGHT = 370;
    public final static int GROUPCODE_THICKNESS = 39;
    public final static int GROUPCODE_STYLENAME = 3;
    public final static int GROUPCODE_TEXT = 1;
    public final static int GROUPCODE_ROTATION_ANGLE = 50;
    public final static int GROUPCODE_MODELSPACE = 67;
    protected DXFDocument doc;

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#setDXFDocument(org.dxf2svg.xml.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;
    }

    protected void parseCommonProperty(int groupCode, DXFValue value,
        DXFEntity entity) {
        switch (groupCode) {
        case ELEMENT_REFERENCE:
            entity.setID(value.getValue());

            break;

        case LAYER_NAME:
            entity.setLayerName(value.getValue());

            break;

        case FLAGS:
            entity.setFlags(value.getIntegerValue());

            break;

        case VISIBILITY:
            entity.setVisibile(!value.getBooleanValue());

            break;

        case LINE_TYPE:
            entity.setLineType(value.getValue());

            break;

        case LINE_TYPE_SCALE:
            entity.setLinetypeScaleFactor(value.getDoubleValue());

            break;

        case COLOR_CODE:
            entity.setColor(value.getIntegerValue());

            break;

        case EXTRUSION_X:
            entity.setExtrusionX(value.getDoubleValue());

            break;

        case EXTRUSION_Y:
            entity.setExtrusionY(value.getDoubleValue());

            break;

        case EXTRUSION_Z:
            entity.setExtrusionZ(value.getDoubleValue());

            break;

        case COLOR_24BIT:
            break;

        case COLOR_TRANSPARENCY:
            break;

        case LINE_WEIGHT:
            entity.setLineWeight(value.getIntegerValue());

            break;

        case GROUPCODE_THICKNESS:
            entity.setThickness(value.getDoubleValue());

            break;

        case GROUPCODE_MODELSPACE:
            entity.setModelSpace(value.getBooleanValue());

            break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#getEntityName()
     */
    public abstract String getDXFEntityName();

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        this.doc = null;
    }
}
