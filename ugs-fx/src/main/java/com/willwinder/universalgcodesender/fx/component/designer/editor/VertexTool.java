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
import com.willwinder.ugs.designer.model.path.PointType;
import com.willwinder.ugs.designer.model.path.Segment;
import com.willwinder.ugs.designer.model.path.SegmentType;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.utils.PathUtils;
import com.willwinder.universalgcodesender.fx.component.designer.editor.EditorState.VertexHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Edits the nodes of a path. Selecting a path shows a handle on every node; clicking one
 * selects it and shows the curve handles that shape the curves on either side of it, and with
 * Shift held adds it to or removes it from the selected nodes. Dragging a selected node moves
 * all selected nodes together with their curve handles, dragging a curve handle reshapes the
 * curve. Each drag reshapes the path live and records one {@link ChangePathAction} on release.
 * Dragging a rubber band away from the nodes selects the nodes inside it, adding to the
 * selected ones with Shift. Clicking elsewhere deselects the nodes and selects the path under the cursor or clears the
 * selection. Double clicking the path's outline adds a node there, double clicking one of its
 * points or inside it goes back to the select tool. Nodes can also be added between selected
 * nodes, and turned into curve or corner nodes, through {@link VertexEditing}.
 */
public final class VertexTool implements EditorTool {
    public static final double NODE_SIZE_PX = 9;
    public static final double CURVE_HANDLE_SIZE_PX = 7;
    private static final double HIT_FACTOR = 1.3;
    private static final double SAME_POSITION = 1e-9;
    /** A rubber band no larger than this in millimeters either way counts as a click. */
    private static final double MARQUEE_CLICK_EXTENT = 1;

    /**
     * One editable point: which segment it belongs to, the segment's place in the path and the
     * point's index within the segment.
     */
    private record Vertex(Segment segment, int segmentIndex, int index) {
        Point2D position() {
            return segment.getPoint(index);
        }

        boolean isNode() {
            return segment.getType().getPointTypes().get(index) == PointType.COORDINATE;
        }
    }

    /**
     * A curve handle and the node it belongs to.
     */
    private record CurveHandle(Vertex handle, Vertex node) {
    }

    private final ToolContext context;
    private Path target;
    private List<Segment> segments = List.of();
    private List<Vertex> nodes = List.of();
    // By place in nodes: the start and end of a closed subpath, which are one node
    private Map<Integer, Integer> joined = Map.of();
    // By place in nodes: the ends of open subpaths, which connect to only one other node
    private Set<Integer> endpoints = Set.of();
    private List<Vertex> selectedNodes = new ArrayList<>();
    private Vertex hovered;
    private Vertex dragged;
    private boolean moved;
    private boolean narrowOnRelease;
    private Point2D marqueeStart;
    private VertexEditing.Abilities abilities = VertexEditing.Abilities.NONE;
    // What the abilities were found for
    private List<Segment> abilitiesSegments;
    private List<Vertex> abilitiesNodes = List.of();
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
        hovered = null;
        marqueeStart = null;
        context.state().setRubberBand(null);
        selectedNodes = new ArrayList<>();
        target = null;
        segments = List.of();
        nodes = List.of();
        context.state().setVertices(List.of());
        VertexEditing.clear(this);
    }

    @Override
    public void onSelectionChanged() {
        refresh();
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Optional<Vertex> vertex = vertexAt(design, event.isShiftDown());
        if (event.clickCount() == 2 && target != null && vertex.isEmpty() && insertNodeAt(design)) {
            return;
        }
        if (event.clickCount() == 2 && target != null && (vertex.isPresent() || isOnTarget(design))) {
            Path path = target;
            dragged = null;
            context.selectTool(Tool.SELECT);
            context.selection().setSelection(List.of(path));
            return;
        }
        if (vertex.isPresent() && target != null) {
            Vertex pressed = vertex.get();
            if (pressed.isNode() && event.isShiftDown() && selectedNodes.contains(pressed)) {
                // Shift on a selected node takes it out of the selection, nothing to drag
                selectedNodes.remove(pressed);
                dragged = null;
                publishVertices();
                context.render();
                return;
            }
            // A plain press on an already selected node keeps the others so they can be dragged
            // together, and narrows the selection down to it if released without dragging
            narrowOnRelease = pressed.isNode() && !event.isShiftDown() && selectedNodes.contains(pressed)
                    && selectedNodes.size() > 1;
            if (pressed.isNode() && event.isShiftDown()) {
                selectedNodes.add(pressed);
            } else if (pressed.isNode() && !selectedNodes.contains(pressed)) {
                selectedNodes = new ArrayList<>(List.of(pressed));
            }
            dragged = pressed;
            moved = false;
            originalShape = (Path2D) target.getShape();
            publishVertices();
            context.render();
            return;
        }
        dragged = null;
        if (target != null) {
            // A drag selects the nodes inside a rubber band, a click is handled on release
            marqueeStart = design;
            context.state().setRubberBand(null);
            return;
        }
        selectAt(design);
    }

    /**
     * A click away from the nodes: selects the path under the cursor, or clears the selection.
     * Either way the selected nodes are dropped.
     */
    private void selectAt(Point2D design) {
        selectedNodes = new ArrayList<>();
        Optional<Entity> hit = context.hitTester().entityAt(design, context.pickTolerance(design));
        if (hit.isPresent()) {
            context.selection().setSelection(List.of(hit.get()));
        } else {
            context.selection().clearSelection();
        }
        refresh();
    }

    /**
     * Ends a press away from the nodes. A drag selects the nodes inside the rubber band, adding
     * them to the selected ones with Shift. A click selects the path under the cursor or clears
     * the selection, unless Shift is held so a missed node does not lose the selection.
     */
    private void endMarquee(PointerEvent event, Point2D design) {
        Rectangle2D rectangle = MarqueeGesture.rectangle(marqueeStart, design);
        marqueeStart = null;
        context.state().setRubberBand(null);
        if (rectangle.getWidth() > MARQUEE_CLICK_EXTENT || rectangle.getHeight() > MARQUEE_CLICK_EXTENT) {
            List<Vertex> inside = nodes.stream().filter(node -> rectangle.contains(node.position())).toList();
            List<Vertex> selection = event.isShiftDown() ? new ArrayList<>(selectedNodes) : new ArrayList<>();
            // The start and end of a closed shape are one node, so only one of the two is taken
            for (Vertex node : inside) {
                if (joinedWith(node).stream().noneMatch(selection::contains)) {
                    selection.add(node);
                }
            }
            selectedNodes = selection;
            publishVertices();
            context.render();
        } else if (event.isShiftDown()) {
            context.render();
        } else {
            selectAt(design);
        }
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (marqueeStart != null) {
            context.state().setRubberBand(MarqueeGesture.rectangle(marqueeStart, design));
            context.render();
            return;
        }
        if (dragged == null || target == null) {
            return;
        }
        Point2D position = context.snapper().snap(design);
        if (dragged.isNode()) {
            moveSelectedNodes(position.getX() - dragged.position().getX(), position.getY() - dragged.position().getY());
        } else {
            dragged.segment().setPosition(dragged.index(), position);
        }
        moved = true;
        target.setShape(toRelative(PathUtils.toPath2D(segments)));
        publishVertices();
        context.render();
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (marqueeStart != null) {
            endMarquee(event, design);
            return;
        }
        if (dragged == null || target == null) {
            return;
        }
        if (moved) {
            ChangePathAction action = new ChangePathAction(context.controller(), target, originalShape,
                    PathUtils.toPath2D(segments));
            action.redo();
            context.undoManager().addAction(action);
        } else if (narrowOnRelease) {
            selectedNodes = new ArrayList<>(List.of(dragged));
        }
        narrowOnRelease = false;
        dragged = null;
        originalShape = null;
        refresh();
    }

    @Override
    public void onMoved(PointerEvent event, Point2D design) {
        Vertex vertex = vertexAt(design).orElse(null);
        if (!Objects.equals(vertex, hovered)) {
            hovered = vertex;
            publishVertices();
            context.render();
        }
    }

    /**
     * Deletes the selected nodes, joining the curves on either side of each. When the path would
     * have nothing left to draw it is left to the default delete, which removes the whole path.
     */
    @Override
    public boolean onDelete() {
        if (target == null || selectedNodes.isEmpty() || dragged != null) {
            return false;
        }
        List<PathNodes.NodeAt> toDelete = selectedNodes.stream()
                .map(node -> new PathNodes.NodeAt(node.segmentIndex(), node.position()))
                .toList();
        Optional<Path2D> remaining = PathNodes.deleteNodes(segments, toDelete);
        if (remaining.isEmpty()) {
            return false;
        }
        applyChange(remaining.get());
        return true;
    }

    /**
     * Adds a node where the cursor is on the path's outline and selects it, as one undoable
     * change.
     *
     * @return true when the cursor was on the outline and a node was added
     */
    private boolean insertNodeAt(Point2D design) {
        Optional<PathNodes.CurvePoint> curvePoint = PathNodes.nearestCurvePoint(segments, design)
                .filter(point -> point.position().distance(design) <= context.pickTolerance(design));
        if (curvePoint.isEmpty()) {
            return false;
        }
        Optional<Path2D> inserted = PathNodes.insertNode(segments, curvePoint.get().segmentIndex(), curvePoint.get().t());
        if (inserted.isEmpty()) {
            return false;
        }
        Point2D position = curvePoint.get().position();
        dragged = null;
        applyChange(inserted.get());
        selectedNodes = new ArrayList<>(nodesAt(position).stream().limit(1).toList());
        publishVertices();
        context.render();
        return true;
    }

    /**
     * Adds a node halfway between each two selected nodes next to each other, as one undoable
     * change. The new nodes are selected along with the old ones.
     *
     * @return true when nodes were added
     */
    boolean addNodesBetweenSelected() {
        if (target == null || dragged != null) {
            return false;
        }
        Optional<Path2D> inserted = PathNodes.insertNodesBetween(segments, selectedNodeLocations());
        if (inserted.isEmpty()) {
            return false;
        }
        List<Point2D> before = nodes.stream().map(node -> (Point2D) new Point2D.Double(node.position().getX(), node.position().getY())).toList();
        List<Point2D> selectedPositions = selectedNodes.stream()
                .map(node -> (Point2D) new Point2D.Double(node.position().getX(), node.position().getY())).toList();
        applyChange(inserted.get());
        // The old selection plus the nodes that were not there before
        selectedNodes = new ArrayList<>(nodes.stream()
                .filter(node -> isAt(node, selectedPositions) || !isAt(node, before))
                .toList());
        publishVertices();
        context.render();
        return true;
    }

    /**
     * Turns the selected nodes into smooth curve nodes, as one undoable change.
     *
     * @return true when a node was changed
     */
    boolean makeSelectedNodesCurves() {
        if (target == null || dragged != null) {
            return false;
        }
        return applyKeepingSelection(PathNodes.makeCurveNodes(segments, selectedNodeLocations()));
    }

    /**
     * Turns the selected nodes into corner nodes with straight lines on both sides, as one
     * undoable change.
     *
     * @return true when a node was changed
     */
    boolean makeSelectedNodesLines() {
        if (target == null || dragged != null) {
            return false;
        }
        return applyKeepingSelection(PathNodes.makeLineNodes(segments, selectedNodeLocations()));
    }

    /**
     * Applies a change that keeps the nodes where they are, so the same nodes stay selected.
     */
    private boolean applyKeepingSelection(Optional<Path2D> changed) {
        if (changed.isEmpty()) {
            return false;
        }
        SelectedNodes before = selectedNodesSnapshot();
        applyChange(changed.get());
        restoreSelectedNodes(before);
        publishVertices();
        context.render();
        return true;
    }

    /**
     * Replaces the path's shape as one undoable change and rebuilds the nodes. The selected
     * nodes are dropped, for the caller to select the ones it wants.
     */
    private void applyChange(Path2D shape) {
        ChangePathAction action = new ChangePathAction(context.controller(), target, (Path2D) target.getShape(), shape);
        action.redo();
        context.undoManager().addAction(action);
        selectedNodes = new ArrayList<>();
        refresh();
    }

    private static boolean isAt(Vertex node, List<Point2D> positions) {
        return positions.stream().anyMatch(position -> node.position().distance(position) <= SAME_POSITION);
    }

    private List<PathNodes.NodeAt> selectedNodeLocations() {
        return selectedNodes.stream()
                .map(node -> new PathNodes.NodeAt(node.segmentIndex(), node.position()))
                .toList();
    }

    /**
     * Breaks the path at the selected node, as one undoable change. The node stays selected,
     * now as the end of one of the parts. Only done with exactly one node selected.
     *
     * @return true when the path was broken
     */
    boolean breakAtSelectedNode() {
        if (target == null || selectedNodes.size() != 1 || dragged != null) {
            return false;
        }
        Vertex selectedNode = selectedNodes.getFirst();
        Point2D position = new Point2D.Double(selectedNode.position().getX(), selectedNode.position().getY());
        Optional<Path2D> broken = PathNodes.breakAtNode(segments, selectedNode.segmentIndex(), position);
        if (broken.isEmpty()) {
            return false;
        }
        applyChange(broken.get());
        selectedNodes = new ArrayList<>(nodesAt(position).stream().limit(1).toList());
        publishVertices();
        context.render();
        return true;
    }

    /**
     * Joins the two selected loose ends into one node, as one undoable change. The joined node
     * is selected afterwards.
     *
     * @return true when the nodes were joined
     */
    boolean joinSelectedNodes() {
        if (target == null || dragged != null) {
            return false;
        }
        Optional<Path2D> joinedPath = joinedPath();
        if (joinedPath.isEmpty()) {
            return false;
        }
        Point2D a = selectedNodes.get(0).position();
        Point2D b = selectedNodes.get(1).position();
        Point2D middle = new Point2D.Double((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
        applyChange(joinedPath.get());
        selectedNodes = new ArrayList<>(nodesAt(middle).stream().limit(1).toList());
        publishVertices();
        context.render();
        return true;
    }

    /**
     * The path with the two selected nodes joined, or empty unless exactly two loose ends are
     * selected that can be joined.
     */
    private Optional<Path2D> joinedPath() {
        if (selectedNodes.size() != 2) {
            return Optional.empty();
        }
        Vertex a = selectedNodes.get(0);
        Vertex b = selectedNodes.get(1);
        return PathNodes.joinNodes(segments,
                new PathNodes.NodeAt(a.segmentIndex(), a.position()),
                new PathNodes.NodeAt(b.segmentIndex(), b.position()));
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        if (vertexAt(design).isPresent()) {
            return Optional.of(Cursor.HAND);
        }
        return context.hitTester().entityAt(design, context.pickTolerance(design)).map(entity -> Cursor.HAND);
    }

    /**
     * Where the selected nodes are, to select the same nodes again after the path is rebuilt.
     */
    private record SelectedNodes(List<Integer> indexes, List<Point2D> positions, int nodeCount) {
    }

    private SelectedNodes selectedNodesSnapshot() {
        List<Integer> indexes = new ArrayList<>();
        List<Point2D> positions = new ArrayList<>();
        for (Vertex node : selectedNodes) {
            int index = nodes.indexOf(node);
            if (index >= 0) {
                indexes.add(index);
                positions.add(new Point2D.Double(node.position().getX(), node.position().getY()));
            }
        }
        return new SelectedNodes(indexes, positions, nodes.size());
    }

    /**
     * Selects the same nodes again. While the path has as many nodes as before, as after a
     * drag, they are found by their places in the path. When nodes were added or removed, as by
     * an undo, the places have shifted, so they are found by position and dropped when no
     * longer there.
     */
    private void restoreSelectedNodes(SelectedNodes snapshot) {
        List<Vertex> restored = new ArrayList<>();
        if (snapshot.nodeCount() == nodes.size()) {
            snapshot.indexes().forEach(index -> restored.add(nodes.get(index)));
        } else {
            for (Point2D position : snapshot.positions()) {
                nodesAt(position).stream()
                        .filter(node -> !restored.contains(node))
                        .findFirst()
                        .ifPresent(restored::add);
            }
        }
        selectedNodes = restored;
    }

    /**
     * Rebuilds the editable points from the selected path, if a single path is selected. The
     * selected nodes are kept while the same path stays selected, so they survive the rebuild
     * after a drag, see {@link #restoreSelectedNodes}.
     */
    private void refresh() {
        List<Entity> selected = context.selection().getSelection();
        SelectedNodes snapshot = selectedNodesSnapshot();
        if (selected.size() == 1 && selected.getFirst() instanceof Path path) {
            boolean samePath = path == target;
            target = path;
            segments = PathUtils.getSegments(path.getShape());
            List<Vertex> points = new ArrayList<>();
            for (int segmentIndex = 0; segmentIndex < segments.size(); segmentIndex++) {
                Segment segment = segments.get(segmentIndex);
                for (int index = 0; index < segment.getPoints().length; index++) {
                    Vertex vertex = new Vertex(segment, segmentIndex, index);
                    if (vertex.isNode()) {
                        points.add(vertex);
                    }
                }
            }
            nodes = points;
            findJoinedAndEndpoints();
            if (samePath) {
                restoreSelectedNodes(snapshot);
            } else {
                selectedNodes = new ArrayList<>();
            }
        } else {
            target = null;
            segments = List.of();
            nodes = List.of();
            joined = Map.of();
            endpoints = Set.of();
            selectedNodes = new ArrayList<>();
        }
        hovered = null;
        publishVertices();
        context.render();
    }

    private void publishVertices() {
        List<VertexHandle> handles = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Vertex node = nodes.get(i);
            handles.add(new VertexHandle(node.position(), null, endpoints.contains(i),
                    selectedNodes.contains(node), node.equals(hovered)));
        }
        for (CurveHandle curveHandle : curveHandles()) {
            Vertex handle = curveHandle.handle();
            handles.add(new VertexHandle(handle.position(), curveHandle.node().position(), false, false,
                    handle.equals(hovered)));
        }
        context.state().setVertices(handles);
        // Whether the path can be broken only changes with the selected nodes, not while dragging them
        // Only changes with the selected nodes and the path's shape, not while dragging or hovering
        if (dragged == null && (segments != abilitiesSegments || !selectedNodes.equals(abilitiesNodes))) {
            abilities = findAbilities();
            abilitiesSegments = segments;
            abilitiesNodes = List.copyOf(selectedNodes);
        }
        VertexEditing.update(this, !selectedNodes.isEmpty(), abilities);
    }

    /**
     * What can be done with the selected nodes, found by trying each change.
     */
    private VertexEditing.Abilities findAbilities() {
        if (selectedNodes.isEmpty()) {
            return VertexEditing.Abilities.NONE;
        }
        List<PathNodes.NodeAt> locations = selectedNodeLocations();
        boolean canBreak = selectedNodes.size() == 1 && PathNodes.breakAtNode(segments,
                selectedNodes.getFirst().segmentIndex(), selectedNodes.getFirst().position()).isPresent();
        return new VertexEditing.Abilities(canBreak, joinedPath().isPresent(),
                PathNodes.insertNodesBetween(segments, locations).isPresent(),
                PathNodes.makeCurveNodes(segments, locations).isPresent(),
                PathNodes.makeLineNodes(segments, locations).isPresent());
    }

    /**
     * The curve handles of the selected nodes, and of the nodes they are joined with when one is
     * the start or end of a closed shape. None when no node is selected. A handle shared by two
     * selected nodes, as on a quadratic curve, is listed once.
     */
    private List<CurveHandle> curveHandles() {
        List<CurveHandle> handles = new ArrayList<>();
        Set<Point2D> listed = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Vertex selected : selectedNodes) {
            for (Vertex node : joinedWith(selected)) {
                for (CurveHandle curveHandle : curveHandlesOf(node)) {
                    if (listed.add(curveHandle.handle().position())) {
                        handles.add(curveHandle);
                    }
                }
            }
        }
        return handles;
    }

    /**
     * The curve handles on either side of a node: the one ending the curve into it and the one
     * starting the curve out of it.
     */
    private List<CurveHandle> curveHandlesOf(Vertex node) {
        List<CurveHandle> handles = new ArrayList<>(2);
        Segment incoming = node.segment();
        if (incoming.getType() == SegmentType.CUBIC_TO) {
            handles.add(new CurveHandle(new Vertex(incoming, node.segmentIndex(), 1), node));
        } else if (incoming.getType() == SegmentType.QUAD_TO) {
            handles.add(new CurveHandle(new Vertex(incoming, node.segmentIndex(), 0), node));
        }
        int next = node.segmentIndex() + 1;
        if (next < segments.size()) {
            Segment outgoing = segments.get(next);
            if (outgoing.getType() == SegmentType.CUBIC_TO || outgoing.getType() == SegmentType.QUAD_TO) {
                handles.add(new CurveHandle(new Vertex(outgoing, next, 0), node));
            }
        }
        return handles;
    }

    private boolean isOnTarget(Point2D design) {
        return context.hitTester().entityAt(design, context.pickTolerance(design))
                .filter(entity -> entity == target)
                .isPresent();
    }

    /**
     * The node and, when it is the start or end of a closed subpath, the other one of the two.
     */
    private List<Vertex> joinedWith(Vertex node) {
        int index = nodes.indexOf(node);
        Integer other = joined.get(index);
        return other == null ? List.of(node) : List.of(node, nodes.get(other));
    }

    /**
     * Works out from the structure of the path, not from where the nodes are, which nodes are
     * one and which are loose ends. A subpath ending with a close segment is closed, and when it
     * returns to its start its first and last node are one. Any other subpath is open and its
     * first and last node are ends.
     */
    private void findJoinedAndEndpoints() {
        Map<Integer, Integer> joinedNodes = new HashMap<>();
        Set<Integer> ends = new HashSet<>();
        int first = -1;
        boolean closed = false;
        int nodeIndex = 0;
        for (Segment segment : segments) {
            if (segment.getType() == SegmentType.MOVE_TO) {
                finishSubpath(first, nodeIndex - 1, closed, joinedNodes, ends);
                first = nodeIndex;
                closed = false;
            } else if (segment.getType() == SegmentType.CLOSE) {
                closed = true;
            } else if (first < 0) {
                first = nodeIndex;
            }
            if (segment.getType() != SegmentType.CLOSE) {
                nodeIndex++;
            }
        }
        finishSubpath(first, nodeIndex - 1, closed, joinedNodes, ends);
        joined = joinedNodes;
        endpoints = ends;
    }

    private void finishSubpath(int first, int last, boolean closed, Map<Integer, Integer> joinedNodes, Set<Integer> ends) {
        if (first < 0 || last < first) {
            return;
        }
        if (!closed) {
            ends.add(first);
            ends.add(last);
        } else if (last > first && nodes.get(first).position().distance(nodes.get(last).position()) <= SAME_POSITION) {
            joinedNodes.put(first, last);
            joinedNodes.put(last, first);
        }
    }

    private List<Vertex> nodesAt(Point2D position) {
        return nodes.stream()
                .filter(node -> node.position().distance(position) <= SAME_POSITION)
                .toList();
    }

    /**
     * Moves the selected nodes, the nodes they are joined with and their curve handles by the
     * same amount, so curves keep their shape around the nodes and a closed shape stays closed.
     * Nodes that merely share a position, like the two ends left by breaking a path, move apart.
     * Every point is moved once, even when it belongs to more than one of the moving nodes.
     */
    private void moveSelectedNodes(double dx, double dy) {
        List<Vertex> moving = new ArrayList<>();
        for (Vertex selected : selectedNodes) {
            moving.addAll(joinedWith(selected));
        }
        Set<Point2D> moved = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Vertex node : moving) {
            for (CurveHandle curveHandle : curveHandlesOf(node)) {
                movePoint(curveHandle.handle(), dx, dy, moved);
            }
        }
        for (Vertex node : moving) {
            movePoint(node, dx, dy, moved);
        }
    }

    private static void movePoint(Vertex vertex, double dx, double dy, Set<Point2D> moved) {
        Point2D point = vertex.position();
        if (moved.add(point)) {
            vertex.segment().setPosition(vertex.index(), new Point2D.Double(point.getX() + dx, point.getY() + dy));
        }
    }

    private Optional<Vertex> vertexAt(Point2D design) {
        return vertexAt(design, false);
    }

    /**
     * The point under the cursor. Curve handles come first since they can sit on top of their
     * node, then the selected nodes, then the others. When adding to the selection the nodes
     * not yet selected come before the selected ones, so the second of two nodes on the same
     * spot, like the ends left by breaking a path, can be added.
     */
    private Optional<Vertex> vertexAt(Point2D design, boolean preferUnselected) {
        List<Vertex> candidates = new ArrayList<>();
        curveHandles().forEach(curveHandle -> candidates.add(curveHandle.handle()));
        if (preferUnselected) {
            nodes.stream().filter(node -> !selectedNodes.contains(node)).forEach(candidates::add);
        }
        candidates.addAll(selectedNodes);
        candidates.addAll(nodes);
        return candidates.stream()
                .filter(vertex -> {
                    double size = vertex.isNode() ? NODE_SIZE_PX : CURVE_HANDLE_SIZE_PX;
                    double reach = size * context.worldUnitsPerPixelAt(vertex.position()) / 2 * HIT_FACTOR;
                    return vertex.position().distance(design) <= reach;
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
