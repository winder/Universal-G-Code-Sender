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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.objects.DXFDictionary;
import org.kabeja.dxf.objects.DXFObject;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 */
public class DXFDocument {
    public static String PROPERTY_ENCODING = "encoding";
    public static final double DEFAULT_MARGIN = 5;
    private Hashtable layers = new Hashtable();
    private Hashtable blocks = new Hashtable();
    private HashMap lineTypes = new HashMap();
    private HashMap dimensionStyles = new HashMap();
    private HashMap textStyles = new HashMap();

    // the user coordinate systems
    private Hashtable ucs = new Hashtable();
    private Hashtable properties = new Hashtable();
    private List viewports = new ArrayList();
    private Bounds bounds = new Bounds();
    private double margin;
    private DXFHeader header = new DXFHeader();
    private HashMap objects = new HashMap();
    private HashMap patterns = new HashMap();
    private List views = new ArrayList();
    private DXFDictionary rootDictionary = new DXFDictionary();

    public DXFDocument() {
        // the defalut layer
        DXFLayer defaultLayer = new DXFLayer();
        defaultLayer.setDXFDocument(this);
        defaultLayer.setName(DXFConstants.DEFAULT_LAYER);
        this.layers.put(DXFConstants.DEFAULT_LAYER, defaultLayer);

        // setup the margin
        this.margin = DEFAULT_MARGIN;

        // setup the root Dictionary
        this.rootDictionary = new DXFDictionary();
        this.rootDictionary.setDXFDocument(this);
    }

    public void addDXFLayer(DXFLayer layer) {
        layer.setDXFDocument(this);
        layers.put(layer.getName(), layer);
    }

    /**
     *
     * Returns the specified layer.
     *
     * @param key
     *            The layer id
     * @return the layer or if not found the default layer (layer "0")
     */
    public DXFLayer getDXFLayer(String key) {
        if (this.layers.containsKey(key)) {
            return (DXFLayer) layers.get(key);
        }

        // retun the default layer
        if (this.layers.containsKey(DXFConstants.DEFAULT_LAYER)) {
            return (DXFLayer) layers.get(DXFConstants.DEFAULT_LAYER);
        } else {
            DXFLayer layer = new DXFLayer();
            layer.setName(DXFConstants.DEFAULT_LAYER);
            this.addDXFLayer(layer);

            return layer;
        }
    }

    /**
     * Returns true if the document contains the specified layer.
     *
     * @param layerName
     *            the layer name
     * @return true - if the document contains the layer, otherwise false
     */
    public boolean containsDXFLayer(String layerName) {
        return this.layers.containsKey(layerName);
    }

    /**
     *
     * @return the iterator over all DXFLayer of this document
     */
    public Iterator getDXFLayerIterator() {
        return layers.values().iterator();
    }

    public void addDXFLineType(DXFLineType ltype) {
        lineTypes.put(ltype.getName(), ltype);
    }

    public DXFLineType getDXFLineType(String name) {
        return (DXFLineType) lineTypes.get(name);
    }

    /**
     *
     * @return the iterator over all DXFLineTypes
     */
    public Iterator getDXFLineTypeIterator() {
        return lineTypes.values().iterator();
    }

    public void addDXFEntity(DXFEntity entity) {
        entity.setDXFDocument(this);

        DXFLayer layer = this.getDXFLayer(entity.getLayerName());
        layer.addDXFEntity(entity);
    }

    public void addDXFBlock(DXFBlock block) {
        block.setDXFDocument(this);
        this.blocks.put(block.getName(), block);
    }

    public DXFBlock getDXFBlock(String name) {
        return (DXFBlock) blocks.get(name);
    }

    /**
     *
     * @return the iterator over all DXFBlocks
     */
    public Iterator getDXFBlockIterator() {
        return blocks.values().iterator();
    }

    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public String getProperty(String key) {
        if (properties.contains(key)) {
            return (String) properties.get(key);
        }

        return null;
    }

    public boolean hasProperty(String key) {
        return this.properties.containsKey(key);
    }

    /**
     * Returns the bounds of this document
     *
     * @return
     */
    public Bounds getBounds() {
        this.bounds = new Bounds();

        Enumeration e = this.layers.elements();

        while (e.hasMoreElements()) {
            DXFLayer layer = (DXFLayer) e.nextElement();

            if (!layer.isFrozen()) {
                Bounds b = layer.getBounds();

                if (b.isValid()) {
                    this.bounds.addToBounds(b);
                }
            }
        }

        return bounds;
    }

    /**
     * Returns the bounds of this document
     *
     * @return
     */
    public Bounds getBounds(boolean onModelspace) {
        Bounds bounds = new Bounds();

        Enumeration e = this.layers.elements();

        while (e.hasMoreElements()) {
            DXFLayer layer = (DXFLayer) e.nextElement();

            if (!layer.isFrozen()) {
                Bounds b = layer.getBounds(onModelspace);

                if (b.isValid()) {
                    bounds.addToBounds(b);
                }
            }
        }

        return bounds;
    }

    /**
     * @deprecated use getBounds().getHeight() instead
     * @return
     */
    public double getHeight() {
        return this.bounds.getHeight();
    }

    /**
     * @deprecated use getBounds().getWidth() instead
     * @return
     */
    public double getWidth() {
        return this.bounds.getWidth();
    }

    public DXFHeader getDXFHeader() {
        return this.header;
    }

    public void setDXFHeader(DXFHeader header) {
        this.header = header;
    }

    public void addDXFDimensionStyle(DXFDimensionStyle style) {
        this.dimensionStyles.put(style.getName(), style);
    }

    public DXFDimensionStyle getDXFDimensionStyle(String name) {
        return (DXFDimensionStyle) this.dimensionStyles.get(name);
    }

    public Iterator getDXFDimensionStyleIterator() {
        return this.dimensionStyles.values().iterator();
    }

    public void addDXStyle(DXFStyle style) {
        this.textStyles.put(style.getName(), style);
    }

    public DXFStyle getDXFStyle(String name) {
        return (DXFStyle) this.textStyles.get(name);
    }

    public Iterator getDXFStyleIterator() {
        return this.textStyles.values().iterator();
    }

    public void removeDXFLayer(String id) {
        this.layers.remove(id);
    }

    public void addDXFViewport(DXFViewport viewport) {
        this.viewports.add(viewport);
    }

    public Iterator getDXFViewportIterator() {
        return this.viewports.iterator();
    }

    public void removeDXFViewport(DXFViewport viewport) {
        this.viewports.remove(viewport);
    }

    public void removeDXFViewport(int index) {
        this.viewports.remove(index);
    }

    public void addDXFView(DXFView view) {
        this.views.add(view);
    }

    public Iterator getDXFViewIterator() {
        return this.views.iterator();
    }

    public void addDXFObject(DXFObject obj) {
        // look if the object goes in a dictionary
        DXFDictionary d = this.rootDictionary.getDXFDictionaryForID(obj.getID());

        if (d != null) {
            d.putDXFObject(obj);
        } else {
            // is not bound to a dictionary
            HashMap type = null;

            if (this.objects.containsKey(obj.getObjectType())) {
                type = (HashMap) objects.get(obj.getObjectType());
            } else {
                type = new HashMap();
                this.objects.put(obj.getObjectType(), type);
            }

            type.put(obj.getID(), obj);
        }
    }

    /**
     * Returns the root dictionary.
     *
     * @return the root DXFDictionray
     */
    public DXFDictionary getRootDXFDictionary() {
        return this.rootDictionary;
    }

    public void setRootDXFDictionary(DXFDictionary root) {
        this.rootDictionary = root;
    }

    public List getDXFObjectsByType(String type) {
        HashMap objecttypes = (HashMap) this.objects.get(type);
        List list = new ArrayList(objecttypes.values());

        return list;
    }

    /**
     *
     * @param id,
     *            the ID of the object
     * @return the object
     */
    public DXFObject getDXFObjectByID(String id) {
        Iterator i = this.objects.values().iterator();

        while (i.hasNext()) {
            HashMap map = (HashMap) i.next();
            Object obj;

            if ((obj = map.get(id)) != null) {
                return (DXFObject) obj;
            }
        }

        // Nothing found --> search in the dictionaries
        return this.rootDictionary.getDXFObjectByID(id);
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
        Iterator i = this.getDXFLayerIterator();

        while (i.hasNext()) {
            DXFLayer layer = (DXFLayer) i.next();

            if ((entity = layer.getDXFEntityByID(id)) != null) {
                return entity;
            }
        }

        i = this.getDXFBlockIterator();

        while (i.hasNext()) {
            DXFBlock block = (DXFBlock) i.next();

            if ((entity = block.getDXFEntityByID(id)) != null) {
                return entity;
            }
        }

        return entity;
    }

    /**
     * Adds a DXFHatchPattern to the document.
     *
     * @param pattern
     */
    public void addDXFHatchPattern(DXFHatchPattern pattern) {
        this.patterns.put(pattern.getID(), pattern);
    }

    /**
     *
     * @return java.util.Iterator over all DXFHatchPattern of the document
     */
    public Iterator getDXFHatchPatternIterator() {
        return this.patterns.values().iterator();
    }

    /**
     *
     * @param ID
     *            of the pattern (also called pattern name)
     * @return the DXFHatchPattern or null
     */
    public DXFHatchPattern getDXFHatchPattern(String id) {
        return (DXFHatchPattern) this.patterns.get(id);
    }
}
