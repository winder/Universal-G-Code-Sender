/*
    Copyright 2022 Will Winder

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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * A control that will create a new point
 *
 * @author Joacim Breiler
 */
public class CreatePointControl extends AbstractControl {

    private final Controller controller;

    public CreatePointControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        // This controller has nothing to render
    }

    @Override
    public boolean isWithin(Point2D point) {
        // This control will be active only if the point tool is activated
        return controller.getTool() == Tool.POINT;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent) {
            MouseEntityEvent mouseEntityEvent = (MouseEntityEvent) entityEvent;
            if (mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                controller.addEntity(new Point(mouseEntityEvent.getStartMousePosition().getX(), mouseEntityEvent.getStartMousePosition().getY()));
            }
        }
    }

    @Override
    public String toString() {
        return "CreatePointControl";
    }
}
