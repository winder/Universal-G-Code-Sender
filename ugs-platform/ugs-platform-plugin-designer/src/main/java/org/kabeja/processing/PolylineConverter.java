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
package org.kabeja.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.processing.helper.PolylineQueue;


public class PolylineConverter extends AbstractPostProcessor {
    public final static String PROPERTY_POINT_DISTANCE = "point.distance";
    private List queues;
    private double radius = DXFConstants.POINT_CONNECTION_RADIUS;

    public void process(DXFDocument doc, Map context) throws ProcessorException {
        Iterator i = doc.getDXFLayerIterator();

        while (i.hasNext()) {
            DXFLayer layer = (DXFLayer) i.next();

            processLayer(layer);
        }

        // TODO process the blocks too
    }

    public void setProperties(Map properties) {
        if (properties.containsKey(PROPERTY_POINT_DISTANCE)) {
            this.radius = Double.parseDouble((String) properties.get(
                        PROPERTY_POINT_DISTANCE));
        }
    }

    protected void processLayer(DXFLayer layer) {
        this.queues = new ArrayList();

        // check the lines
        if (layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_LINE)) {
            List l = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);
            Iterator i = l.iterator();

            while (i.hasNext()) {
                DXFLine line = (DXFLine) i.next();
                Point start = line.getStartPoint();
                Point end = line.getEndPoint();
                checkDXFEntity(line, start, end);
            }
        }

        // check the polylines
        if (layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_POLYLINE)) {
            List l = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POLYLINE);
            Iterator i = l.iterator();

            while (i.hasNext()) {
                DXFPolyline pl = (DXFPolyline) i.next();

                if (!pl.isClosed() && !pl.is3DPolygonMesh() &&
                        !pl.isClosedMeshMDirection() &&
                        !pl.isClosedMeshNDirection() &&
                        !pl.isCubicSurefaceMesh()) {
                    Point start = pl.getVertex(0).getPoint();
                    Point end = pl.getVertex(pl.getVertexCount() - 1).getPoint();
                    checkDXFEntity(pl, start, end);
                }
            }
        }

        // check the lwpolylines
        if (layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
            List l = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            Iterator i = l.iterator();

            while (i.hasNext()) {
                DXFLWPolyline pl = (DXFLWPolyline) i.next();

                if (!pl.isClosed() && !pl.is3DPolygonMesh() &&
                        !pl.isClosedMeshMDirection() &&
                        !pl.isClosedMeshNDirection() &&
                        !pl.isCubicSurefaceMesh()) {
                    Point start = pl.getVertex(0).getPoint();
                    Point end = pl.getVertex(pl.getVertexCount() - 1).getPoint();
                    checkDXFEntity(pl, start, end);
                }
            }
        }

        // check the arcs
        if (layer.hasDXFEntities(DXFConstants.ENTITY_TYPE_ARC)) {
            List l = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_ARC);
            Iterator i = l.iterator();

            while (i.hasNext()) {
                DXFArc arc = (DXFArc) i.next();

                // note that this points are calculated
                // and could be not connected to the rest
                // even though they should in you CAD
                Point start = arc.getStartPoint();
                Point end = arc.getEndPoint();
                checkDXFEntity(arc, start, end);
            }
        }

        // finish up the connection search
        // and connect parts, if it is possible
        connectPolylineQueues();

        cleanUp(layer);
    }

    protected void checkDXFEntity(DXFEntity e, Point start, Point end) {
        Iterator i = this.queues.iterator();

        while (i.hasNext()) {
            PolylineQueue queue = (PolylineQueue) i.next();

            if (queue.connectDXFEntity(e, start, end)) {
                return;
            }
        }

        // nothing found create a new queue
        PolylineQueue queue = new PolylineQueue(e, start, end, this.radius);

        this.queues.add(queue);
    }

    protected void cleanUp(DXFLayer layer) {
        Iterator i = this.queues.iterator();

        while (i.hasNext()) {
            PolylineQueue queue = (PolylineQueue) i.next();

            if (queue.size() > 1) {
                queue.createDXFPolyline(layer);
            } else {
                // ignore
                i.remove();
            }
        }
    }

    /**
     * Goes through all polylinequeues and connect them, if they have the same
     * start and end points.
     *
     */
    protected void connectPolylineQueues() {
        for (int i = 0; i < this.queues.size(); i++) {
            PolylineQueue queue = (PolylineQueue) this.queues.get(i);

            boolean connected = false;

            //inner loop -> test all following polylines if
            //we can connect
            for (int x = i + 1; (x < this.queues.size()) && !connected; x++) {
                if (((PolylineQueue) this.queues.get(x)).connect(queue)) {
                    this.queues.remove(i);
                    i--;
                    connected = true;
                }
            }
        }
    }
}
