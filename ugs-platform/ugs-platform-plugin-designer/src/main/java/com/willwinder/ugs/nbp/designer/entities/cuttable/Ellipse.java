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
import com.willwinder.ugs.nbp.designer.model.Size;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class Ellipse extends AbstractCuttable {

    private final Ellipse2D.Double shape;

    public Ellipse(double x, double y) {
        super(x, y);
        setName("Ellipse");
        this.shape = new Ellipse2D.Double(0, 0, 10, 10);
    }

    public Ellipse() {
        this(0,0);
    }

    public Ellipse(double x, double y, double w, double h) {
        this(x, y);
        setSize(new Size(w, h));
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public Entity copy() {
        Ellipse ellipse = new Ellipse();
        copyPropertiesTo(ellipse);
        return ellipse;
    }

    public boolean isCircle() {
        return isEqual(getSize().getHeight(), getSize().getWidth(), 0.01);
    }

    @Override
    public List<CutType> getAvailableCutTypes() {
        return List.of(CutType.POCKET, CutType.SURFACE, CutType.ON_PATH, CutType.INSIDE_PATH, CutType.OUTSIDE_PATH, CutType.LASER_ON_PATH, CutType.LASER_FILL, CutType.CENTER_DRILL);
    }
}
