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

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;

import java.awt.*;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {

    private CutType cutType = CutType.NONE;
    private double cutDepth;

    protected AbstractCuttable() {
        this(0, 0);
    }

    protected AbstractCuttable(double relativeX, double relativeY) {
        super(relativeX, relativeY);
    }

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public double getCutDepth() {
        return cutDepth;
    }

    @Override
    public void setCutDepth(double cutDepth) {
        this.cutDepth = cutDepth;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1));

        if (getCutType() == CutType.POCKET) {
            graphics.setColor(Color.BLACK);
            graphics.fill(getShape());
            graphics.draw(getShape());
        } else if (getCutType() == CutType.INSIDE_PATH ||getCutType() == CutType.ON_PATH ||getCutType() == CutType.OUTSIDE_PATH) {
            graphics.setColor(Color.BLACK);
            graphics.draw(getShape());
        }
        else {
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.draw(getShape());
        }
    }
}
