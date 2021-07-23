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

import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

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

    @Override
    public void setSize(Size size) {
        if (size.getWidth() < 2) {
            size = new Size(2, size.getHeight());
        }

        if (size.getHeight() < 2) {
            size = new Size(size.getWidth(), 2);
        }
    }

    public void moveTo(double x, double y) {
        shape.moveTo(x, y);
    }

    public void lineTo(double x, double y) {
        shape.lineTo(x, y);
    }

    public void quadTo(double x1, double y1, double x2, double y2) {
        shape.quadTo(x1, y1, x2, y2);
    }

    public void curveTo(double x1, double y1, double x2, double y2, double x3, double y3) {
        shape.curveTo(x1, y1, x2, y2, x3, y3);
    }

    public void append(Shape s) {
        shape.append(s, true);
    }
}
