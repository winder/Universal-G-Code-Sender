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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.actions.ReorderEntitiesAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.fx.component.designer.EntityTreeDrop.Position;
import com.willwinder.universalgcodesender.fx.component.designer.EntityTreeDrop.Target;
import javafx.css.PseudoClass;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Lets the rows of an {@link EntityTreeView} be dragged to reorder the entities or to put them into
 * a group. Dragging a selected row takes the whole selection along. The rows show where the drop
 * would land through the {@code drop-before}, {@code drop-into} and {@code drop-after} pseudo
 * classes, and dropping on the empty space below the rows appends to the end of the design.
 */
class EntityTreeDragSupport {

    private static final DataFormat ENTITIES = new DataFormat("application/x-ugs-design-entities");
    private static final PseudoClass DROP_BEFORE = PseudoClass.getPseudoClass("drop-before");
    private static final PseudoClass DROP_INTO = PseudoClass.getPseudoClass("drop-into");
    private static final PseudoClass DROP_AFTER = PseudoClass.getPseudoClass("drop-after");

    private final EntityTreeView tree;
    private final Controller controller;

    // The entities of the drag in progress, in tree order. Objects can not travel on the dragboard.
    private List<Entity> dragged = List.of();

    EntityTreeDragSupport(EntityTreeView tree, Controller controller) {
        this.tree = tree;
        this.controller = controller;
        tree.setOnDragOver(this::onDragOverEmptySpace);
        tree.setOnDragDropped(this::onDropInEmptySpace);
    }

    void install(EntityTreeCell cell) {
        cell.setOnDragDetected(event -> startDrag(cell, event));
        cell.setOnDragOver(event -> onDragOver(cell, event));
        cell.setOnDragExited(_ -> clearHighlight(cell));
        cell.setOnDragDropped(event -> onDrop(cell, event));
        cell.setOnDragDone(_ -> dragged = List.of());
    }

    private void startDrag(EntityTreeCell cell, MouseEvent event) {
        if (cell.isEmpty() || cell.getItem() == null) {
            return;
        }

        dragged = entitiesToDrag(cell);
        Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(ENTITIES, dragged.size() + " entities");
        dragboard.setContent(content);
        dragboard.setDragView(cell.snapshot(null, null));
        event.consume();
    }

    /**
     * A selected row drags the whole selection; entities whose ancestor is selected too are left
     * out as they travel with that ancestor.
     */
    private List<Entity> entitiesToDrag(EntityTreeCell cell) {
        if (!cell.isSelected()) {
            return List.of(cell.getItem());
        }
        List<TreeItem<Entity>> selected = tree.getSelectionModel().getSelectedItems().stream()
                .filter(item -> item != null && item.getValue() != null)
                .toList();
        return selected.stream()
                .filter(item -> !hasAncestorIn(item, selected))
                .sorted(Comparator.comparingInt(tree::getRow))
                .map(TreeItem::getValue)
                .toList();
    }

    private static boolean hasAncestorIn(TreeItem<Entity> item, List<TreeItem<Entity>> items) {
        for (TreeItem<Entity> parent = item.getParent(); parent != null; parent = parent.getParent()) {
            if (items.contains(parent)) {
                return true;
            }
        }
        return false;
    }

    private void onDragOver(EntityTreeCell cell, DragEvent event) {
        if (!isEntityDrag(event) || cell.isEmpty() || cell.getItem() == null) {
            return;
        }

        Position position = positionIn(cell, event);
        Optional<Target> target = targetFor(cell, position);
        if (target.isPresent()) {
            event.acceptTransferModes(TransferMode.MOVE);
            highlight(cell, position);
        } else {
            clearHighlight(cell);
        }
        event.consume();
    }

    private void onDrop(EntityTreeCell cell, DragEvent event) {
        if (!isEntityDrag(event) || cell.isEmpty() || cell.getItem() == null) {
            return;
        }

        Optional<Target> target = targetFor(cell, positionIn(cell, event));
        target.ifPresent(this::move);
        clearHighlight(cell);
        event.setDropCompleted(target.isPresent());
        event.consume();
    }

    private void onDragOverEmptySpace(DragEvent event) {
        if (isEntityDrag(event) && EntityTreeDrop.canDrop(dragged, endOfDesign())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void onDropInEmptySpace(DragEvent event) {
        if (!isEntityDrag(event)) {
            return;
        }
        Target target = endOfDesign();
        boolean accepted = EntityTreeDrop.canDrop(dragged, target);
        if (accepted) {
            move(target);
        }
        event.setDropCompleted(accepted);
        event.consume();
    }

    private Target endOfDesign() {
        EntityGroup root = controller.getModel().getRootEntity();
        return new Target(root, root.getChildren().size());
    }

    private boolean isEntityDrag(DragEvent event) {
        return event.getDragboard().hasContent(ENTITIES) && !dragged.isEmpty();
    }

    private static Position positionIn(EntityTreeCell cell, DragEvent event) {
        return EntityTreeDrop.positionFor(cell.getItem() instanceof EntityGroup, event.getY(), cell.getHeight());
    }

    private Optional<Target> targetFor(EntityTreeCell cell, Position position) {
        Entity entity = cell.getItem();
        TreeItem<Entity> parentItem = cell.getTreeItem() != null ? cell.getTreeItem().getParent() : null;
        if (dragged.contains(entity) || parentItem == null || !(parentItem.getValue() instanceof EntityGroup parent)) {
            return Optional.empty();
        }

        Target target = EntityTreeDrop.resolve(position, entity, parent);
        return EntityTreeDrop.canDrop(dragged, target) ? Optional.of(target) : Optional.empty();
    }

    private void move(Target target) {
        List<Entity> entities = dragged;
        ReorderEntitiesAction action = new ReorderEntitiesAction(
                controller.getModel().getRootEntity(), entities, target.parent(), target.index());
        controller.getUndoManager().addAction(action);
        action.redo();
        controller.getSelectionManager().setSelection(entities);
    }

    private static void highlight(EntityTreeCell cell, Position position) {
        cell.pseudoClassStateChanged(DROP_BEFORE, position == Position.BEFORE);
        cell.pseudoClassStateChanged(DROP_INTO, position == Position.INTO);
        cell.pseudoClassStateChanged(DROP_AFTER, position == Position.AFTER);
    }

    private static void clearHighlight(EntityTreeCell cell) {
        cell.pseudoClassStateChanged(DROP_BEFORE, false);
        cell.pseudoClassStateChanged(DROP_INTO, false);
        cell.pseudoClassStateChanged(DROP_AFTER, false);
    }
}
