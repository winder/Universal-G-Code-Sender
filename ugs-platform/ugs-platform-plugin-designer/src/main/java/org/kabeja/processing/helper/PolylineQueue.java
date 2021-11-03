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
package org.kabeja.processing.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFUtils;
import org.kabeja.dxf.helpers.Point;


public class PolylineQueue {
    private List elements = new ArrayList();
    private Point startPoint;
    private Point endPoint;
    private double radius = DXFConstants.POINT_CONNECTION_RADIUS;

    public PolylineQueue(DXFEntity e, Point start, Point end, double radius) {
        this.elements.add(e);
        this.startPoint = start;
        this.endPoint = end;
        this.radius = radius;
    }

    public int size() {
        return this.elements.size();
    }

    /**
     * connect a DXF entity if possible.
     *
     * @param e
     * @param start
     * @param end
     * @return true if the entity could be connected, otherwise false
     */
    public boolean connectDXFEntity(DXFEntity e, Point start, Point end) {
        if (DXFUtils.equals(this.startPoint, end, radius)) {
            this.startPoint = start;
            this.elements.add(0, e);

            return true;
        } else if (DXFUtils.equals(this.endPoint, start, radius)) {
            this.endPoint = end;
            this.elements.add(e);

            return true;
        } else if (DXFUtils.equals(this.startPoint, start, radius)) {
            // we need to reverse then the entity
            this.startPoint = end;
            reverse(e);
            this.elements.add(0, e);

            return true;
        } else if (DXFUtils.equals(this.endPoint, end, radius)) {
            // we need to reverse then the entity
            this.endPoint = start;
            reverse(e);
            this.elements.add(e);

            return true;
        }

        return false;
    }

    public Point getStartPoint() {
        return this.startPoint;
    }

    public Point getEndPoint() {
        return this.endPoint;
    }

    public Iterator getElementIterator() {
        return this.elements.iterator();
    }

    public boolean connect(PolylineQueue queue) {
        if (DXFUtils.equals(queue.getStartPoint(), this.endPoint, radius)) {
            // add to the end
            add(queue);

            return true;
        } else if (DXFUtils.equals(queue.getEndPoint(), this.startPoint, radius)) {
            // insert before
            insertBefore(queue);

            return true;
        } else if (DXFUtils.equals(queue.getStartPoint(), this.startPoint,
                    radius)) {
            queue.reverse();
            insertBefore(queue);

            return true;
        } else if (DXFUtils.equals(queue.getEndPoint(), this.endPoint, radius)) {
            queue.reverse();
            add(queue);

            return true;
        }

        return false;
    }

    public void createDXFPolyline(DXFLayer layer) {
        // create the polyline and remove the entity
        DXFPolyline pline = new DXFPolyline();
        DXFVertex first = new DXFVertex(this.startPoint);
        pline.addVertex(first);

        Iterator i = this.elements.iterator();

        while (i.hasNext()) {
            DXFEntity e = (DXFEntity) i.next();

            if (DXFConstants.ENTITY_TYPE_LINE.equals(e.getType())) {
                DXFLine line = (DXFLine) e;
                first = new DXFVertex(line.getEndPoint());
                pline.addVertex(first);
            } else if (DXFConstants.ENTITY_TYPE_POLYLINE.equals(e.getType()) ||
                    DXFConstants.ENTITY_TYPE_LWPOLYLINE.equals(e.getType())) {
                DXFPolyline pl = (DXFPolyline) e;
                double bulge = pl.getVertex(0).getBulge();

                if (bulge != 0.0) {
                    first.setBulge(bulge);
                }

                for (int x = 1; x < pl.getVertexCount(); x++) {
                    first = pl.getVertex(x);
                    pline.addVertex(first);
                }
            } else if (DXFConstants.ENTITY_TYPE_ARC.equals(e.getType())) {
                DXFArc arc = (DXFArc) e;

                if (arc.getTotalAngle() > 0.0) {
                    double h = arc.getRadius() * (1 -
                        Math.cos(Math.toRadians(arc.getTotalAngle() / 2)));
                    double chordLength = arc.getChordLength();

                    if (DXFUtils.equals(arc.getStartPoint(), first.getPoint(),
                                radius)) {
                        // the last point is our start point,
                        // which is always set
                        // we have to calculate the bulge
                        first.setBulge((2 * h) / chordLength);
                        first = new DXFVertex(arc.getEndPoint());
                        pline.addVertex(first);
                    } else {
                        // reverse the arc, we change the start/end points
                        // and set the bulge to >0
                        first.setBulge(-1.0 * ((2 * h) / chordLength));

                        first = new DXFVertex(arc.getStartPoint());
                        pline.addVertex(first);
                    }
                }
            }

            // remove from layer
            layer.removeDXFEntity(e);
        }

        // add the new polyline to the layer
        pline.setLayerName(layer.getName());
        layer.addDXFEntity(pline);
    }

    protected void reverse(DXFEntity entity) {
        if (DXFConstants.ENTITY_TYPE_LINE.equals(entity.getType())) {
            DXFUtils.reverseDXFLine((DXFLine) entity);
        } else if (DXFConstants.ENTITY_TYPE_POLYLINE.equals(entity.getType()) ||
                DXFConstants.ENTITY_TYPE_LWPOLYLINE.equals(entity.getType())) {
            DXFUtils.reverseDXFPolyline((DXFPolyline) entity);
        } else if (DXFConstants.ENTITY_TYPE_ARC.equals(entity.getType())) {
            // we cannot reverse a DXF ARC
        }
    }

    protected void reverse() {
        Point p = this.endPoint;
        this.endPoint = this.startPoint;
        this.startPoint = p;

        // reverse the list and all entities
        int last = this.elements.size() - 1;

        for (int i = 0; i < (last + 1); i++) {
            DXFEntity first = (DXFEntity) this.elements.get(i);
            this.reverse(first);

            if (i < last) {
                DXFEntity e = (DXFEntity) this.elements.set(last, first);
                this.reverse(e);
                this.elements.set(i, e);
                last--;
            }
        }
    }

    /**
     * Insert the PolylineQueue before the first element.
     *
     * @param queue
     */
    public void insertBefore(PolylineQueue queue) {
        this.startPoint = queue.getStartPoint();

        Iterator i = queue.getElementIterator();
        int x = 0;

        while (i.hasNext()) {
            DXFEntity e = (DXFEntity) i.next();
            this.elements.add(x, e);
            x++;
        }
    }

    /**
     * Adds the queue to the end.
     *
     * @param queue
     */
    public void add(PolylineQueue queue) {
        this.endPoint = queue.getEndPoint();

        Iterator i = queue.getElementIterator();

        while (i.hasNext()) {
            DXFEntity e = (DXFEntity) i.next();
            this.elements.add(e);
        }
    }
}
