/*
   Copyright 2006 Simon Mieth

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
import org.kabeja.dxf.DXFHatch;
import org.kabeja.math.MathUtils;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class HatchLineIterator implements Iterator {
    public static final double LIMIT = 0.00001;
    protected double angle;
    protected Bounds hatchBounds;
    protected HatchLineFamily pattern;
    protected double length;
    protected Vector v;
    protected Vector r;
    protected List bounderyEdges;
    protected ParametricLine patternLine;
    protected double tmin = Double.POSITIVE_INFINITY;
    protected double tmax = Double.NEGATIVE_INFINITY;
    protected double walkingLength;
    protected double currentWalkingStep = 0;

    // public HatchLineIterator(List boundaryEdges,DXFHatch
    // hatch,HatchLineFamily lineFamily){
    // this.bounderyEdges=boundaryEdges;
    // this.hatchBounds = hatch.getBounds();
    // this.pattern=lineFamily;
    // this.initialize();
    // }
    public HatchLineIterator(DXFHatch hatch, HatchLineFamily pattern) {
        this.angle = Math.toRadians(pattern.getRotationAngle());
        this.hatchBounds = hatch.getBounds();
        this.length = pattern.getLength();

        this.bounderyEdges = new ArrayList();

        // edge 0
        Point start = new Point(this.hatchBounds.getMinimumX(),
                this.hatchBounds.getMaximumY(), 0);
        Point end = new Point(this.hatchBounds.getMinimumX(),
                this.hatchBounds.getMinimumY(), 0);
        this.bounderyEdges.add(new ParametricLine(start,
                MathUtils.getVector(start, end)));

        // edge 1
        start = new Point(this.hatchBounds.getMinimumX(),
                this.hatchBounds.getMinimumY(), 0);
        end = new Point(this.hatchBounds.getMaximumX(),
                this.hatchBounds.getMinimumY(), 0);
        this.bounderyEdges.add(new ParametricLine(start,
                MathUtils.getVector(start, end)));

        // edge 2
        start = new Point(this.hatchBounds.getMaximumX(),
                this.hatchBounds.getMinimumY(), 0);
        end = new Point(this.hatchBounds.getMaximumX(),
                this.hatchBounds.getMaximumY(), 0);
        this.bounderyEdges.add(new ParametricLine(start,
                MathUtils.getVector(start, end)));

        // edge 3
        start = new Point(this.hatchBounds.getMaximumX(),
                this.hatchBounds.getMaximumY(), 0);
        end = new Point(this.hatchBounds.getMinimumX(),
                this.hatchBounds.getMaximumY(), 0);
        this.bounderyEdges.add(new ParametricLine(start,
                MathUtils.getVector(start, end)));

        this.pattern = pattern;
        this.initialize();
    }

    public boolean hasNext() {
        return this.currentWalkingStep <= this.walkingLength;
    }

    protected void initialize() {
        // setup a length
        // this can happen on solid lines
        if (this.length == 0) {
            this.length = 1;
        }

        // first get the center point of the bound rectangle
        Point center = new Point();
        center.setX(this.hatchBounds.getMinimumX() +
            (this.hatchBounds.getWidth() / 2));
        center.setY(this.hatchBounds.getMinimumY() +
            (this.hatchBounds.getHeight() / 2));
        center.setZ(0);

        this.r = new Vector();

        if (Math.abs(this.pattern.getOffsetY()) < LIMIT) {
            this.r.setY(0);
        } else {
            this.r.setY(this.pattern.getOffsetY());
        }

        if (Math.abs(this.pattern.getOffsetX()) < LIMIT) {
            this.r.setX(0);
        } else {
            this.r.setX(this.pattern.getOffsetX());
        }

        // create the direction vector of the line family
        this.v = new Vector();
        this.v.setX(this.length * Math.cos(this.angle));
        this.v.setY(this.length * Math.sin(this.angle));

        if (Math.abs(this.v.getX()) < LIMIT) {
            this.v.setX(0);
        }

        if (Math.abs(this.v.getY()) < LIMIT) {
            this.v.setY(0);
        }

        // we will now find the next raster point near the center point
        double[] para = this.getRasterValues(center.getX(), center.getY());
        center = this.getPoint(Math.round(para[0]), Math.round(para[1]));

        // we create now our walking line
        this.patternLine = new ParametricLine(center, this.r);

        this.calculateIntersection(this.hatchBounds.getMinimumX(),
            this.hatchBounds.getMaximumY());
        this.calculateIntersection(this.hatchBounds.getMinimumX(),
            this.hatchBounds.getMinimumY());
        this.calculateIntersection(this.hatchBounds.getMaximumX(),
            this.hatchBounds.getMinimumY());
        this.calculateIntersection(this.hatchBounds.getMaximumX(),
            this.hatchBounds.getMaximumY());

        // the minimum point is our starting point
        this.tmin = Math.floor(this.tmin);
        this.tmax = Math.ceil(this.tmax);

        Point p = this.patternLine.getPointAt(this.tmin);
        this.patternLine.setStartPoint(p);
        this.walkingLength = Math.ceil(Math.abs(this.tmax - this.tmin));
    }

    protected void calculateIntersection(double x, double y) {
        Point s = new Point(x, y, 0);
        ParametricLine line = new ParametricLine(s, this.v);
        double t = this.patternLine.getIntersectionParameter(line);

        if (t < this.tmin) {
            this.tmin = t;
        }

        if (t > this.tmax) {
            this.tmax = t;
        }
    }

    /**
     * calculate the m and n raster values of a given point.
     *
     * @return the raster values, where v[0]=m and v[1]=n
     */
    protected double[] getRasterValues(double x, double y) {
        double[] v = new double[2];

        if (this.r.getX() == 0.0) {
            v[0] = (x - this.pattern.getBaseX()) / this.v.getX();
            v[1] = (y - this.pattern.getBaseY() - (this.v.getY() * v[0])) / this.r.getY();
        } else if (this.r.getY() == 0.0) {
            v[0] = (y - this.pattern.getBaseY()) / this.v.getY();
            v[1] = (x - this.pattern.getBaseX()) / this.r.getX();
        } else if (this.v.getX() == 0) {
            v[1] = (x - this.pattern.getBaseX()) / this.r.getX();
            v[0] = (y - this.pattern.getBaseY() - (this.r.getY() * v[1])) / this.v.getY();
        } else if (this.v.getY() == 0.0) {
            v[1] = (y - this.pattern.getBaseY()) / this.r.getY();
            v[0] = (x - this.pattern.getBaseX() - (this.r.getX() * v[1])) / this.v.getX();
        } else {
            // a helper variable
            double a = this.r.getY() / this.r.getX();

            v[0] = (y - this.pattern.getBaseY() - (x * a) +
                (this.pattern.getBaseX() * a)) / (this.v.getY() -
                (a * this.v.getX()));
            v[1] = (x - this.pattern.getBaseX() - (this.v.getX() * v[0])) / this.r.getX();
        }

        return v;
    }

    public Object next() {
        Point p = this.patternLine.getPointAt(this.currentWalkingStep);
        ParametricLine line = new ParametricLine(p, this.v);

        // get the next intersection of
        Iterator i = this.bounderyEdges.iterator();
        List points = new ArrayList();

        while (i.hasNext()) {
            ParametricLine edge = (ParametricLine) i.next();
            double t = edge.getIntersectionParameter(line);

            if ((t >= 0) && (t < 1)) {
                points.add(edge.getPointAt(t));
            }
        }

        double startL = 0;
        double l = 0;

        if (points.size() == 2) {
            Point start = (Point) points.get(0);
            double startT = line.getParameter(start);
            Point end = (Point) points.get(1);
            double endT = line.getParameter(end);
            startL = 0;

            if (startT > endT) {
                line.setStartPoint(end);
                startL = Math.abs(endT - Math.floor(endT)) * this.length;
            } else {
                line.setStartPoint(start);
                startL = Math.abs(startT - Math.floor(startT)) * this.length;
            }

            l = Math.abs(endT - startT) * this.length;
        }

        line.setDirectionVector(MathUtils.normalize(this.v));

        HatchLineSegment segment = new HatchLineSegment(line, l, startL,
                this.pattern.getPattern());

        this.currentWalkingStep++;

        return segment;
    }

    public void remove() {
        // we do nothing here
    }

    protected Point getPoint(double m, double n) {
        Point p = new Point();
        p.setX((n * this.r.getX()) + this.pattern.getBaseX() +
            (this.v.getX() * m));
        p.setY((n * this.r.getY()) + this.pattern.getBaseY() +
            (this.v.getY() * m));

        return p;
    }
}
