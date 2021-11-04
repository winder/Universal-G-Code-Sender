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
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A control that will select entities in the drawing
 *
 * @author Joacim Breiler
 */
public class SelectionControl extends AbstractControl {

    private final Controller controller;
    private Point2D.Double startPosition;
    private Point2D.Double currentPosition;
    private boolean isPressed;

    public SelectionControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (isPressed) {
            double startX = Math.min(startPosition.getX(), currentPosition.getX());
            double endX = Math.max(startPosition.getX(), currentPosition.getX());
            double startY = Math.min(startPosition.getY(), currentPosition.getY());
            double endY = Math.max(startPosition.getY(), currentPosition.getY());

            Rectangle2D.Double rect = new Rectangle2D.Double(startX, startY, endX - startX, endY - startY);
            graphics.setColor(ThemeColors.LIGHT_BLUE_GREY);
            graphics.draw(rect);
        }
    }

    @Override
    public boolean isWithin(Point2D point) {
        // This control will be active if the select tool is activated
        return controller.getTool() == Tool.SELECT;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent) {
            MouseEntityEvent mouseEntityEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseEntityEvent.getCurrentMousePosition();

            if (mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                startPosition = new Point2D.Double(mousePosition.getX(), mousePosition.getY());
                currentPosition = startPosition;
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_DRAGGED) {
                currentPosition = new Point2D.Double(mousePosition.getX(), mousePosition.getY());
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_RELEASED) {
                isPressed = false;
                double startX = Math.min(startPosition.getX(), currentPosition.getX());
                double startY = Math.min(startPosition.getY(), currentPosition.getY());
                double width = Math.max(startPosition.getX(), currentPosition.getX()) - startX;
                double height = Math.max(startPosition.getY(), currentPosition.getY()) - startY;
                Rectangle2D rect = new Rectangle2D.Double(startX, startY, width, height);

                if (rect.getWidth() > 1 && rect.getHeight() > 1) {
                    selectIntersection(rect, mouseEntityEvent.isShiftPressed());
                } else {
                    selectOne(mousePosition, mouseEntityEvent.isShiftPressed());
                }
            }
        }
    }

    private void selectIntersection(Shape shape, boolean selectMultiple) {
        Set<Entity> entitiesIntersecting = controller.getDrawing()
                .getEntitiesIntersecting(shape)
                .stream()
                .filter(e -> e != this)
                .filter(e -> !(e instanceof Control))
                .collect(Collectors.toSet());

        if (selectMultiple) {
            if (!entitiesIntersecting.isEmpty()) {
                entitiesIntersecting.forEach(e -> controller.getSelectionManager().toggleSelection(e));
            }
        } else {
            controller.getSelectionManager().setSelection(new ArrayList<>(entitiesIntersecting));
        }
    }

    private void selectOne(Point2D mousePosition, boolean selectMultiple) {
        Set<Entity> entitiesAt = controller.getDrawing()
                .getEntitiesAt(mousePosition)
                .stream()
                .filter(e -> e != this)
                .filter(e -> !(e instanceof Control))
                .collect(Collectors.toSet());

        if (selectMultiple) {
            entitiesAt.forEach(e -> controller.getSelectionManager().toggleSelection(e));
        } else {
            List<Entity> selection = entitiesAt.stream()
                    .sorted(Comparator.comparingDouble(e -> e.getBounds().getWidth() * e.getBounds().getHeight()))
                    .limit(1)
                    .filter(e -> !controller.getSelectionManager().isSelected(e))
                    .collect(Collectors.toList());
            controller.getSelectionManager().setSelection(selection);
        }
    }

    @Override
    public String toString() {
        return "SelectionControl";
    }
}
