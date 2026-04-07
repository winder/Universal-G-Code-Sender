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

import java.util.Iterator;

import org.kabeja.dxf.helpers.Point;


public class NURBSFixedNTELSPointIterator implements Iterator {
    private NURBS nurbs;
    private int ntels;
    private double dt = 0;
    private double t = 0;
    private int interval;
    private int lastInterval;

    /**
     *
     * @param nurbs
     *            The NURBS curve to draw
     * @param ntels
     *            the ntels per interval to use
     */
    public NURBSFixedNTELSPointIterator(NURBS nurbs, int ntels) {
        this.nurbs = nurbs;
        this.ntels = ntels;

        if (this.nurbs.getKnots().length == (this.nurbs.getDegree() +
                this.nurbs.controlPoints.length + 1)) {
            this.lastInterval = this.nurbs.getKnots().length -
                this.nurbs.getDegree() - 1;
            this.interval = this.nurbs.getDegree();
        } else if (this.nurbs.getKnots().length > 0) {
            // find self the start and end interval
            this.interval = 0;

            double start = this.nurbs.getKnots()[0];

            while (start == this.nurbs.getKnots()[this.interval + 1]) {
                this.interval++;
            }

            this.lastInterval = this.nurbs.getKnots().length - 1;

            double end = this.nurbs.getKnots()[this.lastInterval];

            while (end == this.nurbs.getKnots()[this.lastInterval]) {
                this.lastInterval--;
            }
        }

        //init t
        this.t = this.nurbs.getKnots()[this.nurbs.getDegree()];
        this.nextInterval();

        //fix for some broken nurbs
        if ((this.interval - 1) < this.nurbs.getDegree()) {
            this.interval = this.nurbs.getDegree() + 1;
        }
    }

    public boolean hasNext() {
        if (this.t < this.nurbs.getKnots()[this.interval]) {
            return true;
        } else if (this.interval < this.lastInterval) {
            this.nextInterval();

            return hasNext();
        }

        return false;
    }

    public Object next() {
        Point p = this.nurbs.getPointAt(this.interval - 1, t);
        //		System.out.println("t="+t);
        //		Point p = this.nurbs.getPointAt(t);
        this.t += this.dt;

        return p;
    }

    public void remove() {
        //nothing todo here
    }

    protected void nextInterval() {
        this.interval++;

        while ((this.t > this.nurbs.getKnots()[this.interval]) &&
                (this.interval < this.lastInterval)) {
            this.interval++;
        }

        double length = this.nurbs.getKnots()[this.interval] - this.t;
        this.dt = length / this.ntels;
    }
}
