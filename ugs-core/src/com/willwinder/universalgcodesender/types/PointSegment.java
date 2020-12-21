/*
 * An optimized LineSegment which only uses the end point with the expectation
 * that a collection of points will represent a continuous set of line segments.
 *
 * Created on Nov 9, 2013
 */

/*
    Copyright 2013-2017 Will Winder

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
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.INCH;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;

/**
 *
 * @author wwinder
 */
final public class PointSegment {
    private double speed;
    private Position point;
    
    // Line properties
    private boolean isMetric = true;
    private boolean isZMovement = false;
    private boolean isRotation = false;
    private boolean isArc = false;
    private boolean isFastTraverse = false;
    private boolean isProbe = false;
    private int lineNumber;
    private ArcProperties arcProperties = null;

    private class ArcProperties {
        public boolean isClockwise;
        public double radius = 0.0;
        public Position center = null;
        public Plane plane = null;
    }

    
    public PointSegment(PointSegment ps) {
        this(ps.point(), ps.getLineNumber());
    
        this.setSpeed(ps.speed);
        this.setIsArc(ps.isArc);
        this.setIsMetric(ps.isMetric);
        this.setIsZMovement(ps.isZMovement);
        this.setIsRotation(ps.isRotation);
        this.setIsFastTraverse(ps.isFastTraverse);
        this.setIsProbe(ps.isProbe);

        if (ps.isArc) {
            this.setArcCenter(ps.center());
            this.setRadius(ps.getRadius());
            this.setIsClockwise(ps.isClockwise());
        }
    }
    
    public PointSegment(final Position b, final int num) {
        this.point = new Position (b);
        this.lineNumber = num;
    }
    
    public PointSegment(final Position point, final int num, final Position center, final double radius, final boolean clockwise, Plane plane) {
        this(point, num);
        this.isArc = true;
        this.arcProperties = new ArcProperties();
        this.arcProperties.center = new Position(center);
        this.arcProperties.radius = radius;
        this.arcProperties.isClockwise = clockwise;
        this.arcProperties.plane = plane;
    }
    
    public void setPoint(final Position point) {
        this.point = new Position(point);
    }

    public Position point()
    {
        return point;
    }
    
    public double[] points() {
        return new double[]{point.x, point.y, point.z};
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

    public void setIsRotation(final boolean hasRotation) {
        this.isRotation = hasRotation;
    }
    
    public boolean isRotation() {
        return this.isRotation;
    }
    
    public void setIsMetric(final boolean isMetric) {
        this.isMetric = isMetric;
    }
    
    public boolean isMetric() {
        return isMetric;
    }

    public void setIsArc(final boolean isArc) {
        this.isArc = isArc;
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
    
    public void setIsProbe(final boolean isProbe) {
        this.isProbe = isProbe;
    }
    
    public boolean isProbe() {
        return this.isProbe;
    }

    // Arc properties.
    
    public void setArcCenter(final Position center) {
        if (this.arcProperties == null) {
            this.arcProperties = new ArcProperties();
        }
        
        this.arcProperties.center = new Position(center);
        this.setIsArc(true);
    }
    
    public double[] centerPoints()
    {
        if (this.arcProperties != null && this.arcProperties.center != null) {
            return new double[]{arcProperties.center.x, arcProperties.center.y, arcProperties.center.z};
        }
        return null;
    }

    
    public Position center() {
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

        this.point = this.point.getPositionIn(MM);

        if (this.isArc && this.arcProperties != null) {
            this.arcProperties.center = this.arcProperties.center.getPositionIn(MM);
            this.arcProperties.radius *= UnitUtils.scaleUnits(INCH, MM);
        }
    }
}
