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

import com.willwinder.ugs.designer.actions.ChangePathAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityException;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.model.path.Segment;
import com.willwinder.ugs.designer.utils.PathUtils;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Edits the points of a path. Selecting a path shows a handle on every point; dragging one
 * reshapes the path live and records one {@link ChangePathAction} on release. Clicking
 * elsewhere selects the path under the cursor or clears the selection.
 */
public final class VertexTool implements EditorTool {
    public static final double HANDLE_SIZE_PX = 6;
    private static final double HIT_FACTOR = 1.3;

    /** One editable point: which segment it belongs to and its index within that segment. */
    private record Vertex(Segment segment, int index) {
        Point2D position() {
            return segment.getPoint(index);
        }
    }

    private final ToolContext context;
    private Path target;
    private List<Segment> segments = List.of();
    private List<Vertex> vertices = List.of();
    private Vertex dragged;
    private Path2D originalShape;

    public VertexTool(ToolContext context) {
        this.context = context;
    }

    @Override
    public void activate() {
        refresh();
    }

    @Override
    public void deactivate() {
        dragged = null;
        target = null;
        segments = List.of();
        vertices = List.of();
        context.state().setVertices(List.of(), -1);
    }

    @Override
    public void onSelectionChanged() {
        refresh();
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Optional<Vertex> vertex = vertexAt(design);
        if (vertex.isPresent() && target != null) {
            dragged = vertex.get();
            originalShape = (Path2D) target.getShape();
            return;
        }
        dragged = null;
        Optional<Entity> hit = context.hitTester().entityAt(design, context.pickTolerance(design));
        if (hit.isPresent()) {
            context.selection().setSelection(List.of(hit.get()));
        } else {
            context.selection().clearSelection();
        }
        refresh();
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (dragged == null || target == null) {
            return;
        }
        dragged.segment().setPosition(dragged.index(), context.snapper().snap(design));
        target.setShape(toRelative(PathUtils.toPath2D(segments)));
        publishVertices(vertices.indexOf(dragged));
        context.render();
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (dragged == null || target == null) {
            return;
        }
        ChangePathAction action = new ChangePathAction(context.controller(), target, originalShape,
                PathUtils.toPath2D(segments));
        action.redo();
        context.undoManager().addAction(action);
        dragged = null;
        originalShape = null;
        refresh();
    }

    @Override
    public void onMoved(PointerEvent event, Point2D design) {
        int hovered = vertexAt(design).map(vertices::indexOf).orElse(-1);
        if (hovered != context.state().hoveredVertex()) {
            publishVertices(hovered);
            context.render();
        }
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        if (vertexAt(design).isPresent()) {
            return Optional.of(Cursor.HAND);
        }
        return context.hitTester().entityAt(design, context.pickTolerance(design)).map(entity -> Cursor.HAND);
    }

    /**
     * Rebuilds the editable points from the selected path, if a single path is selected.
     */
    private void refresh() {
        List<Entity> selected = context.selection().getSelection();
        if (selected.size() == 1 && selected.get(0) instanceof Path path) {
            target = path;
            segments = PathUtils.getSegments(path.getShape());
            List<Vertex> points = new ArrayList<>();
            for (Segment segment : segments) {
                for (int index = 0; index < segment.getPoints().length; index++) {
                    points.add(new Vertex(segment, index));
                }
            }
            vertices = points;
        } else {
            target = null;
            segments = List.of();
            vertices = List.of();
        }
        publishVertices(-1);
        context.render();
    }

    private void publishVertices(int hovered) {
        context.state().setVertices(vertices.stream().map(Vertex::position).toList(), hovered);
    }

    private Optional<Vertex> vertexAt(Point2D design) {
        return vertices.stream()
                .filter(vertex -> {
                    double reach = HANDLE_SIZE_PX * context.worldUnitsPerPixelAt(vertex.position()) / 2 * HIT_FACTOR;
                    return Math.abs(vertex.position().getX() - design.getX()) <= reach
                            && Math.abs(vertex.position().getY() - design.getY()) <= reach;
                })
                .findFirst();
    }

    private Path2D toRelative(Path2D absolute) {
        try {
            return (Path2D) target.getTransform().createInverse().createTransformedShape(absolute);
        } catch (NoninvertibleTransformException e) {
            throw new EntityException("Could not transform the path", e);
        }
    }
}
