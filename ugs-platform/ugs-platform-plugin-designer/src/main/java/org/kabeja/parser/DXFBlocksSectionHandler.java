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

import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFBlocksSectionHandler extends DXFEntitiesSectionHandler {
    public final static String SECTION_KEY = "BLOCKS";
    public final static String BLOCK_START = "BLOCK";
    public final static String BLOCK_END = "ENDBLK";
    public final static int BLOCK = 0;
    public final static int BLOCK_NAME = 2;
    public final static int BLOCK_NAME2 = 3;
    public final static int BLOCK_DESCRIPTION = 4;
    public final static int BLOCK_XREFPATHNAME = 1;
    public final static int BLOCK_BASE_X = 10;
    public final static int BLOCK_BASE_Y = 20;
    public final static int BLOCK_BASE_Z = 30;
    protected boolean parseBlockHeader = false;
    private DXFBlock block;

    /**
     *
     */
    public DXFBlocksSectionHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.DXFSectionHandler#getSectionKey()
     */
    public String getSectionKey() {
        return SECTION_KEY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.DXFSectionHandler#parseGroup(int,
     *      org.dxf2svg.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case BLOCK:

            if (BLOCK_START.equals(value.getValue())) {
                // handle
                parseBlockHeader = true;

                block = new DXFBlock();
            } else if (BLOCK_END.equals(value.getValue())) {
                // handle
                endEntity();

                doc.addDXFBlock(block);
            } else {
                // an entity
                parseBlockHeader = false;
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_NAME:

            if (parseBlockHeader) {
                block.setName(value.getValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_NAME2:

            if (parseBlockHeader) {
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_DESCRIPTION:

            if (parseBlockHeader) {
                block.setDescription(value.getValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case DXFConstants.GROUPCODE_STANDARD_LAYER:

            if (parseBlockHeader) {
                block.setLayerID(value.getValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_BASE_X:

            if (parseBlockHeader) {
                block.getReferencePoint().setX(value.getDoubleValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_BASE_Y:

            if (parseBlockHeader) {
                block.getReferencePoint().setY(value.getDoubleValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        case BLOCK_BASE_Z:

            if (parseBlockHeader) {
                block.getReferencePoint().setZ(value.getDoubleValue());
            } else {
                super.parseGroup(groupCode, value);
            }

            break;

        default:
            super.parseGroup(groupCode, value);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.DXFSectionHandler#setDXFDocument(org.dxf2svg.dxf.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        super.setDXFDocument(doc);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.DXFSectionHandler#startSection()
     */
    public void startSection() {
        parseEntity = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.DXFSectionHandler#endSection()
     */
    public void endSection() {
        // endEntity();
    }

    protected void endEntity() {
        if (parseEntity) {
            handler.endDXFEntity();

            DXFEntity entity = handler.getDXFEntity();
            block.addDXFEntity(entity);
            parseEntity = false;
        }
    }
}
