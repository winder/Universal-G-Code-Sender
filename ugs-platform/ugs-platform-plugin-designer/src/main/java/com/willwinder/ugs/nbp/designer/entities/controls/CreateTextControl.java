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

import com.willwinder.ugs.nbp.designer.actions.AddAction;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A control that will create a new ellipse
 *
 * @author Joacim Breiler
 */
public class CreateTextControl extends AbstractControl {

    private final Controller controller;
    private Point2D startPosition;
    private Point2D endPosition;
    private boolean isPressed;

    public CreateTextControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (isPressed) {
            double startX = Math.min(startPosition.getX(), endPosition.getX());
            double endX = Math.max(startPosition.getX(), endPosition.getX());
            double startY = Math.min(startPosition.getY(), endPosition.getY());
            double endY = Math.max(startPosition.getY(), endPosition.getY());
            Rectangle2D.Double rect = new Rectangle2D.Double(startX, startY, endX - startX, endY - startY);
            graphics.setColor(ThemeColors.LIGHT_BLUE_GREY);
            graphics.draw(rect);
        }
    }

    @Override
    public boolean isWithin(Point2D point) {
        return controller.getTool() == Tool.TEXT;
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
                createEntity();
            }
        }
    }

    private void createEntity() {
        double startX = Math.min(startPosition.getX(), endPosition.getX());
        double startY = Math.min(startPosition.getY(), endPosition.getY());

        Text text = new Text(startX, startY);
        AddAction addAction = new AddAction(controller, text);
        addAction.actionPerformed(new ActionEvent(this, 0, ""));
        controller.addEntity(text);
        controller.setTool(Tool.SELECT);
        controller.getSelectionManager().addSelection(text);
    }

    @Override
    public String toString() {
        return "CreateTextControl";
    }
}
