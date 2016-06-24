/*
 * An optimized LineSegment which only uses the end point with the expectation
 * that a collection of points will represent a continuous set of line segments.
 *
 * Created on Nov 9, 2013
 */

/*
    Copywrite 2013 Will Winder

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
package com.willwinder.universalgcodesender.types;

import com.willwinder.universalgcodesender.gcode.util.Plane;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
final public class PointSegment {
    private int toolhead = 0; //DEFAULT TOOLHEAD ASSUMED TO BE 0!
    private double speed;
    private Point3d point;
    
    // Line properties
    private boolean isMetric = true;
    private boolean isZMovement = false;
    private boolean isArc = false;
    private boolean isFastTraverse = false;
    private int lineNumber;
    private ArcProperties arcProperties = null;

    private class ArcProperties {
        public boolean isClockwise;
        public double radius = 0.0;
        public Point3d center = null;
        public Plane plane = null;
    }
    
    public PointSegment() {
        this.lineNumber = -1;
        this.point = new Point3d();
    }
    
    public PointSegment(PointSegment ps) {
        this(ps.point(), ps.getLineNumber());
    
        this.setToolHead(ps.toolhead);
        this.setSpeed(ps.speed);
        this.setIsMetric(ps.isMetric);
        this.setIsZMovement(ps.isZMovement);
        this.setIsFastTraverse(ps.isFastTraverse);

        if (ps.isArc) {
            this.setArcCenter(ps.center());
            this.setRadius(ps.getRadius());
            this.setIsClockwise(ps.isClockwise());
        }
    }
    
    public PointSegment(final Point3d b, final int num)
    {
        this();
        this.point = new Point3d (b);
        this.lineNumber = num;
    }
    
    public PointSegment(final Point3d point, final int num, final Point3d center, final double radius, final boolean clockwise, Plane plane) {
        this(point, num);
        this.isArc = true;
        this.arcProperties = new ArcProperties();
        this.arcProperties.center = new Point3d(center);
        this.arcProperties.radius = radius;
        this.arcProperties.isClockwise = clockwise;
        this.arcProperties.plane = plane;
    }
    
    public void setPoint(final Point3d point) {
        this.point = new Point3d(point);
    }

    public Point3d point()
    {
        return point;
    }
    
    public double[] points()
    {
        double[] points = {point.x, point.y, point.z};
        return points;
    }
    
    public void setToolHead(final int head) {
        this.toolhead = head;
    }
    
    public int getToolhead()
    {
        return toolhead;
    }
    
    public void setLineNumber(final int num) {
        this.lineNumber = num;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setSpeed(final double s) {
        this.speed = s;
    }
    
    public double getSpeed()
    {
        return speed;
    }
    
    public void setIsZMovement(final boolean isZ) {
        this.isZMovement = isZ;
    }
    
    public boolean isZMovement() {
        return isZMovement;
    }
    
    public void setIsMetric(final boolean isMetric) {
        this.isMetric = isMetric;
    }
    
    public boolean isMetric() {
        return isMetric;
    }
    
    public void setIsArc(final boolean isA) {
        this.isArc = isA;
    }
    
    public boolean isArc() {
        return isArc;
    }
    
    public void setIsFastTraverse(final boolean isF) {
        this.isFastTraverse = isF;
    }
    
    public boolean isFastTraverse() {
        return this.isFastTraverse;
    }
    
    // Arc properties.
    
    public void setArcCenter(final Point3d center) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }
        
        this.arcProperties.center = new Point3d(center);
        this.setIsArc(true);
    }
    
    public double[] centerPoints()
    {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            double[] points = {arcProperties.center.x, arcProperties.center.y, arcProperties.center.z};
            return points;
        }
        return null;
    }

    
    public Point3d center() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.center;
        }
        return null;
    }
        
    public void setIsClockwise(final boolean clockwise) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }

        this.arcProperties.isClockwise = clockwise;
    }
    
    public boolean isClockwise() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.isClockwise;
        }
        return false;
    }
    
    public void setRadius(final double rad) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }

        this.arcProperties.radius = rad;
    }
    
    public double getRadius() {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return this.arcProperties.radius;
        }
        return 0;
    }

    public void setPlaneState(Plane plane) {
        this.arcProperties.plane = plane;
    }

    public Plane getPlaneState() {
        return this.arcProperties.plane;
    }
    
    public void convertToMetric() {
        if (this.isMetric) {
            return;
        }

        this.isMetric = true;
        this.point.scale(25.4);

        if (this.isArc && this.arcProperties != null) {
            this.arcProperties.center.scale(25.4);
            this.arcProperties.radius *= 25.4;
        }
    }
}