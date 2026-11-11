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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityEvent;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.universalgcodesender.i18n.Localization;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

/**
 * Holds the constraints of a design and keeps them satisfied. Whenever an entity in the design
 * moves or changes shape it stays where it was put and the entities constrained to it are moved
 * to satisfy the constraints again, whichever end of the constraints it is at. The held
 * entities, such as the selected ones being dragged, are never moved by a constraint, so moving
 * two constrained entities together leaves both where they were put. When no entity was moved,
 * as when a constraint is added or its distance changed, references stay and targets follow.
 * <p>
 * Each entity can be driven by at most one constraint per axis and the constraints may not form
 * a loop, so the constraints on an axis join the entities as trees and every entity can be
 * reached from the moved one in exactly one way. The order the points of a constraint are picked
 * in does not matter for that: when its target is already driven, the constraints from the
 * target up to the root of its tree are flipped, so the target becomes the root and can be
 * driven by the new constraint. Only constraints that would join entities already joined on the
 * axis are refused. A coincidence holds both axes, so it counts on each of them.
 * <p>
 * Sizes are held the same way, apart from the positions: equal sizes join the widths and heights
 * of the entities as trees, flipped as needed, the width of one entity possibly joined to the
 * height of another. Each tree can have one size held by a size constraint, which is then its
 * root. The sizes are taken from the root, or from the entity that was resized when no size is
 * held. Sizes are applied first, as
 * moving an entity into place afterwards does not change its size.
 * Constraints whose entities have been removed from the design are kept but inactive, so they
 * come back when the removal is undone.
 * <p>
 * One constraint at a time can be selected, apart from the selected entities, so it can be
 * edited or deleted.
 */
public class ConstraintManager {
    /**
     * Why a constraint can not be added.
     */
    public enum Conflict {
        /**
         * The target can not be freed to be driven on the axis, as flipping the constraints to
         * free it on another axis would leave it driven twice on this one.
         */
        POSITION_CONSTRAINED,
        /** The entities are already joined by constraints on the axis. */
        LOOP,
        /** The entity already has a size constraint on the axis. */
        SIZE_CONSTRAINED,
        /** The points of a size constraint are not apart along the axis. */
        NO_SIZE,
        /** The sizes are already joined by equal sizes. */
        SIZE_LINKED,
        /** An entity of an equal size has no size on the axis. */
        EMPTY_SHAPE;

        /**
         * Explains the conflict to the user, for a constraint on the given axis.
         */
        public String message(Axis axis) {
            String key = switch (this) {
                case POSITION_CONSTRAINED -> "positionConstrained";
                case LOOP -> "loop";
                case SIZE_CONSTRAINED -> "sizeConstrained";
                case NO_SIZE -> "noSize";
                case SIZE_LINKED -> "sizeLinked";
                case EMPTY_SHAPE -> "emptyShape";
            };
            String axisSuffix = this == SIZE_LINKED ? "" : "." + axis.name().toLowerCase(Locale.ROOT);
            return Localization.getString("designer.constraint.conflict." + key + axisSuffix);
        }
    }

    private record AxisConflict(Conflict conflict, Axis axis) {
    }

    // A position or a size of an entity along one axis, which the constraints join as trees
    private record Node(Entity entity, Axis axis) {
    }

    // A link of a constraint, from the node driving it to the node it drives
    private record Edge(Constraint constraint, Node parent, Node child) {
        Edge flipped() {
            return new Edge(constraint, child, parent);
        }

        Node other(Node node) {
            return node.equals(parent) ? child : parent;
        }

        // Moves or resizes the entity of the node to satisfy the constraint, the other node staying as it is. Returns
        // whether it was moved.
        boolean applyTo(Node node) {
            return switch (constraint) {
                case EqualSizeConstraint equalSize -> {
                    equalSize.applyResizing(node.equals(child));
                    yield false;
                }
                case PointConstraint pointConstraint -> pointConstraint.applyMoving(node.entity(), node.axis());
            };
        }
    }

    // The constraints joining positions as trees, and separately the equal sizes joining sizes
    private enum Layer {
        POSITION(Conflict.LOOP, Conflict.POSITION_CONSTRAINED),
        SIZE(Conflict.SIZE_LINKED, Conflict.SIZE_CONSTRAINED);

        // Why a constraint can not join nodes already joined, or drive a node already driven
        private final Conflict joined;
        private final Conflict driven;

        Layer(Conflict joined, Conflict driven) {
            this.joined = joined;
            this.driven = driven;
        }

        List<Edge> edges(Constraint constraint) {
            if (this == SIZE) {
                return constraint instanceof EqualSizeConstraint equalSize
                        ? List.of(new Edge(equalSize, new Node(equalSize.getReference(), equalSize.getReferenceAxis()),
                        new Node(equalSize.getTarget(), equalSize.getTargetAxis())))
                        : List.of();
            }
            if (!constraint.holdsPosition()) {
                return List.of();
            }
            return constraint.getAxes().stream()
                    .map(axis -> new Edge(constraint, new Node(constraint.getReference(), axis), new Node(constraint.getTarget(), axis)))
                    .toList();
        }
    }

    // The constraints to flip to add a constraint, or why it can not be added
    private record Plan(Set<Constraint> flips, AxisConflict conflict) {
        static Plan refused(Conflict conflict, Axis axis) {
            return new Plan(Set.of(), new AxisConflict(conflict, axis));
        }
    }

    // Positions measured along differently rotated axes move an entity along each other, so they are solved again until they settle
    private static final int MAX_POSITION_PASSES = 10;

    private static final Set<EventType> GEOMETRY_EVENTS = EnumSet.of(EventType.MOVED, EventType.RESIZED,
            EventType.ROTATED, EventType.PATH_CHANGED, EventType.SETTINGS_CHANGED, EventType.CHILD_ADDED,
            EventType.CHILDREN_ADDED);

    private final EntityGroup root;
    private final List<Constraint> constraints = new CopyOnWriteArrayList<>();
    private final Set<ConstraintListener> listeners = new CopyOnWriteArraySet<>();
    private Constraint selectedConstraint;
    private Supplier<Collection<Entity>> heldEntities = List::of;
    private boolean solving;

    public ConstraintManager(EntityGroup root) {
        this.root = root;
        root.addListener(this::onEntityEvent);
    }

    /**
     * Every constraint, including those that are inactive because an entity was removed.
     */
    public List<Constraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    /**
     * The constraints whose entities are all in the design and whose points are still on them.
     */
    public List<Constraint> getActiveConstraints() {
        if (constraints.isEmpty()) {
            return List.of();
        }
        Set<Entity> entities = entitiesInDesign();
        return constraints.stream()
                .filter(constraint -> isActive(constraint, entities))
                .toList();
    }

    /**
     * Whether the constraint could be added, see {@link #validate}.
     */
    public boolean canAdd(Constraint constraint) {
        return validate(constraint).isEmpty();
    }

    /**
     * Why the constraint could not be added, if it could not: it may not join two entities that
     * are already joined on one of its axes, whichever way the constraints between them go. A
     * size constraint needs a size to hold, so its points must be apart along the axis, and the
     * entity's size may not already be held on that axis, by another size constraint or through
     * equal sizes. An equal size may not join sizes that are already joined.
     */
    public Optional<Conflict> validate(Constraint constraint) {
        return Optional.ofNullable(plan(constraint).conflict()).map(AxisConflict::conflict);
    }

    /**
     * Explains to the user why the constraint could not be added, if it could not, see
     * {@link #validate}.
     */
    public Optional<String> conflictMessage(Constraint constraint) {
        return Optional.ofNullable(plan(constraint).conflict()).map(found -> found.conflict().message(found.axis()));
    }

    /**
     * Adds the constraint and moves its target, and whatever is constrained to it, to satisfy
     * it. Constraints are flipped as needed for the target to be driven by it, see the class
     * description.
     *
     * @return the constraints that were flipped, to flip back with {@link #flip} when the
     * constraint is removed again by undo
     * @throws IllegalArgumentException if the constraint can not be added, see {@link #validate}
     */
    public List<Constraint> add(Constraint constraint) {
        Plan plan = plan(constraint);
        if (plan.conflict() != null) {
            throw new IllegalArgumentException("The constraint conflicts with the existing constraints");
        }
        plan.flips().forEach(Constraint::flip);
        constraints.add(constraint);
        solve();
        notifyListeners();
        return plan.flips().stream().filter(flipped -> flipped != constraint).toList();
    }

    /**
     * Flips the constraints, swapping their reference and target, as when undoing an
     * {@link #add} that flipped them. The entities stay where they are.
     */
    public void flip(Collection<Constraint> flipped) {
        if (!flipped.isEmpty()) {
            flipped.forEach(Constraint::flip);
            notifyListeners();
        }
    }

    private Plan plan(Constraint constraint) {
        List<Constraint> active = getActiveConstraints();
        return switch (constraint) {
            case DistanceConstraint size when size.isSize() -> planSize(size, active);
            case EqualSizeConstraint equalSize -> planEqualSize(equalSize, active);
            default -> planLinks(Layer.POSITION, constraint, active, false);
        };
    }

    // The constraints from the size to the root of its tree are flipped, so the size can be held
    private static Plan planSize(DistanceConstraint constraint, List<Constraint> active) {
        if (!constraint.isLocatable() || Math.abs(constraint.measure()) < Constraint.MINIMUM_SIZE) {
            return Plan.refused(Conflict.NO_SIZE, constraint.getAxis());
        }
        Set<Constraint> flips = newIdentitySet();
        flips.addAll(pathToRoot(new Node(constraint.getTarget(), constraint.getAxis()), drivers(Layer.SIZE, active)));
        return checked(Layer.SIZE, constraint, active, flips);
    }

    // An equal size is flipped itself when only its reference can take a size from it
    private static Plan planEqualSize(EqualSizeConstraint constraint, List<Constraint> active) {
        if (EqualSizeConstraint.sizeOf(constraint.getReference(), constraint.getReferenceAxis()) < Constraint.MINIMUM_SIZE) {
            return Plan.refused(Conflict.EMPTY_SHAPE, constraint.getReferenceAxis());
        }
        if (EqualSizeConstraint.sizeOf(constraint.getTarget(), constraint.getTargetAxis()) < Constraint.MINIMUM_SIZE) {
            return Plan.refused(Conflict.EMPTY_SHAPE, constraint.getTargetAxis());
        }
        Plan plan = planLinks(Layer.SIZE, constraint, active, false);
        if (plan.conflict() == null || plan.conflict().conflict() == Conflict.SIZE_LINKED) {
            return plan;
        }
        Plan flipped = planLinks(Layer.SIZE, constraint, active, true);
        return flipped.conflict() == null ? flipped : plan;
    }

    // Each node the constraint drives becomes the root of its tree by flipping the constraints up to the root
    private static Plan planLinks(Layer layer, Constraint constraint, List<Constraint> active, boolean flipped) {
        Map<Node, Edge> drivers = drivers(layer, active);
        Set<Constraint> flips = newIdentitySet();
        if (flipped) {
            flips.add(constraint);
        }
        for (Edge edge : layer.edges(constraint)) {
            Edge link = flipped ? edge.flipped() : edge;
            if (root(link.parent(), drivers).equals(root(link.child(), drivers))) {
                return Plan.refused(layer.joined, link.child().axis());
            }
            flips.addAll(pathToRoot(link.child(), drivers));
        }
        return checked(layer, constraint, active, flips);
    }

    // A constraint flipped to free a node of one axis is flipped on the other axis too, which is checked here
    private static Plan checked(Layer layer, Constraint constraint, List<Constraint> active,
                                Set<Constraint> flips) {
        List<Constraint> after = new ArrayList<>(active);
        after.add(constraint);
        return conflictingNode(layer, after, flips)
                .map(node -> Plan.refused(layer.driven, node.axis()))
                .orElseGet(() -> new Plan(flips, null));
    }

    private static <T> Set<T> newIdentitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static List<Constraint> pathToRoot(Node node, Map<Node, Edge> drivers) {
        return ancestry(node, drivers).stream().map(Edge::constraint).toList();
    }

    private static Node root(Node node, Map<Node, Edge> drivers) {
        List<Edge> ancestry = ancestry(node, drivers);
        return ancestry.isEmpty() ? node : ancestry.getLast().parent();
    }

    // The edges from the node up to the root of its tree, stopping should they loop
    private static List<Edge> ancestry(Node node, Map<Node, Edge> drivers) {
        List<Edge> ancestry = new ArrayList<>();
        for (Edge driver = drivers.get(node); driver != null && ancestry.size() <= drivers.size(); driver = drivers.get(driver.parent())) {
            ancestry.add(driver);
        }
        return ancestry;
    }

    // A node driven twice or in a loop, if any. For sizes, a node whose size is held counts as driven by that.
    private static Optional<Node> conflictingNode(Layer layer, List<Constraint> constraints, Set<Constraint> flips) {
        Map<Node, Node> parents = new HashMap<>();
        Set<Node> sized = new HashSet<>();
        for (Constraint constraint : constraints) {
            Optional<Node> size = heldSize(constraint);
            if (layer == Layer.SIZE && size.isPresent() && !sized.add(size.get())) {
                return size;
            }
            for (Edge edge : layer.edges(constraint)) {
                Edge link = flips.contains(constraint) ? edge.flipped() : edge;
                if (parents.put(link.child(), link.parent()) != null) {
                    return Optional.of(link.child());
                }
            }
        }
        Optional<Node> drivenSize = sized.stream().filter(parents::containsKey).findFirst();
        if (drivenSize.isPresent()) {
            return drivenSize;
        }
        for (Node node : parents.keySet()) {
            Node current = node;
            for (int steps = 0; current != null; steps++) {
                if (steps > parents.size()) {
                    return Optional.of(node);
                }
                current = parents.get(current);
            }
        }
        return Optional.empty();
    }

    public void remove(Constraint constraint) {
        if (constraints.remove(constraint)) {
            if (selectedConstraint == constraint) {
                selectedConstraint = null;
            }
            notifyListeners();
        }
    }

    /**
     * Changes the distance a constraint holds and moves its target, and whatever follows it,
     * accordingly.
     */
    public void setDistance(DistanceConstraint constraint, double distance) {
        constraint.setDistance(distance);
        solve();
        notifyListeners();
    }

    /**
     * Moves where the dimension of a constraint is drawn, which does not affect any entity.
     */
    public void setOffset(DistanceConstraint constraint, double offset) {
        constraint.setOffset(offset);
        notifyListeners();
    }

    public Optional<Constraint> getSelectedConstraint() {
        return Optional.ofNullable(selectedConstraint);
    }

    /**
     * Selects a constraint, or clears the selection with null.
     */
    public void setSelectedConstraint(Constraint constraint) {
        if (selectedConstraint != constraint) {
            selectedConstraint = constraint;
            notifyListeners();
        }
    }

    /**
     * Replaces every constraint, as when a design is loaded.
     */
    public void setConstraints(List<Constraint> newConstraints) {
        selectedConstraint = null;
        constraints.clear();
        constraints.addAll(newConstraints);
        solve();
        notifyListeners();
    }

    public void clear() {
        selectedConstraint = null;
        if (!constraints.isEmpty()) {
            constraints.clear();
            notifyListeners();
        }
    }

    /**
     * Sets the entities constraints never move, such as the selected entities while they are
     * being dragged.
     */
    public void setHeldEntities(Supplier<Collection<Entity>> heldEntities) {
        this.heldEntities = heldEntities;
    }

    /**
     * Applies every active constraint, the references staying where they are and the targets
     * following them.
     */
    public void solve() {
        solve(Set.of());
    }

    private void solve(Collection<Entity> fixedEntities) {
        if (solving) {
            return;
        }
        solving = true;
        try {
            List<Constraint> active = getActiveConstraints();
            active.stream()
                    .filter(PointConstraint.class::isInstance)
                    .map(PointConstraint.class::cast)
                    .forEach(PointConstraint::followEntities);
            active.stream().filter(Constraint::isSize).forEach(Constraint::apply);
            List<Node> sizeSeeds = new ArrayList<>(active.stream()
                    .map(ConstraintManager::heldSize)
                    .flatMap(Optional::stream)
                    .toList());
            List<Node> fixedNodes = nodesOf(fixedEntities);
            sizeSeeds.addAll(fixedNodes);
            solveLayer(linksOf(Layer.SIZE, active), List.of(), sizeSeeds);
            Links positions = linksOf(Layer.POSITION, active);
            boolean moved = true;
            for (int pass = 0; moved && pass < MAX_POSITION_PASSES; pass++) {
                moved = solveLayer(positions, fixedNodes, List.of());
            }
        } finally {
            solving = false;
        }
    }

    // The size of the entity a size constraint holds
    private static Optional<Node> heldSize(Constraint constraint) {
        return constraint instanceof DistanceConstraint size && size.isSize()
                ? Optional.of(new Node(size.getTarget(), size.getAxis()))
                : Optional.empty();
    }

    private static List<Node> nodesOf(Collection<Entity> entities) {
        return entities.stream()
                .flatMap(entity -> Arrays.stream(Axis.values()).map(axis -> new Node(entity, axis)))
                .toList();
    }

    // Places the nodes outwards from the held ones, each one by the link joining it to a node already
    // in place, then outwards from each seed in turn and the trees left from their root. A link
    // between two held nodes is left as it is. Sizes are seeded by the held sizes first, so they win.
    // Returns whether any entity was moved
    private static boolean solveLayer(Links links, Collection<Node> held, List<Node> seeds) {
        Map<Node, List<Edge>> edgesOf = links.edgesOf();
        if (edgesOf.isEmpty()) {
            return false;
        }

        Set<Node> placed = new HashSet<>();
        Deque<Node> queue = new ArrayDeque<>();
        held.stream().filter(edgesOf::containsKey).forEach(node -> {
            placed.add(node);
            queue.add(node);
        });
        boolean moved = spread(queue, placed, edgesOf);

        List<Node> starts = new ArrayList<>(seeds);
        starts.addAll(links.roots());
        for (Node start : starts) {
            if (edgesOf.containsKey(start) && placed.add(start)) {
                queue.add(start);
                moved |= spread(queue, placed, edgesOf);
            }
        }
        return moved;
    }

    private static boolean spread(Deque<Node> queue, Set<Node> placed, Map<Node, List<Edge>> edgesOf) {
        boolean moved = false;
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            for (Edge edge : edgesOf.get(node)) {
                Node other = edge.other(node);
                if (placed.add(other)) {
                    moved |= edge.applyTo(other);
                    queue.add(other);
                }
            }
        }
        return moved;
    }

    public void addListener(ConstraintListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ConstraintListener listener) {
        listeners.remove(listener);
    }

    private void onEntityEvent(EntityEvent event) {
        Entity entity = event.getTarget();
        if (!solving && GEOMETRY_EVENTS.contains(event.getType()) && affectsConstraints(entity)) {
            List<Entity> fixed = new ArrayList<>(heldEntities.get());
            if (!(entity instanceof EntityGroup)) {
                fixed.add(entity);
            }
            solve(fixed);
        }
    }

    // A group's event, such as the root's after flipping the selection, may stand for changes of its children
    private boolean affectsConstraints(Entity entity) {
        return !constraints.isEmpty()
                && (entity instanceof EntityGroup || constraints.stream().anyMatch(constraint -> constraint.involves(entity)));
    }

    // The links of a layer at each node, and the root of each tree
    private record Links(Map<Node, List<Edge>> edgesOf, List<Node> roots) {
    }

    private static Links linksOf(Layer layer, List<Constraint> constraints) {
        Map<Node, Edge> drivers = drivers(layer, constraints);
        Map<Node, List<Edge>> edgesOf = new HashMap<>();
        drivers.values().forEach(edge -> {
            edgesOf.computeIfAbsent(edge.parent(), _ -> new ArrayList<>()).add(edge);
            edgesOf.computeIfAbsent(edge.child(), _ -> new ArrayList<>()).add(edge);
        });
        List<Node> roots = edgesOf.keySet().stream().filter(node -> !drivers.containsKey(node)).toList();
        return new Links(edgesOf, roots);
    }

    private static Map<Node, Edge> drivers(Layer layer, List<Constraint> constraints) {
        Map<Node, Edge> drivers = new HashMap<>();
        constraints.forEach(constraint -> layer.edges(constraint).forEach(edge -> drivers.put(edge.child(), edge)));
        return drivers;
    }

    private static boolean isActive(Constraint constraint, Set<Entity> entities) {
        return entities.contains(constraint.getReference()) && entities.contains(constraint.getTarget())
                && constraint.isLocatable();
    }

    private Set<Entity> entitiesInDesign() {
        Set<Entity> entities = newIdentitySet();
        entities.addAll(root.getAllChildren());
        return entities;
    }

    private void notifyListeners() {
        listeners.forEach(ConstraintListener::onConstraintsChanged);
    }
}
