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
package org.kabeja.dxf.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFEntity;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class HatchBoundaryLoop {
    private List edges = new ArrayList();
    private boolean outermost = true;

    /**
     * @return Returns the outermost.
     */
    public boolean isOutermost() {
        return outermost;
    }

    /**
     * @param outermost
     *            The outermost to set.
     */
    public void setOutermost(boolean outermost) {
        this.outermost = outermost;
    }

    public Iterator getBoundaryEdgesIterator() {
        return edges.iterator();
    }

    public void addBoundaryEdge(DXFEntity edge) {
        edges.add(edge);
    }

    public Bounds getBounds() {
        Bounds bounds = new Bounds();

        // System.out.println("edges="+edges.size());
        if (edges.size() > 0) {
            Iterator i = edges.iterator();

            while (i.hasNext()) {
                DXFEntity entity = (DXFEntity) i.next();
                Bounds b = entity.getBounds();

                if (b.isValid()) {
                    bounds.addToBounds(b);
                }
            }

            return bounds;
        } else {
            bounds.setValid(false);

            return bounds;
        }
    }

    public int getEdgeCount() {
        return this.edges.size();
    }
}
