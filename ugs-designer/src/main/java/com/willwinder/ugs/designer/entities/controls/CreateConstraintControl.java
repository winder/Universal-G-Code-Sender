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

import com.willwinder.ugs.designer.constraint.ConstraintPlacement;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping.Snap;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.gui.ConstraintDistanceDialog;
import com.willwinder.ugs.designer.gui.DimensionPainter;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.DesignModel;
import com.willwinder.ugs.designer.logic.Tool;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The constraint tools, adding distance constraints, alignments, coincidences or equal sizes to
 * the shapes of entities as {@link ConstraintPlacement} describes. Placing a distance constraint
 * asks for the distance to keep.
 */
public class CreateConstraintControl extends AbstractControl {
    private final Controller controller;
    private final Map<Tool, ConstraintPlacement> placements = new EnumMap<>(Tool.class);
    private Point2D cursor;
    private Point2D snapPosition;
    private Optional<Snap> snap = Optional.empty();
    private DistanceConstraint pendingEdit;

    public CreateConstraintControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
        for (Tool tool : Tool.values()) {
            tool.constraintKind().ifPresent(kind -> placements.put(tool, new ConstraintPlacement(controller.getModel().getConstraintManager(), kind)));
        }
        controller.addListener(event -> {
            if (event == ControllerEventType.TOOL_SELECTED) {
                placements.values().forEach(ConstraintPlacement::reset);
                pendingEdit = null;
            }
        });
    }

    /**
     * Whether a press at the point would not be picking a point for a constraint, so it can be
     * used for something else, such as dragging a dimension.
     */
    public boolean isIdleAt(Point2D point) {
        return placement().isIdle() && snapAt(point).isEmpty();
    }

    /**
     * Whether the tool is one this control places constraints with.
     */
    public static boolean isPlacementTool(Tool tool) {
        return tool.constraintKind().isPresent();
    }

    @Override
    public boolean isWithin(Point2D point) {
        return isPlacementTool(controller.getTool());
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.of(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (!(entityEvent instanceof MouseEntityEvent mouseEvent) || !isPlacementTool(controller.getTool())) {
            return;
        }

        cursor = mouseEvent.getCurrentMousePosition();
        switch (mouseEvent.getType()) {
            case MOUSE_PRESSED -> press(cursor);
            case MOUSE_RELEASED -> release();
            case MOUSE_OUT -> cursor = null;
            default -> {
                // Moving the cursor only changes what is shown
            }
        }
        controller.repaintDrawing();
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (!isPlacementTool(controller.getTool())) {
            return;
        }

        ConstraintPlacement placement = placement();
        Optional<Snap> hovered = cursor == null || placement.isPlacing() ? Optional.empty() : snapAt(cursor);
        Optional<Entity> underCursor = cursor == null ? Optional.empty() : entityAt(cursor);
        placement.markers(hovered, underCursor)
                .forEach(marker -> DimensionPainter.paintSnapMarker(graphics, drawing, marker.position(), marker.highlighted()));
        if (cursor != null) {
            placement.preview(hovered, cursor).ifPresent(preview -> {
                DimensionPainter.paintDimension(graphics, drawing, preview.dimension(), true);
                DimensionPainter.paintReadout(graphics, drawing, preview.readout(), cursor);
            });
        }
    }

    private void press(Point2D position) {
        snapPosition = null;
        ConstraintPlacement placement = placement();
        Optional<Snap> pressed = placement.isPlacing() ? Optional.empty() : snapAt(position);
        placement.press(pressed, position).ifPresent(constraint -> {
            controller.addConstraint(constraint);
            controller.selectConstraint(constraint);
            pendingEdit = constraint instanceof DistanceConstraint distance ? distance : null;
        });
    }

    private void release() {
        if (pendingEdit != null) {
            ConstraintDistanceDialog.editDistanceLater(controller, pendingEdit);
            pendingEdit = null;
        }
    }

    private ConstraintPlacement placement() {
        return placements.getOrDefault(controller.getTool(), placements.get(Tool.CONSTRAINT));
    }

    private Optional<Snap> snapAt(Point2D position) {
        if (!Objects.equals(position, snapPosition)) {
            snapPosition = position;
            snap = ShapeSnapping.snapAt(controller.getModel().getPickableEntities(), position,
                    ConstraintPlacement.SNAP_PX / controller.getDrawing().getScale());
        }
        return placement().pickAt(snap, entityAt(position), position);
    }

    private Optional<Entity> entityAt(Point2D position) {
        return controller.getModel().getEntitiesAt(position).stream()
                .filter(DesignModel::isPickable)
                .min(Comparator.comparingDouble(ShapeSnapping::area));
    }

    @Override
    public String toString() {
        return "CreateConstraintControl";
    }
}
