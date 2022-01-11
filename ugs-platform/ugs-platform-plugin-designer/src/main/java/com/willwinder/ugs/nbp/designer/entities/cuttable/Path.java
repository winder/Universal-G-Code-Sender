/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityException;

import java.awt.*;
import java.awt.geom.*;

/**
 * @author Joacim Breiler
 */
public class Path extends AbstractCuttable {

    private final Path2D.Double shape;

    public Path() {
        super();
        setName("Path");
        this.shape = new Path2D.Double();
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    public void moveTo(double x, double y) {
        try {
            Point2D relativePoint = getTransform().inverseTransform(new Point2D.Double(x, y), null);
            shape.moveTo(relativePoint.getX(), relativePoint.getY());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException(e);
        }
    }

    public void lineTo(double x, double y) {
        try {
            Point2D relativePoint = getTransform().inverseTransform(new Point2D.Double(x, y), null);
            shape.lineTo(relativePoint.getX(), relativePoint.getY());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException(e);
        }
    }

    public void quadTo(double x1, double y1, double x2, double y2) {
        try {
            Point2D relativePoint1 = getTransform().inverseTransform(new Point2D.Double(x1, y1), null);
            Point2D relativePoint2 = getTransform().inverseTransform(new Point2D.Double(x2, y2), null);
            shape.quadTo(relativePoint1.getX(), relativePoint1.getY(), relativePoint2.getX(), relativePoint2.getY());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException(e);
        }
    }

    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        try {
            Point2D relativePoint1 = getTransform().inverseTransform(new Point2D.Double(x1, y1), null);
            Point2D relativePoint2 = getTransform().inverseTransform(new Point2D.Double(x2, y2), null);
            Point2D relativePoint3 = getTransform().inverseTransform(new Point2D.Double(x3, y3), null);
            shape.curveTo(relativePoint1.getX(), relativePoint1.getY(), relativePoint2.getX(), relativePoint2.getY(), relativePoint3.getX(), relativePoint3.getY());
        } catch (NoninvertibleTransformException e) {
            throw new EntityException(e);
        }
    }

    public void append(Shape s) {
        shape.append(s, true);
    }

    /**
     * Returns true if the path contains multiple paths (such as holes)
     *
     * @return true if path is compound path
     */
    public boolean isCompoundPath() {
        PathIterator pathIterator = shape.getPathIterator(getTransform());
        int numberOfCompunds = 0;
        double[] params = new double[6];
        while (!pathIterator.isDone() && numberOfCompunds <= 1) {
            pathIterator.next();
            int type = pathIterator.currentSegment(params);
            if (type == PathIterator.SEG_CLOSE) {
                numberOfCompunds++;
            }
        }
        return numberOfCompunds > 1;
    }

    @Override
    public Entity copy() {
        Path path = new Path();
        path.append(getRelativeShape());
        path.setTransform(new AffineTransform(getTransform()));
        path.setStartDepth(getStartDepth());
        path.setTargetDepth(getTargetDepth());
        path.setCutType(getCutType());
        return path;
    }

    public void close() {
        shape.closePath();
    }
}
