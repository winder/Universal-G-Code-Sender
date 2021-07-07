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
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 * @author Joacim Breiler
 */
public class GridControl extends AbstractEntity implements Control {

    private final Controller controller;

    public GridControl(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(0.1f));
        graphics.setColor(Color.GRAY);

        double gridSize = 10;
        double width = (int)controller.getSettings().getStockSize().getWidth();
        double height = (int)controller.getSettings().getStockSize().getHeight();

        for (double x = 0; x <= width; x += gridSize) {
            graphics.drawLine((int) Math.round(x), 0, (int) Math.round(x), (int) height);
        }

        for (double y = 0; y <= height; y += gridSize) {
            graphics.drawLine(0, (int) Math.round(y), (int) width, (int) Math.round(y));
        }
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public Shape getShape() {
        return new Rectangle();
    }

    @Override
    public Shape getRelativeShape() {
        return new Rectangle();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return null;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
