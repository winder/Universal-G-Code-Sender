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

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFText;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class DXFTextHandler extends AbstractEntityHandler {
    public static final int TEXT_VALUE = 1;
    public static final int TEXT_HEIGHT = 40;
    public static final int TEXT_SCALEX = 41;
    public static final int TEXT_GENERATION_FLAG = 71;
    public static final int TEXT_ALIGN = 72;
    public static final int TEXT_VALIGN = 73;
    public static final int TEXT_ALIGN_X = 11;
    public static final int TEXT_ALIGN_Y = 21;
    public static final int TEXT_ALIGN_Z = 31;
    public static final int TEXT_STYLE = 7;
    public static final int TEXT_OBLIQUEANGLE = 51;
    public static final int TEXT_ROTATION = 50;
    protected DXFText text;
    protected String content;

    /**
     *
     */
    public DXFTextHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#endParsing()
     */
    public void endDXFEntity() {
        text.setText(this.content);
        this.content = "";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#getEntity()
     */
    public DXFEntity getDXFEntity() {
        return text;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#getEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_TEXT;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#parseGroup(int,
     *      org.dxf2svg.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case TEXT_VALUE:
            //we set the content after the
            //parsing is finished, so the
            //DXFParser will get all infos
            this.content = value.getValue();

            break;

        case TEXT_ALIGN:
            text.setAlign(value.getIntegerValue());

            break;

        case TEXT_VALIGN:
            text.setValign(value.getIntegerValue());

            break;

        case GROUPCODE_START_X:
            text.setX(value.getDoubleValue());

            break;

        case GROUPCODE_START_Y:
            text.setY(value.getDoubleValue());

            break;

        case GROUPCODE_START_Z:
            text.setZ(value.getDoubleValue());

            break;

        case TEXT_ALIGN_X:
            text.setAlignX(value.getDoubleValue());
            text.setAlignmentPoint(true);

            break;

        case TEXT_ALIGN_Y:
            text.setAlignY(value.getDoubleValue());
            text.setAlignmentPoint(true);

            break;

        case TEXT_ALIGN_Z:
            text.setAlignZ(value.getDoubleValue());
            text.setAlignmentPoint(true);

            break;

        case TEXT_HEIGHT:
            text.setHeight(value.getDoubleValue());

            break;

        case TEXT_GENERATION_FLAG:

            switch (value.getIntegerValue()) {
            case 2:
                text.setBackward(true);

                break;

            case 4:
                text.setUpsideDown(true);

                break;
            }

            break;

        case TEXT_STYLE:
            text.setTextStyle(value.getValue());

            break;

        case TEXT_ROTATION:
            text.setRotation(value.getDoubleValue());

            break;

        case TEXT_SCALEX:
            text.setScaleX(value.getDoubleValue());

            break;

        case TEXT_OBLIQUEANGLE:
            text.setObliqueAngle(value.getDoubleValue());

            break;

        default:
            super.parseCommonProperty(groupCode, value, text);

            break;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#startParsing()
     */
    public void startDXFEntity() {
        text = new DXFText();
        text.setDXFDocument(this.doc);
    }
}
