/*
   Copyright 2008 Simon Mieth

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;


public class LayerFilter extends AbstractPostProcessor {
    public final static String PROPERTY_REMOVE_LAYERS = "layers.remove";
    public final static String PROPERTY_MERGE_LAYERS = "layers.merge";
    public final static String MERGED_LAYER_NAME = "ALL";
    protected boolean merge = false;
    protected Set removableLayers = new HashSet();

    public void setProperties(Map properties) {
        super.setProperties(properties);

        if (properties.containsKey(PROPERTY_MERGE_LAYERS)) {
            this.merge = Boolean.valueOf((String) properties.get(
                        PROPERTY_MERGE_LAYERS)).booleanValue();
        }

        if (properties.containsKey(PROPERTY_REMOVE_LAYERS)) {
            this.removableLayers.clear();

            StringTokenizer st = new StringTokenizer((String) properties.get(
                        PROPERTY_REMOVE_LAYERS), "|");

            while (st.hasMoreTokens()) {
                this.removableLayers.add(st.nextToken());
            }
        }
    }

    public void process(DXFDocument doc, Map context) throws ProcessorException {
        DXFLayer mergeLayer = null;

        if (this.merge) {
            if (doc.containsDXFLayer(MERGED_LAYER_NAME)) {
                mergeLayer = doc.getDXFLayer(MERGED_LAYER_NAME);
            } else {
                mergeLayer = new DXFLayer();
                mergeLayer.setName(MERGED_LAYER_NAME);
                doc.addDXFLayer(mergeLayer);
            }
        }

        // iterate over all layers
        Iterator i = doc.getDXFLayerIterator();

        while (i.hasNext()) {
            DXFLayer layer = (DXFLayer) i.next();

            if (this.removableLayers.contains(layer.getName())) {
                i.remove();
            } else if (this.merge) {
                if (layer != mergeLayer) {
                    Iterator types = layer.getDXFEntityTypeIterator();

                    while (types.hasNext()) {
                        String type = (String) types.next();
                        Iterator entityIterator = layer.getDXFEntities(type)
                                                       .iterator();

                        while (entityIterator.hasNext()) {
                            DXFEntity e = (DXFEntity) entityIterator.next();
                            // we set all entities to the merged layer
                            // and remove them from the last layer
                            e.setLayerName(MERGED_LAYER_NAME);

                            // set again to the doc, which will
                            // place the entity on the right
                            // layer -> the LAYER = "ALL"
                            doc.addDXFEntity(e);
                            entityIterator.remove();
                        }
                    }

                    // remove the layer
                    i.remove();
                }
            }
        }
    }
}
