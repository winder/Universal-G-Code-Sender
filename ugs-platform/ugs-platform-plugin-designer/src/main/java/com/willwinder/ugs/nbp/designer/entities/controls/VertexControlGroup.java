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

import com.willwinder.ugs.nbp.designer.logic.Controller;

/**
 * A control group that wraps the vertex control selector and all
 * vertex control points.
 *
 * @author Joacim Breiler
 */
public class VertexControlGroup extends ControlGroup implements Control {

    public VertexControlGroup(Controller controller) {
        super(controller);
        addChild(new VertexControlSelector(controller, this));
    }

    public void addVertexControl(VertexControl vertexControl) {
        addChild(vertexControl);
    }

    public void removeVertexControls() {
        getChildren().stream()
                .filter(child -> child instanceof VertexControl)
                .forEach(this::removeChild);
    }
}
