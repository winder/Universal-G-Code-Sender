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

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintPlacement;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping.Snap;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * The constraint tool, adding distance constraints between vertices and centers of entities as
 * {@link ConstraintPlacement} describes. Placing a constraint asks for the distance to keep. The
 * alignment, coincidence and equal size tools are the same tool placing those instead.
 * <p>
 * Existing dimensions can be selected, dragged and double clicked to edit their distance, as with
 * the select tool.
 */
public final class ConstraintTool implements EditorTool {
    private final ToolContext context;
    private final ConstraintPlacement placement;
    private DistanceConstraint pendingEdit;
    private Gesture gesture;

    public ConstraintTool(ToolContext context, Constraint.Kind kind) {
        this.context = context;
        this.placement = new ConstraintPlacement(context.constraintManager(), kind);
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Optional<Snap> snap = placement.isPlacing() ? Optional.empty() : snapAt(design);
        Optional<Constraint> dimension = placement.isIdle() && snap.isEmpty() ? context.constraintAt(design) : Optional.empty();
        if (dimension.isPresent()) {
            gesture = new DimensionDragGesture(context, dimension.get());
            gesture.begin(event, design);
            return;
        }

        placement.press(snap, design).ifPresent(constraint -> {
            context.controller().addConstraint(constraint);
            ConstraintEditing.select(context, constraint);
            pendingEdit = constraint instanceof DistanceConstraint distance ? distance : null;
        });
        update(design);
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (gesture != null) {
            gesture.drag(event, design);
        } else {
            update(design);
        }
    }

    /**
     * Asks for the distance once the click that placed a dimension is over.
     */
    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (gesture != null) {
            gesture.end(event, design);
            gesture = null;
        }
        update(design);
        if (pendingEdit != null) {
            DistanceConstraint constraint = pendingEdit;
            pendingEdit = null;
            ConstraintEditing.editDistance(context, constraint);
        }
    }

    @Override
    public void onMoved(PointerEvent event, Point2D design) {
        update(design);
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        Constraint hovered = context.state().hoveredConstraint();
        if (hovered != null) {
            return Optional.of(DimensionStyle.dragCursor(hovered));
        }
        return Optional.of(Cursor.CROSSHAIR);
    }

    @Override
    public void deactivate() {
        placement.reset();
        pendingEdit = null;
        gesture = null;
        context.state().setHoveredConstraint(null);
        context.state().setSnapMarkers(List.of());
        context.state().clearTransient();
    }

    private void update(Point2D design) {
        Optional<Snap> snap = placement.isPlacing() ? Optional.empty() : snapAt(design);
        context.state().setSnapMarkers(placement.markers(snap, context.hitTester().entityAt(design, context.pickTolerance(design))));
        context.state().setHoveredConstraint(placement.isIdle() && snap.isEmpty()
                ? context.constraintAt(design).orElse(null)
                : null);

        Optional<ConstraintPlacement.Preview> preview = placement.preview(snap, design);
        context.state().setPreviewDimension(preview.map(ConstraintPlacement.Preview::dimension).orElse(null));
        context.state().setReadout(preview.map(ConstraintPlacement.Preview::readout).orElse(null), preview.isPresent() ? design : null);
        context.render();
    }

    private Optional<Snap> snapAt(Point2D design) {
        Optional<Snap> snap = context.hitTester().snapAt(design, ConstraintPlacement.SNAP_PX * context.worldUnitsPerPixelAt(design));
        return placement.pickAt(snap, context.hitTester().entityAt(design, context.pickTolerance(design)), design);
    }
}
