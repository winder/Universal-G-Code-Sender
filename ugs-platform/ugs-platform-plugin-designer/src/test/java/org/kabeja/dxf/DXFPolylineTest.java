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

import junit.framework.TestCase;

import org.kabeja.dxf.helpers.Point;


public class DXFPolylineTest extends TestCase {
    public final static double DELTA = 0.0000000001;

    public void testBounds() {
        DXFPolyline pl = new DXFPolyline();
        pl.addVertex(new DXFVertex());
        pl.addVertex(new DXFVertex(new Point(100, 100, 0)));

        Bounds b = pl.getBounds();
        assertEquals(100, b.getWidth(), 0.0);
        assertEquals(100, b.getHeight(), 0.0);
    }

    public void testBulgedBoundsEqualsNegativeRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(1.0);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        assertEquals(100, b.getWidth(), DELTA);
        assertEquals(50, b.getHeight(), DELTA);
        assertEquals(0, b.getMaximumY(), DELTA);
        assertEquals(-50, b.getMinimumY(), DELTA);
    }

    public void testBulgedBoundsEqualsPositiveRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(-1.0);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        assertEquals(100, b.getWidth(), DELTA);
        assertEquals(50, b.getHeight(), DELTA);
        assertEquals(50, b.getMaximumY(), DELTA);
        assertEquals(0, b.getMinimumY(), DELTA);
    }

    public void testBulgedBoundsLowerPositiveRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(-.5);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        assertEquals(100, b.getWidth(), DELTA);
        assertEquals(25, b.getHeight(), DELTA);
        assertEquals(25, b.getMaximumY(), DELTA);
        assertEquals(0, b.getMinimumY(), DELTA);
    }

    public void testBulgedBoundsLowerNegativeRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(.5);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        assertEquals(100, b.getWidth(), DELTA);
        assertEquals(25, b.getHeight(), DELTA);
        assertEquals(0, b.getMaximumY(), DELTA);
        assertEquals(-25, b.getMinimumY(), DELTA);
    }

    public void testBulgedBoundsGreaterPositiveRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(-2.0);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        double r = 100 / (2 * Math.sin(((4 * Math.atan(v.getBulge())) / 2)));
        r = Math.abs(r);
        assertEquals(2 * r, b.getWidth(), DELTA);
        assertEquals(100, b.getHeight(), DELTA);
        assertEquals(100, b.getMaximumY(), DELTA);
        assertEquals(0, b.getMinimumY(), DELTA);
    }

    public void testBulgedBoundsGreaterNegativeRadius() {
        DXFPolyline pl = new DXFPolyline();
        DXFVertex v = new DXFVertex(new Point(0, 0, 0));
        v.setBulge(2.0);
        pl.addVertex(v);

        DXFVertex v2 = new DXFVertex(new Point(100, 0.0, 0));
        pl.addVertex(v2);

        Bounds b = pl.getBounds();

        double r = 100 / (2 * Math.sin(((4 * Math.atan(v.getBulge())) / 2)));
        r = Math.abs(r);
        assertEquals(2 * r, b.getWidth(), DELTA);
        assertEquals(100, b.getHeight(), DELTA);
        assertEquals(0, b.getMaximumY(), DELTA);
        assertEquals(-100, b.getMinimumY(), DELTA);
    }

    public void testBulgedLength() {
    }
}
