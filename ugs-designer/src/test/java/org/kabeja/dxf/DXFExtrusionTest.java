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
package org.kabeja.dxf;

import junit.framework.TestCase;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;


public class DXFExtrusionTest extends TestCase {
    double DELTA = 0.0000000001;

    public void testLineExtrusion() {
        DXFLine line = new DXFLine();
        line.setStartPoint(new Point(0, 0, 0));
        line.setEndPoint(new Point(100, 100, 0));
        line.setThickness(10.0);

        DXFExtrusion e = line.getExtrusion();
        Point p1 = e.extrudePoint(line.getStartPoint(), line.getThickness());
        Point p2 = e.extrudePoint(line.getEndPoint(), line.getThickness());
        assertEquals(10.0, p1.getZ(), DELTA);
        assertEquals(10.0, p2.getZ(), DELTA);
    }

    public void testLinePlaneExtrusion() {
        DXFLine line = new DXFLine();
        line.setStartPoint(new Point(0, 0, 0));
        line.setEndPoint(new Point(100, 100, 0));
        line.setThickness(10.0);

        DXFExtrusion e = line.getExtrusion();
        Vector v1 = e.getDirectionX();
        Vector v2 = e.getDirectionY();

        assertEquals(1.0, v1.getX(), DELTA);
        assertEquals(0.0, v1.getY(), DELTA);
        assertEquals(0.0, v1.getZ(), DELTA);
        assertEquals(0.0, v2.getX(), DELTA);
        assertEquals(1.0, v2.getY(), DELTA);
        assertEquals(0.0, v2.getZ(), DELTA);
    }
}
