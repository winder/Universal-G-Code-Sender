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

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * The default tool. A press on a handle resizes or rotates, a press inside the selection moves
 * it, a press anywhere else starts a rubber band or a click selection, and a double click on a
 * text entity opens the text editor. With Shift held the press adds to or removes from the
 * selection instead of moving it, so already selected entities can be deselected one by one.
 */
public final class SelectTool implements EditorTool {
    private final ToolContext context;
    private Gesture gesture;

    public SelectTool(ToolContext context) {
        this.context = context;
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Optional<HandleSet.Handle> handle = handleAt(design);
        if (handle.isPresent()) {
            gesture = handle.get().kind() == HandleSet.Kind.ROTATE
                    ? new RotateGesture(context)
                    : new ResizeGesture(context, handle.get().anchor());
        } else if (event.clickCount() == 2 && textAt(design).isPresent()) {
            gesture = null;
            context.textEditor().accept(textAt(design).get());
            return;
        } else if (!event.isShiftDown() && isInsideSelection(design)) {
            gesture = new MoveGesture(context);
        } else {
            gesture = new MarqueeGesture(context);
        }
        gesture.begin(event, design);
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (gesture != null) {
            gesture.drag(event, design);
        }
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (gesture != null) {
            gesture.end(event, design);
            gesture = null;
        }
    }

    @Override
    public void onMoved(PointerEvent event, Point2D design) {
        EditorState state = context.state();
        HandleSet.Handle handle = handleAt(design).orElse(null);
        Entity entity = handle == null && !isInsideSelection(design)
                ? context.hitTester().entityAt(design, context.pickTolerance(design)).orElse(null)
                : null;
        if (handle != state.hoveredHandle() || entity != state.hoveredEntity()) {
            state.setHoveredHandle(handle);
            state.setHoveredEntity(entity);
            context.render();
        }
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        Optional<HandleSet.Handle> handle = handleAt(design);
        if (handle.isPresent()) {
            return Optional.of(cursorFor(handle.get()));
        }
        if (!event.isShiftDown() && isInsideSelection(design)) {
            return Optional.of(Cursor.MOVE);
        }
        if (context.hitTester().entityAt(design, context.pickTolerance(design)).isPresent()) {
            return Optional.of(Cursor.HAND);
        }
        return Optional.empty();
    }

    @Override
    public void deactivate() {
        gesture = null;
        context.state().clearTransient();
        context.state().setHoveredHandle(null);
        context.state().setHoveredEntity(null);
    }

    public List<HandleSet.Handle> handles() {
        return HandleSet.handles(context.selection(), context::worldUnitsPerPixelAt);
    }

    private Optional<HandleSet.Handle> handleAt(Point2D design) {
        return HandleSet.handleAt(handles(), design);
    }

    private boolean isInsideSelection(Point2D design) {
        SelectionManager selection = context.selection();
        if (selection.isEmpty()) {
            return false;
        }
        Shape shape = selection.getShape();
        return shape != null && shape.contains(design);
    }

    private Optional<Text> textAt(Point2D design) {
        return context.hitTester().entityAt(design, context.pickTolerance(design))
                .filter(Text.class::isInstance)
                .map(Text.class::cast);
    }

    /**
     * The resize cursor pointing the way the handle lies from the selection's centre. Cursors
     * do not turn with a rotated selection, so the direction is measured on screen, where Y
     * grows downwards.
     */
    private Cursor cursorFor(HandleSet.Handle handle) {
        if (handle.kind() == HandleSet.Kind.ROTATE) {
            return Cursor.HAND;
        }
        Point2D center = context.selection().getCenter();
        double dx = handle.center().getX() - center.getX();
        double dy = handle.center().getY() - center.getY();
        double threshold = Math.max(Math.abs(dx), Math.abs(dy)) * 0.4;
        boolean east = dx > threshold;
        boolean west = dx < -threshold;
        boolean north = dy > threshold;
        boolean south = dy < -threshold;
        if (north && east) return Cursor.NE_RESIZE;
        if (north && west) return Cursor.NW_RESIZE;
        if (south && east) return Cursor.SE_RESIZE;
        if (south && west) return Cursor.SW_RESIZE;
        if (north) return Cursor.N_RESIZE;
        if (south) return Cursor.S_RESIZE;
        if (east) return Cursor.E_RESIZE;
        if (west) return Cursor.W_RESIZE;
        return Cursor.CROSSHAIR;
    }
}
