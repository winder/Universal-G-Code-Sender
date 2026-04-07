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
package org.kabeja.math;

import junit.framework.TestCase;

import org.kabeja.dxf.helpers.Point;


public class NURBSTest extends TestCase {
    public void testPointAt() {
        Point[] points = new Point[] {
                new Point(0, 0, 0), new Point(1.0, 1.0, 0.0),
                new Point(3.0, 2.0, 0), new Point(4.0, 1.0, 0),
                new Point(5.0, -1.0, 0)
            };
        double[] knots = new double[] { 0.0, 0.0, 0.0, 1.0, 2.0, 3.0, 3.0, 3.0 };
        double[] w = new double[] { 1.0, 4.0, 1.0, 1.0, 1.0 };
        NURBS n = new NURBS(points, knots, w, 2);
        int index = n.findSpawnIndex(1.0);

        Point p = n.getPointAt(index, 1.0);

        assertEquals(7.0 / 5.0, p.getX(), 0.001);
        assertEquals(6.0 / 5.0, p.getY(), 0.001);
    }

    public void testBaseFunctions1() {
        Point[] points = new Point[] {  };
        double[] knots = new double[] { 0, 0, 0, 1, 2, 3, 4, 4, 5, 5 };
        double[] w = new double[] {  };
        NURBS n = new NURBS(points, knots, w, 2);
        double[] bf = n.getBasicFunctions(4, 2.5);

        assertEquals(3, bf.length);
        assertEquals(1.0 / 8.0, bf[0], 0.00001);
        assertEquals(6.0 / 8.0, bf[1], 0.00001);
        assertEquals(1.0 / 8.0, bf[2], 0.00001);
    }

    public void testBaseFunctions2() {
        Point[] points = new Point[] {
                new Point(0, 0, 0), new Point(1, 1, 0), new Point(3, 2, 0),
                new Point(4, 1, 0), new Point(5, -1, 0)
            };
        double[] knots = new double[] { 0, 0, 0, 1, 2, 3, 4, 4, 5, 5, 5 };
        double[] w = new double[] {  };
        NURBS n = new NURBS(points, knots, w, 2);
        int index = n.findSpawnIndex(2.5);

        double[] bf = n.getBasicFunctions(index, 2.5);

        assertEquals(3, bf.length);
        assertEquals(1.0 / 8.0, bf[0], 0.00001);
        assertEquals(6.0 / 8.0, bf[1], 0.00001);
        assertEquals(1.0 / 8.0, bf[2], 0.00001);
    }
}
