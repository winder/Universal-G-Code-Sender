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
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.util.Optional;

/**
 * A control group that can wrap multiple controls
 *
 * @author Joacim Breiler
 */
public class ControlGroup extends EntityGroup implements Control {

    protected final Controller controller;

    public ControlGroup(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        getChildren()
                .forEach(c -> c.render(graphics, drawing));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(c -> c.onEvent(entityEvent));
    }

    @Override
    public SelectionManager getSelectionManager() {
        return controller.getSelectionManager();
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.empty();
    }
}
