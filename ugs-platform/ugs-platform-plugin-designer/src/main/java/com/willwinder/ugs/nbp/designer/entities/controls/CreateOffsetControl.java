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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A control that will create offset shapes based on the current selection.
 */
public class CreateOffsetControl extends SnapToGridControl {

    private final Controller controller;
    private Point2D startPosition;
    private Point2D endPosition;
    private boolean isPressed;

    public CreateOffsetControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        // Always preview when the OFFSET tool is active and there is a selection
/*        if (controller.getTool() != Tool.OFFSET || getSelectionManager().isEmpty()) {
            return;
        }

 */
        double offset = getCurrentOffset();
        if (offset <= 0) {
            return;
        }
        graphics.setColor(ThemeColors.LIGHT_BLUE_GREY);
        // Preview by drawing inflated bounds of each selected entity
        getSelectionManager().getSelection().forEach(entity -> {
            Rectangle2D b = entity.getBounds();
            double x = b.getX() - offset;
            double y = b.getY() - offset;
            double w = b.getWidth() + 2 * offset;
            double h = b.getHeight() + 2 * offset;
            if (entity instanceof Ellipse) {
                graphics.draw(new Ellipse2D.Double(x, y, w, h));
            } else {
                graphics.draw(new Rectangle2D.Double(x, y, w, h));
            }
        });
    }

    @Override
    public boolean isWithin(Point2D point) {
        // This control will be active if the offset tool is activated
        //return controller.getTool() == Tool.OFFSET;
        return isPressed;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent) {
            MouseEntityEvent mouseEntityEvent = (MouseEntityEvent) entityEvent;
            startPosition = mouseEntityEvent.getStartMousePosition();
            endPosition = mouseEntityEvent.getCurrentMousePosition();

            if (mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_DRAGGED) {
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_RELEASED) {
                isPressed = false;
                createEntities();
            }
        }
    }

    private double getCurrentOffset() {
        // Drive the offset distance from the Cut Settings: reuse Target Depth as offset value
        try {
            if (startPosition == null || endPosition == null) return 0d;
            double dx = endPosition.getX() - startPosition.getX();
            double dy = endPosition.getY() - startPosition.getY();
            double d = Math.max(Math.abs(dx), Math.abs(dy));
            return snapToGrid(d);
        } catch (Exception e) {
            return 0d;
        }
    }

    private void createEntities() {
        float offset = Double.valueOf(getCurrentOffset()).floatValue();
        if (offset <= 0 || getSelectionManager().isEmpty()) {
            controller.setTool(Tool.SELECT);
            return;
        }
        List<Entity> created = new ArrayList<>();
        getSelectionManager().getSelection().forEach(entity -> {
            Rectangle2D b = entity.getBounds();
            double x = b.getX() - offset;
            double y = b.getY() - offset;
            double w = snapToGrid(b.getWidth() + 2 * offset);
            double h = snapToGrid(b.getHeight() + 2 * offset);

            Path offsetPath = createOuterOffsetPath(entity.getRelativeShape(), offset);
            if (offsetPath != null) {
                controller.addEntity(offsetPath);
                offsetPath.setPosition(new Point2D.Double(x, y));
                offsetPath.setSize(new Size(w, h));
                created.add(offsetPath);
            }
        });

        controller.setTool(Tool.SELECT);
        controller.getSelectionManager().setSelection(created);
    }

    /**
     * Create a new Path that is an offset of the current path.
     * This method uses BasicStroke to create the offset shape.
     *
     * @param offset The distance to offset the path.
     * @return A new Path representing the offset, or null if the operation fails.
     */
    public Path createOffsetPath(Shape shape, float offset) {
        java.awt.Shape strokedShape = new java.awt.BasicStroke(
                offset * 2, // width of the stroke
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ).createStrokedShape(shape);

        Path offsetPath = new Path();
        offsetPath.append(strokedShape);
        return offsetPath;
    }

    /**
     * Create a new Path that is an outer offset of the current path.
     * This method uses BasicStroke to create the offset shape.
     *
     * @param offset The distance to offset the path outward.
     * @return A new Path representing the outer offset, or null if the operation fails.
     */
    public Path createOuterOffsetPath(Shape shape, float offset) {
        Shape strokedShape = new BasicStroke(
                offset * 2,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ).createStrokedShape(shape);

        Area outlineArea = new Area(strokedShape);
        Area originalArea = new Area(shape);

        outlineArea.add(originalArea);

        Path outerOffsetPath = new Path();
        outerOffsetPath.append(outlineArea);
        copyPropertiesTo(outerOffsetPath);
        return outerOffsetPath;
    }


    @Override
    public String toString() {
        return "CreateOffsetControl";
    }
}
