/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.function.Function;

/**
 * Drags out a rectangle or an ellipse. The corners snap to the grid, Shift keeps the shape
 * square, and the new entity is selected with the select tool active afterwards.
 */
public final class CreateShapeTool implements EditorTool {
    private static final double MIN_SIZE = 0.1;

    private final ToolContext context;
    private final Function<Rectangle2D, Entity> factory;
    private final Function<Rectangle2D, Shape> previewFactory;
    private Point2D start;

    private CreateShapeTool(ToolContext context, Function<Rectangle2D, Entity> factory,
                            Function<Rectangle2D, Shape> previewFactory) {
        this.context = context;
        this.factory = factory;
        this.previewFactory = previewFactory;
    }

    public static CreateShapeTool rectangle(ToolContext context) {
        return new CreateShapeTool(context, bounds -> {
            Rectangle rectangle = new Rectangle(bounds.getX(), bounds.getY());
            rectangle.setSize(new Size(bounds.getWidth(), bounds.getHeight()));
            return rectangle;
        }, bounds -> bounds);
    }

    public static CreateShapeTool ellipse(ToolContext context) {
        return new CreateShapeTool(context, bounds -> {
            Ellipse ellipse = new Ellipse(bounds.getX(), bounds.getY());
            ellipse.setSize(new Size(bounds.getWidth(), bounds.getHeight()));
            return ellipse;
        }, bounds -> new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        start = context.snapper().snap(design);
        context.state().setPreview(null);
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        context.state().setPreview(previewFactory.apply(bounds(event, design)));
        context.render();
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        Rectangle2D bounds = bounds(event, design);
        start = null;
        context.state().setPreview(null);
        if (bounds.getWidth() >= MIN_SIZE && bounds.getHeight() >= MIN_SIZE) {
            Entity entity = factory.apply(bounds);
            context.controller().addEntity(entity);
            context.selectTool(Tool.SELECT);
            context.selection().setSelection(java.util.List.of(entity));
        }
        context.render();
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        return Optional.of(Cursor.CROSSHAIR);
    }

    @Override
    public void deactivate() {
        start = null;
        context.state().setPreview(null);
    }

    private Rectangle2D bounds(PointerEvent event, Point2D design) {
        Point2D end = context.snapper().snap(design);
        double width = end.getX() - start.getX();
        double height = end.getY() - start.getY();
        if (event.isShiftDown()) {
            double side = Math.max(Math.abs(width), Math.abs(height));
            width = Math.copySign(side, width == 0 ? 1 : width);
            height = Math.copySign(side, height == 0 ? 1 : height);
        }
        return new Rectangle2D.Double(
                Math.min(start.getX(), start.getX() + width),
                Math.min(start.getY(), start.getY() + height),
                Math.abs(width), Math.abs(height));
    }
}
