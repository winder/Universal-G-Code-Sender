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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;

import java.awt.geom.Point2D;

/**
 * A control group for all resize handles
 *
 * @author Joacim Breiler
 */
public class ResizeControlGroup extends ControlGroup {
    public ResizeControlGroup(Controller controller) {
        super(controller);

        addChild(new ResizeControl(controller, Anchor.TOP_CENTER));
        addChild(new ResizeControl(controller, Anchor.LEFT_CENTER));
        addChild(new ResizeControl(controller, Anchor.RIGHT_CENTER));
        addChild(new ResizeControl(controller, Anchor.BOTTOM_CENTER));
        addChild(new ResizeControl(controller, Anchor.BOTTOM_LEFT));
        addChild(new ResizeControl(controller, Anchor.BOTTOM_RIGHT));
        addChild(new ResizeControl(controller, Anchor.TOP_LEFT));
        addChild(new ResizeControl(controller, Anchor.TOP_RIGHT));
    }

    @Override
    public boolean isWithin(Point2D point) {
        return !controller.getSelectionManager().isEmpty() &&
                controller.getTool() == Tool.SELECT &&
                super.isWithin(point);
    }
}
