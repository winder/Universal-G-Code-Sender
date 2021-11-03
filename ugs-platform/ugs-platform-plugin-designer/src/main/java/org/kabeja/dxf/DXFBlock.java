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
package org.kabeja.dxf;

import java.util.ArrayList;
import java.util.Iterator;

import org.kabeja.dxf.helpers.Point;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFBlock {
    public static String TYPE = "BLOCK";
    private Point referencePoint;
    private String layerID = DXFConstants.DEFAULT_LAYER;
    private String name = "";
    private String description = "";
    private ArrayList entities;
    private DXFDocument doc;

    /**
     *
     */
    public DXFBlock() {
        super();

        this.entities = new ArrayList();
        this.referencePoint = new Point();
    }

    public Bounds getBounds() {
        // first set the own point
        Bounds bounds = new Bounds();
        Iterator i = entities.iterator();

        if (i.hasNext()) {
            while (i.hasNext()) {
                DXFEntity entity = (DXFEntity) i.next();
                Bounds b = entity.getBounds();

                if (b.isValid()) {
                    bounds.addToBounds(b);
                }
            }
        } else {
            bounds.setValid(false);
        }

        return bounds;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the p.
     */
    public Point getReferencePoint() {
        return referencePoint;
    }

    /**
     * @param p
     *            The p to set.
     */
    public void setReferencePoint(Point p) {
        this.referencePoint = p;
    }

    public void addDXFEntity(DXFEntity entity) {
        entities.add(entity);
    }

    /**
     *
     * @return a iterator over all entities of this block
     */
    public Iterator getDXFEntitiesIterator() {
        return entities.iterator();
    }

    /**
     * @return Returns the layerID.
     */
    public String getLayerID() {
        return layerID;
    }

    /**
     * @param layerID
     *            The layerID to set.
     */
    public void setLayerID(String layerID) {
        this.layerID = layerID;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param doc
     *            The doc to set.
     */
    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;

        Iterator i = entities.iterator();

        while (i.hasNext()) {
            DXFEntity entity = (DXFEntity) i.next();
            entity.setDXFDocument(doc);
        }
    }

    /**
     *
     * @return the parent document
     */
    public DXFDocument getDXFDocument() {
        return this.doc;
    }

    public double getLength() {
        double length = 0;
        Iterator i = entities.iterator();

        while (i.hasNext()) {
            DXFEntity entity = (DXFEntity) i.next();
            length += entity.getLength();
        }

        return length;
    }

    /**
     * Gets the
     *
     * @see DXFEntity with the specified ID.
     * @param id
     *            of the
     * @see DXFEntity
     * @return the
     * @see DXFEntity with the specified ID or null if there is no
     * @see DXFEntity with the specified ID
     */
    public DXFEntity getDXFEntityByID(String id) {
        DXFEntity entity = null;
        Iterator i = this.entities.iterator();

        while (i.hasNext()) {
            DXFEntity e = (DXFEntity) i.next();

            if (e.getID().equals(id)) {
                return e;
            }
        }

        return entity;
    }
}
