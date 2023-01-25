/*
    Copyright 2022 Will Winder

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
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Point extends AbstractCuttable {

    private static final int RADIUS = 1;
    private final Ellipse2D.Double shape;

    public Point(double relativeX, double relativeY) {
        super(relativeX - RADIUS, relativeY - RADIUS);
        setName("Point");
        this.shape = new Ellipse2D.Double(0, 0, RADIUS * 2.0, RADIUS * 2.0);
    }

    public Point() {
        this(0, 0);
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public Size getSize() {
        return new Size(0, 0);
    }

    @Override
    public void setSize(Size size) {
        // Not applicable
    }

    @Override
    public void setHeight(double height) {
        // Not applicable
    }

    @Override
    public void setWidth(double width) {
        // Not applicable
    }

    @Override
    public void scale(double sx, double sy) {
        // Not applicable
    }

    @Override
    public Entity copy() {
        Point point = new Point();
        copyPropertiesTo(point);
        return point;
    }
}
