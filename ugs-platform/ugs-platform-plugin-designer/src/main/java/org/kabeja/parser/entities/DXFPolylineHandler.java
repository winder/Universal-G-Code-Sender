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
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.parser.DXFEntitiesSectionHandler;
import org.kabeja.parser.DXFValue;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth </a>
 *
 */
public class DXFPolylineHandler extends AbstractEntityHandler {
    public static final String ENTITY_NAME = "POLYLINE";
    public static final String ENTITY_VERTEX = "VERTEX";
    public static final String END_SEQUENCE = "SEQEND";
    public static final int END_SEQUENCE_CODE = -2;
    public static final int VERTEX_BULGE = 42;
    public static final int START_WIDTH = 40;
    public static final int END_WIDTH = 41;
    public static final int THICKNESS = 39;
    public static final int SURFACE_TYPE = 75;
    public static final int SUREFACE_DENSITY_ROW_COUNT = 73;
    public static final int SUREFACE_DENSITY_COLUMN_COUNT = 74;
    public static final int ROW_COUNT = 71;
    public static final int COLUMN_COUNT = 72;
    private boolean follow = true;
    private boolean parse_vertex = false;
    private DXFVertex vertex;
    private DXFPolyline polyline;

    /**
     *
     */
    public DXFPolylineHandler() {
        super();

        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#endParsing()
     */
    public void endDXFEntity() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#getEntity()
     */
    public DXFEntity getDXFEntity() {
        return polyline;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#getEntityName()
     */
    public String getDXFEntityName() {
        return DXFConstants.ENTITY_TYPE_POLYLINE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#isFollowSequence()
     */
    public boolean isFollowSequence() {
        return follow;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#parseGroup(int,
     *      org.dxf2svg.parser.DXFValue)
     */
    public void parseGroup(int groupCode, DXFValue value) {
        if ((groupCode == END_SEQUENCE_CODE) ||
                END_SEQUENCE.equals(value.getValue())) {
            polyline.addVertex(vertex);
            follow = false;

            return;
        }

        switch (groupCode) {
        case DXFEntitiesSectionHandler.ENTITY_START:

            if (ENTITY_VERTEX.equals(value.getValue())) {
                // store the old before
                if (parse_vertex) {
                    polyline.addVertex(vertex);
                } else {
                    parse_vertex = true;
                }

                vertex = new DXFVertex();
                vertex.setDXFDocument(doc);
            }

            break;

        case GROUPCODE_START_X:

            if (parse_vertex) {
                vertex.setX(value.getDoubleValue());
            }

            break;

        case GROUPCODE_START_Y:

            if (parse_vertex) {
                vertex.setY(value.getDoubleValue());
            }

            break;

        case GROUPCODE_START_Z:

            if (parse_vertex) {
                vertex.setZ(value.getDoubleValue());
            }

            break;

        case VERTEX_BULGE:

            if (parse_vertex) {
                vertex.setBulge(value.getDoubleValue());
            }

            break;

        case START_WIDTH:

            if (parse_vertex) {
                vertex.setStartWidth(value.getDoubleValue());
            } else {
                polyline.setStartWidth(value.getDoubleValue());
            }

            break;

        case END_WIDTH:

            if (parse_vertex) {
                vertex.setEndWidth(value.getDoubleValue());
            } else {
                polyline.setEndWidth(value.getDoubleValue());
            }

            break;

        case THICKNESS:
            polyline.setThickness(value.getDoubleValue());

            break;

        case SURFACE_TYPE:
            polyline.setSurefaceType(value.getIntegerValue());

            break;

        case ROW_COUNT:

            if (parse_vertex) {
                vertex.setPolyFaceMeshVertex0(value.getIntegerValue());
            } else {
                polyline.setRows(value.getIntegerValue());
            }

            break;

        case COLUMN_COUNT:

            if (parse_vertex) {
                vertex.setPolyFaceMeshVertex1(value.getIntegerValue());
            } else {
                polyline.setColumns(value.getIntegerValue());
            }

            break;

        case SUREFACE_DENSITY_ROW_COUNT:

            if (parse_vertex) {
                vertex.setPolyFaceMeshVertex2(value.getIntegerValue());
            } else {
                polyline.setSurefaceDensityRows(value.getIntegerValue());
            }

            break;

        case SUREFACE_DENSITY_COLUMN_COUNT:

            if (parse_vertex) {
                vertex.setPolyFaceMeshVertex3(value.getIntegerValue());
            } else {
                polyline.setSurefaceDensityColumns(value.getIntegerValue());
            }

            break;

        default:

            if (parse_vertex) {
                super.parseCommonProperty(groupCode, value, vertex);
            } else {
                super.parseCommonProperty(groupCode, value, polyline);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dxf2svg.parser.entities.EntityHandler#startParsing()
     */
    public void startDXFEntity() {
        follow = true;
        parse_vertex = false;
        polyline = new DXFPolyline();
    }
}
