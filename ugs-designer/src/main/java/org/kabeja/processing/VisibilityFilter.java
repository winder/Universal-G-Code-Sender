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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;


/**
 * This postprocessor remove all invisible entities and layers.
 *
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class VisibilityFilter extends AbstractPostProcessor {
    /* (non-Javadoc)
     * @see org.kabeja.tools.PostProcessor#process(org.kabeja.dxf.DXFDocument)
     */
    public void process(DXFDocument doc, Map context) throws ProcessorException {
        Iterator i = doc.getDXFLayerIterator();

        while (i.hasNext()) {
            DXFLayer l = (DXFLayer) i.next();

            if (l.isVisible()) {
                Iterator inner = l.getDXFEntityTypeIterator();

                while (inner.hasNext()) {
                    String type = (String) inner.next();
                    List entities = l.getDXFEntities(type);
                    Iterator ei = entities.iterator();

                    while (ei.hasNext()) {
                        DXFEntity entity = (DXFEntity) ei.next();

                        if (!entity.isVisibile()) {
                            ei.remove();
                        }
                    }
                }
            } else {
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kabeja.tools.PostProcessor#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        // TODO Auto-generated method stub
    }
}
