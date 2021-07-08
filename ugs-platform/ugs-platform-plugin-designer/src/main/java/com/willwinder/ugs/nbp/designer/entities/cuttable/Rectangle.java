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

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Joacim Breiler
 */
public class Rectangle extends AbstractCuttable {

    private final Rectangle2D.Double shape;

    public Rectangle() {
        this(0,0);
    }

    /**
     * Creates a rectangle with the relative position to the parent
     *
     * @param relativeX the relative position to the parent
     * @param relativeY the relative position to the parent
     */
    public Rectangle(double relativeX, double relativeY) {
        super(relativeX, relativeY);
        this.shape = new Rectangle2D.Double(0, 0, 10, 10);
        setName("Rectangle");
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public void setSize(Dimension s) {
        if (s.getWidth() < 2) {
            s.setSize(2, s.getHeight());
        }

        if (s.getHeight() < 2) {
            s.setSize(s.getWidth(), 2);
        }
        shape.setFrame(0, 0, s.getWidth(), s.getHeight());
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }
    
    public void setWidth(double width) {
        shape.setFrame(0, 0, width, shape.getHeight());
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }

    public void setHeight(double height) {
        shape.setFrame(0, 0, shape.getWidth(), height);
        notifyEvent(new EntityEvent(this, EventType.RESIZED));
    }
}
