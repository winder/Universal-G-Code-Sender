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
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * A control that will create a new line
 *
 * @author Joacim Breiler
 */
public class CreateLineControl extends SnapToGridControl {

    private final Controller controller;
    private Point2D startPosition;
    private Point2D endPosition;
    private boolean isPressed;

    public CreateLineControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (isPressed) {
            double startX = snapToGrid(startPosition.getX());
            double startY = snapToGrid(startPosition.getY());
            double endX = snapToGrid(endPosition.getX());
            double endY = snapToGrid(endPosition.getY());
            Line2D.Double line = new Line2D.Double(startX, startY, endX, endY);
            graphics.setColor(ThemeColors.LIGHT_BLUE_GREY);
            graphics.draw(line);
        }
    }

    @Override
    public boolean isWithin(Point2D point) {
        return controller.getTool() == Tool.LINE;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent mouseEntityEvent) {
            startPosition = mouseEntityEvent.getStartMousePosition();
            endPosition = mouseEntityEvent.getCurrentMousePosition();

            if (mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_DRAGGED) {
                isPressed = true;
            } else if (mouseEntityEvent.getType() == EventType.MOUSE_RELEASED) {
                isPressed = false;
                createEntity();
            }
        }
    }

    private void createEntity() {
        double startX = snapToGrid(startPosition.getX());
        double startY = snapToGrid(startPosition.getY());
        double endX = snapToGrid(endPosition.getX());
        double endY = snapToGrid(endPosition.getY());

        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        controller.addEntity(path);

        controller.setTool(Tool.VERTEX);
        controller.getSelectionManager().addSelection(path);
    }

    @Override
    public String toString() {
        return "CreateLineControl";
    }
}