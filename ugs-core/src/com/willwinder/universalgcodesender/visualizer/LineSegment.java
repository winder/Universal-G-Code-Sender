/*
 * 
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013 Noah Levy

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willwinder.universalgcodesender.visualizer;

import javax.vecmath.Point3d;
 
public class LineSegment {

    private int toolhead = 0; //DEFAULT TOOLHEAD ASSUMED TO BE 0!
    private double speed;
    private final Point3d first, second;
    
    // Line properties
    private boolean isZMovement = false;
    private boolean isArc = false;
    private boolean isFastTraverse = false;
    private final int lineNumber;
    
    public LineSegment (final Point3d a,final Point3d b, int num)
    {
        first = new Point3d(a);
        second = new Point3d (b);
        lineNumber = num;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public Point3d[] getPointArray()
    {
        Point3d[] pointarr = { first, second };
        return pointarr;
    }
    
    public double[] getPoints()
    {
        double[] points = {first.x, first.y, first.z , second.x, second.y, second.z };
        return points;
    }
    
    public Point3d getStart() {
        return this.first;
    }

    public Point3d getEnd() {
        return this.second;
    }

    public void setToolHead(int head) {
        this.toolhead = head;
    }
    
    public int getToolhead()
    {
        return toolhead;
    }
    
    public void setSpeed(double s) {
        this.speed = s;
    }
    
    public double getSpeed()
    {
        return speed;
    }
    
    public void setIsZMovement(boolean isZ) {
        this.isZMovement = isZ;
    }
    
    public boolean isZMovement() {
        return isZMovement;
    }

    public void setIsArc(boolean isA) {
        this.isArc = isA;
    }
    
    public boolean isArc() {
        return isArc;
    }
    
    public void setIsFastTraverse(boolean isF) {
        this.isFastTraverse = isF;
    }
    
    public boolean isFastTraverse() {
        return this.isFastTraverse;
    }
}