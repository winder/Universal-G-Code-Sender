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
package com.willwinder.ugs.designer.entities.entities.controls;

import com.willwinder.ugs.designer.entities.entities.EntityEvent;
import com.willwinder.ugs.designer.gui.Colors;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class HighlightModelControl extends AbstractControl {
    private final Controller controller;

    public HighlightModelControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (getSelectionManager().getSelection().isEmpty() || controller.getTool() != Tool.SELECT) {
            return;
        }

        // Draw the bounds
        graphics.setColor(Colors.CONTROL_BORDER);
        graphics.setStroke(new BasicStroke((float) (0.8f / drawing.getScale())));
        Rectangle2D bounds = getRelativeShape().getBounds2D();
        bounds.setFrame(bounds.getX() , bounds.getY() , bounds.getWidth(), bounds.getHeight());
        graphics.draw(getSelectionManager().getTransform().createTransformedShape(bounds));
    }

    @Override
    public boolean isWithin(Point2D point) {
        return false;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        // Not applicable
    }
}
