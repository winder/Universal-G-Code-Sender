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


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class HatchLineSegment {
    protected Point startPoint;
    protected Vector direction;
    protected double angle;
    protected double totalLength;
    protected double length;
    protected double currentLength;
    protected double[] pattern;
    protected double l;
    protected int index;
    protected ParametricLine line;

    /**
     *
     * @param startPoint
     * @param angle the angle in degrees
     */
    public HatchLineSegment(Point startPoint, double angle, double length) {
        this.startPoint = startPoint;
        this.angle = Math.toRadians(angle);
        this.totalLength = length;
    }

    public HatchLineSegment(Point startPoint, Vector direction, double length) {
        this.startPoint = startPoint;
        this.direction = direction;
        this.totalLength = length;
    }

    public HatchLineSegment(ParametricLine line, double length,
        double startLength, double[] pattern) {
        this.startPoint = line.getStartPoint();
        this.angle = Math.toRadians(angle);
        this.totalLength = length;
        this.currentLength = startLength;
        this.pattern = pattern;
        this.line = line;
        this.initialize(startLength);
    }

    public Point getStartPoint() {
        return this.startPoint;
    }

    public double getLength() {
        return this.totalLength;
    }

    public Point getPoint(double offset) {
        Point p = new Point();
        p.setX(this.startPoint.getX() +
            (Math.cos(this.angle) * this.totalLength));
        p.setY(this.startPoint.getY() +
            (Math.sin(this.angle) * this.totalLength));

        return p;
    }

    public Point getPointAt(double para) {
        return line.getPointAt(para);
    }

    public boolean hasNext() {
        return this.length <= totalLength;
    }

    public double next() {
        double l = this.currentLength;
        this.length += Math.abs(this.currentLength);

        if (index == pattern.length) {
            index = 0;
        }

        this.currentLength = pattern[index];
        index++;

        return l;
    }

    protected void initialize(double startLength) {
        double l = 0;

        for (int i = 0; i < pattern.length; i++) {
            l += Math.abs(pattern[i]);

            // System.out.println("test Pattern part:"+pattern[i]+" startLength="+startLength+" currentLength:"+l);
            if (l > startLength) {
                this.currentLength = l - startLength;

                if (pattern[i] < 0) {
                    //System.out.println("is empty");
                    this.currentLength *= (-1);
                }

                //System.out.println("pattern startet bei="+i+" mit length="+this.currentLength);
                this.index = i + 1;

                return;
            }
        }
    }

    public boolean isSolid() {
        return pattern.length == 0;
    }
}
