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
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityException;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class GridControl extends AbstractEntity implements Control {

    public static final int LARGE_GRID_SIZE = 100;
    public static final int SMALL_GRID_SIZE = 20;
    private final Controller controller;

    public GridControl(Controller controller) {
        this.controller = controller;
    }

    private static void drawZeroLines(Graphics2D graphics, Drawing drawing, int startPosX, int startPosY, int endPosX, int endPosY) {
        // Draw zero lines
        graphics.setStroke(new BasicStroke((float) (0.5 / drawing.getScale())));
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawLine(0, startPosY, 0, endPosY);
        graphics.drawLine(startPosX, 0, endPosX, 0);
    }

    private static void drawSmallGrid(Graphics2D graphics, Drawing drawing, int startPosX, int startPosY, int endPosX, int endPosY, int gridSize) {
        graphics.setStroke(new BasicStroke((float) (0.2 / drawing.getScale())));
        graphics.setColor(Color.LIGHT_GRAY);
        for (int x = startPosX; x <= endPosX; x += gridSize) {
            graphics.drawLine(x, startPosY, x, endPosY);
        }

        for (int y = startPosY; y <= endPosY; y += gridSize) {
            graphics.drawLine(startPosX, y, endPosX, y);
        }
    }

    private static void drawLargeGridAndText(Graphics2D graphics, Drawing drawing, int startPosX, int startPosY, int endPosX, int endPosY, int gridSize) {
        Rectangle2D clipBounds = graphics.getClipBounds();
        AffineTransform affineTransform = AffineTransform.getScaleInstance(1 / drawing.getScale(), -1 / drawing.getScale());
        affineTransform.rotate(Math.PI / 2);
        Font font = new Font(null, Font.PLAIN, 10).deriveFont(affineTransform);
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();

        graphics.setStroke(new BasicStroke((float) (0.3 / drawing.getScale())));
        graphics.setColor(Color.LIGHT_GRAY);

        int fontOffset = (int) Math.round(6 / drawing.getScale());

        for (int x = startPosX; x <= endPosX; x += gridSize) {
            if (x < clipBounds.getMinX() || x > clipBounds.getMaxX()) {
                continue;
            }
            graphics.drawLine(x, startPosY, x, endPosY);

            String text = x + " mm";
            int y = -fontMetrics.stringWidth(text) - fontOffset;
            graphics.drawString(text, x + fontOffset, y);
        }

        affineTransform = AffineTransform.getScaleInstance(1 / drawing.getScale(), -1 / drawing.getScale());
        font = new Font(null, Font.PLAIN, 10).deriveFont(affineTransform);
        graphics.setFont(font);
        fontMetrics = graphics.getFontMetrics();

        for (int y = startPosY; y <= endPosY; y += gridSize) {
            if (y < clipBounds.getMinY() || y > clipBounds.getMaxY()) {
                continue;
            }
            graphics.drawLine(startPosX, y, endPosX, y);

            String text = y + " mm";
            int x = -fontMetrics.stringWidth(text);
            graphics.drawString(text, x - fontOffset, y + fontOffset);
        }
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        double gridSize = LARGE_GRID_SIZE;


        Rectangle2D bounds = controller.getDrawing().getRootEntity().getBounds();
        double gridMargin = (gridSize * 100);


        int startPosX = (int) Math.min(-gridMargin, (Math.floor(bounds.getMinX() / gridSize) * gridSize + gridMargin));
        int startPosY = (int) Math.min(-gridMargin, (Math.floor(bounds.getMinY() / gridSize) * gridSize + gridMargin));
        int endPosX = (int) Math.max(gridMargin, (Math.floor(bounds.getMaxX() / gridSize) * gridSize + gridMargin));
        int endPosY = (int) Math.max(gridMargin, (Math.floor(bounds.getMaxY() / gridSize) * gridSize + gridMargin));

        int width = endPosX - startPosX;
        int height = endPosY - startPosY;
        graphics.setColor(Color.WHITE);
        graphics.fillRect(startPosX, startPosY, width, height);

        int smallGridSize;
        int largeGridSize;
        if (drawing.getScale() < 1.3) {
            smallGridSize = SMALL_GRID_SIZE;
            largeGridSize = LARGE_GRID_SIZE;
        } else if (drawing.getScale() < 4) {
            smallGridSize = SMALL_GRID_SIZE / 2;
            largeGridSize = LARGE_GRID_SIZE / 2;
        } else if (drawing.getScale() < 12) {
            smallGridSize = SMALL_GRID_SIZE / 4;
            largeGridSize = LARGE_GRID_SIZE / 4;
        } else {
            smallGridSize = SMALL_GRID_SIZE / 10;
            largeGridSize = LARGE_GRID_SIZE / 10;
        }
        drawSmallGrid(graphics, drawing, startPosX, startPosY, endPosX, endPosY, smallGridSize);
        drawLargeGridAndText(graphics, drawing, startPosX, startPosY, endPosX, endPosY, largeGridSize);
        drawZeroLines(graphics, drawing, startPosX, startPosY, endPosX, endPosY);
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.empty();
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
        // Not applicable
    }

    @Override
    public Entity copy() {
        throw new EntityException("Not implemented");
    }
}
