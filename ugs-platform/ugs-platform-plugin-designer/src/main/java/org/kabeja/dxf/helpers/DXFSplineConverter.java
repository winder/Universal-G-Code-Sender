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
package org.kabeja.dxf.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.math.NURBS;
import org.kabeja.math.NURBSFixedNTELSPointIterator;


public class DXFSplineConverter {
    public static DXFPolyline toDXFPolyline(DXFSpline spline) {
        DXFPolyline p = new DXFPolyline();
        p.setDXFDocument(spline.getDXFDocument());

        if ((spline.getDegree() > 0) && (spline.getKnots().length > 0)) {
            Iterator pi = new NURBSFixedNTELSPointIterator(toNurbs(spline), 30);

            while (pi.hasNext()) {
                p.addVertex(new DXFVertex((Point) pi.next()));
            }
        } else {
            // the curve is the controlpoint polygon
            Iterator i = spline.getSplinePointIterator();

            while (i.hasNext()) {
                SplinePoint sp = (SplinePoint) i.next();

                if (sp.isControlPoint()) {
                    p.addVertex(new DXFVertex(sp));
                }
            }
        }

        if (spline.isClosed()) {
            p.setFlags(1);
        }

        return p;
    }

    public static NURBS toNurbs(DXFSpline spline) {
        Iterator i = spline.getSplinePointIterator();
        ArrayList list = new ArrayList();

        while (i.hasNext()) {
            SplinePoint sp = (SplinePoint) i.next();

            if (sp.isControlPoint()) {
                list.add((Point) sp);
            }
        }

        NURBS n = new NURBS((Point[]) list.toArray(new Point[list.size()]),
                spline.getKnots(), spline.getWeights(), spline.getDegree());
        n.setClosed(spline.isClosed());

        return n;
    }
}
