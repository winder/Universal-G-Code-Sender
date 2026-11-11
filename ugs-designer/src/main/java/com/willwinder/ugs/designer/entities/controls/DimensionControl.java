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
package com.willwinder.ugs.designer.entities.controls;

import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.DimensionDrag;
import com.willwinder.ugs.designer.constraint.DimensionLine;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.gui.ConstraintDistanceDialog;
import com.willwinder.ugs.designer.gui.DimensionPainter;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.Optional;

/**
 * Draws the dimensions of the constraints and lets them be selected, dragged closer to or
 * further from the entities they measure and double clicked to edit their distance, with the
 * select tool or with the constraint tools when they are not placing one.
 */
public class DimensionControl extends AbstractControl {
    private static final double PICK_PX = 4;

    private final Controller controller;
    private final CreateConstraintControl createConstraintControl;
    private Constraint hovered;
    private Constraint pressed;
    private DimensionDrag drag;
    private boolean doubleClick;
    private Point2D hitPosition;
    private Optional<Constraint> hit = Optional.empty();

    public DimensionControl(Controller controller, CreateConstraintControl createConstraintControl) {
        super(controller.getSelectionManager());
        this.controller = controller;
        this.createConstraintControl = createConstraintControl;
        constraintManager().addListener(() -> {
            hitPosition = null;
            controller.repaintDrawing();
        });
        controller.addListener(_ -> hitPosition = null);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        Constraint selected = constraintManager().getSelectedConstraint().orElse(null);
        DimensionLine.allOf(constraintManager().getActiveConstraints(), constraint -> constraint == selected || constraint == hovered)
                .forEach(drawn -> DimensionPainter.paintDimension(graphics, drawing, drawn.dimension(), drawn.highlighted()));
    }

    @Override
    public boolean isWithin(Point2D point) {
        return constraintAt(point).isPresent();
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.ofNullable(hovered).map(DimensionControl::cursorFor);
    }

    private static Cursor cursorFor(Constraint constraint) {
        if (!(constraint instanceof DistanceConstraint distance)) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }
        return Cursor.getPredefinedCursor(distance.getFrame().isAcrossVertical() ? Cursor.N_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR);
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (!(entityEvent instanceof MouseEntityEvent mouseEvent)) {
            return;
        }

        Point2D position = mouseEvent.getCurrentMousePosition();
        switch (mouseEvent.getType()) {
            case MOUSE_IN, MOUSE_MOVED -> setHovered(constraintAt(position).orElse(null));
            case MOUSE_OUT -> setHovered(null);
            case MOUSE_PRESSED -> press(position, mouseEvent.getClickCount() == 2);
            case MOUSE_DRAGGED -> {
                if (drag != null) {
                    drag.drag(position);
                }
            }
            case MOUSE_RELEASED -> release();
            default -> {
                // Not used
            }
        }
    }

    private void press(Point2D position, boolean isDoubleClick) {
        hitPosition = null;
        pressed = constraintAt(position).orElse(null);
        drag = pressed instanceof DistanceConstraint distance ? new DimensionDrag(constraintManager(), distance, position) : null;
        if (pressed != null) {
            controller.selectConstraint(pressed);
            doubleClick = isDoubleClick;
        }
    }

    private void release() {
        if (pressed == null) {
            return;
        }
        Optional<UndoableAction> change = drag == null ? Optional.empty() : drag.finish();
        change.ifPresentOrElse(controller.getUndoManager()::addAction, () -> {
            if (doubleClick && pressed instanceof DistanceConstraint distance) {
                ConstraintDistanceDialog.editDistanceLater(controller, distance);
            }
        });
        pressed = null;
        drag = null;
    }

    private void setHovered(Constraint constraint) {
        if (hovered != constraint) {
            hovered = constraint;
            controller.repaintDrawing();
        }
    }

    private Optional<Constraint> constraintAt(Point2D point) {
        if (!Objects.equals(point, hitPosition)) {
            hitPosition = point;
            hit = isActiveAt(point) ? findConstraintAt(point) : Optional.empty();
        }
        return hit;
    }

    private Optional<Constraint> findConstraintAt(Point2D point) {
        double worldPerPixel = 1 / controller.getDrawing().getScale();
        Constraint selected = constraintManager().getSelectedConstraint().orElse(null);
        return DimensionLine.constraintAt(constraintManager().getActiveConstraints(), constraint -> constraint == selected || constraint == hovered,
                point, PICK_PX * worldPerPixel, worldPerPixel);
    }

    private boolean isActiveAt(Point2D point) {
        Tool tool = controller.getTool();
        return tool == Tool.SELECT || (CreateConstraintControl.isPlacementTool(tool) && createConstraintControl.isIdleAt(point));
    }

    private ConstraintManager constraintManager() {
        return controller.getModel().getConstraintManager();
    }

    @Override
    public String toString() {
        return "DimensionControl";
    }
}
