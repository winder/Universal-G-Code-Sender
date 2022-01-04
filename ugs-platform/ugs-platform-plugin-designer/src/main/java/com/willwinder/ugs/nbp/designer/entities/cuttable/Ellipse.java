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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * @author Joacim Breiler
 */
public class Ellipse extends AbstractCuttable {

    private final Ellipse2D.Double shape;

    public Ellipse(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        setName("Ellipse");
        this.shape = new Ellipse2D.Double(0, 0, 10, 10);
    }

    public Ellipse() {
        this(0,0);
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public Entity copy() {
        Ellipse ellipse = new Ellipse();
        ellipse.setTransform(new AffineTransform(getTransform()));
        ellipse.setStartDepth(getStartDepth());
        ellipse.setTargetDepth(getTargetDepth());
        ellipse.setCutType(getCutType());
        return ellipse;
    }
}
