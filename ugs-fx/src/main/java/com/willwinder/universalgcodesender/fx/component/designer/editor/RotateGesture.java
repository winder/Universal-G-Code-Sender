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

import com.willwinder.ugs.designer.Utils;
import com.willwinder.ugs.designer.actions.RotateAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drags the rotate handle around the selection's centre. The angle is kept to whole degrees,
 * tenths with Alt, or multiples of 15 with Shift. Applied live and recorded as one
 * {@link RotateAction} on release.
 */
final class RotateGesture implements Gesture {
    private static final double SHIFT_STEP_DEGREES = 15;

    private final ToolContext context;
    private double startRotation;
    private Point2D center;
    private Point2D lastDesign;

    RotateGesture(ToolContext context) {
        this.context = context;
    }

    @Override
    public void begin(PointerEvent event, Point2D design) {
        SelectionManager selection = context.selection();
        startRotation = selection.getRotation();
        center = selection.getCenter();
        lastDesign = design;
    }

    @Override
    public void drag(PointerEvent event, Point2D design) {
        if (center == null) {
            return;
        }
        SelectionManager selection = context.selection();
        double delta = Utils.calcRotationAngleInDegrees(center, lastDesign)
                - Utils.calcRotationAngleInDegrees(center, design);
        double target = selection.getRotation() + delta;
        double rounded = round(target, event);
        double applied = rounded - selection.getRotation();
        if (applied != 0) {
            selection.rotate(center, applied);
        }
        lastDesign = design;
        context.state().setReadout("%.1f°".formatted(Utils.normalizeRotation(selection.getRotation())), design);
        context.render();
    }

    @Override
    public void end(PointerEvent event, Point2D design) {
        if (center == null) {
            return;
        }
        SelectionManager selection = context.selection();
        double total = selection.getRotation() - startRotation;
        if (total != 0) {
            // The rotation is already applied; the action only records it for undo.
            List<Entity> entities = new ArrayList<>(selection.getSelection());
            context.undoManager().addAction(new RotateAction(entities, center, total));
        }
        center = null;
        context.state().setReadout(null, null);
        context.render();
    }

    private static double round(double degrees, PointerEvent event) {
        if (event.isShiftDown()) {
            return Math.round(degrees / SHIFT_STEP_DEGREES) * SHIFT_STEP_DEGREES;
        }
        return Utils.roundToDecimals(degrees, event.isAltDown() ? 1 : 0);
    }
}
