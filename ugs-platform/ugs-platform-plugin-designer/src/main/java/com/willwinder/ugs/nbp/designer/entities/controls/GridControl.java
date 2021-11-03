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
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Joacim Breiler
 */
public class GridControl extends AbstractEntity implements Control {

    public static final int MINIMUM_SIZE = 300;
    public static final int LARGE_GRID_SIZE = 50;
    public static final int SMALL_GRID_SIZE = 10;
    private final Controller controller;
    public GridControl(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        double gridSize = LARGE_GRID_SIZE;

        Rectangle2D bounds = controller.getDrawing().getRootEntity().getBounds();
        int calculatedMinimumWidth = (int) Math.round(Math.floor(bounds.getMaxX() / gridSize) * gridSize + (gridSize * 2));
        int calculatedMinimumHeight = (int) Math.round(Math.floor(bounds.getMaxY() / gridSize) * gridSize + (gridSize * 2));

        int width = Math.max(calculatedMinimumWidth, MINIMUM_SIZE);
        int height = Math.max(calculatedMinimumHeight, MINIMUM_SIZE);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        graphics.setStroke(new BasicStroke(Double.valueOf(0.1 / drawing.getScale()).floatValue()));
        graphics.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x <= width; x += SMALL_GRID_SIZE) {
            graphics.drawLine(x, 0, x, height);
        }

        for (int y = 0; y <= height; y += SMALL_GRID_SIZE) {
            graphics.drawLine(0, y, width, y);
        }


        graphics.setStroke(new BasicStroke(Double.valueOf(0.2 / drawing.getScale()).floatValue()));
        for (int x = 0; x <= width; x += LARGE_GRID_SIZE) {
            graphics.drawLine(x, 0, x, height);
        }

        for (int y = 0; y <= height; y += LARGE_GRID_SIZE) {
            graphics.drawLine(0, y, width, y);
        }
    }

    @Override
    public void setSize(Size size) {

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
