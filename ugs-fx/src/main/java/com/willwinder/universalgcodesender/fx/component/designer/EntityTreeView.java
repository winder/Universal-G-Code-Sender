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

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.EntityListener;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.model.WorkspaceBounds;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.SelectionMode;

import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A JavaFX tree showing the entities of the active design. It mirrors the Swing
 * {@code EntitiesTree} from the designer module: the structure is built from the design's
 * root {@link EntityGroup} and selection is kept in sync, both ways, with the
 * {@link SelectionManager}. Rows can be dragged to reorder entities or to move them into a group,
 * see {@link EntityTreeDragSupport}. A right click opens the same {@link DesignContextMenu} as the
 * design in the visualizer, acting on the selected entities.
 */
public class EntityTreeView extends TreeView<Entity> {

    private static final double ZOOM_MARGIN_FACTOR = 0.1;
    private static final double MIN_ZOOM_MARGIN = 5;

    // Entity-property events that should refresh the cell labels/icons without rebuilding.
    private static final EnumSet<EventType> REFRESH_EVENTS = EnumSet.of(
            EventType.SETTINGS_CHANGED, EventType.HIDDEN, EventType.PATH_CHANGED);

    private final Controller controller;
    private final SelectionManager selectionManager;

    // Maps the live design entities to their tree nodes so we can sync the selection.
    private final Map<Entity, TreeItem<Entity>> itemByEntity = new HashMap<>();

    // Guards against the selection sync echoing back and forth between the tree and the manager.
    private boolean updatingSelection = false;

    public EntityTreeView() {
        this.controller = ControllerFactory.getController();
        this.selectionManager = controller.getSelectionManager();

        getStyleClass().add("entity-tree");
        setShowRoot(false);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setContextMenu(DesignContextMenu.create());
        EntityTreeDragSupport dragSupport = new EntityTreeDragSupport(this, controller);
        setCellFactory(_ -> {
            EntityTreeCell cell = new EntityTreeCell(() -> controller.getSettings().getPreferredUnits(), this::zoomTo);
            dragSupport.install(cell);
            return cell;
        });

        rebuild();

        EntityListener rootListener = event -> Platform.runLater(() -> onEntityEvent(event.getType()));
        controller.getModel().getRootEntity().addListener(rootListener);

        controller.addListener(event -> {
            if (event == ControllerEventType.NEW_DRAWING) {
                Platform.runLater(this::rebuild);
            }
        });

        SelectionListener selectionListener = _ -> Platform.runLater(this::syncSelectionFromManager);
        selectionManager.addSelectionListener(selectionListener);

        getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<Entity>>) _ -> syncSelectionToManager());
    }

    /**
     * Frames the entity in the visualizer with some room around it; a point gets a fixed extent
     * as its bounds have no size to fit.
     */
    private void zoomTo(Entity entity) {
        Rectangle2D bounds = entity.getBounds();
        if (bounds == null) {
            return;
        }
        double margin = Math.max(ZOOM_MARGIN_FACTOR * Math.max(bounds.getWidth(), bounds.getHeight()), MIN_ZOOM_MARGIN);
        VisualizerService.getInstance().centerOnBounds(new WorkspaceBounds(
                bounds.getMinX() - margin, bounds.getMinY() - margin,
                bounds.getMaxX() + margin, bounds.getMaxY() + margin));
    }

    private void onEntityEvent(EventType type) {
        if (type == EventType.CHILD_ADDED || type == EventType.CHILDREN_ADDED
                || type == EventType.CHILD_REMOVED || type == EventType.CHILDREN_REMOVED) {
            rebuild();
        } else if (REFRESH_EVENTS.contains(type)) {
            refresh();
        }
    }

    private void rebuild() {
        Set<Entity> expanded = collectExpandedEntities();

        itemByEntity.clear();
        EntityGroup root = controller.getModel().getRootEntity();
        TreeItem<Entity> rootItem = buildItem(root, expanded, true);

        // Replacing the root clears the tree selection, which must not clear the manager's.
        updatingSelection = true;
        try {
            setRoot(rootItem);
        } finally {
            updatingSelection = false;
        }

        syncSelectionFromManager();
    }

    private TreeItem<Entity> buildItem(Entity entity, Set<Entity> expanded, boolean isRoot) {
        TreeItem<Entity> item = new TreeItem<>(entity);
        itemByEntity.put(entity, item);

        if (entity instanceof EntityGroup group) {
            for (Entity child : group.getChildren()) {
                item.getChildren().add(buildItem(child, expanded, false));
            }
            // Default to expanded so the design is visible at a glance, but honour the
            // user's previous expansion state when we are rebuilding.
            item.setExpanded(isRoot || expanded.isEmpty() || expanded.contains(entity));
        }
        return item;
    }

    private Set<Entity> collectExpandedEntities() {
        Set<Entity> expanded = new HashSet<>();
        collectExpandedEntities(getRoot(), expanded);
        return expanded;
    }

    private void collectExpandedEntities(TreeItem<Entity> item, Set<Entity> expanded) {
        if (item == null) {
            return;
        }
        if (item.isExpanded() && item.getValue() != null) {
            expanded.add(item.getValue());
        }
        item.getChildren().forEach(child -> collectExpandedEntities(child, expanded));
    }

    /**
     * A group selected in the tree is selected as a whole, like in the platform edition. Entities
     * whose ancestor is selected too are left out, or a move would be applied to them twice.
     */
    private void syncSelectionToManager() {
        if (updatingSelection) {
            return;
        }

        List<TreeItem<Entity>> selectedItems = getSelectionModel().getSelectedItems().stream()
                .filter(item -> item != null && item.getValue() != null)
                .toList();
        List<Entity> selected = selectedItems.stream()
                .filter(item -> !hasSelectedAncestor(item, selectedItems))
                .map(TreeItem::getValue)
                .toList();
        selectionManager.setSelection(selected);
    }

    private static boolean hasSelectedAncestor(TreeItem<Entity> item, List<TreeItem<Entity>> selectedItems) {
        for (TreeItem<Entity> parent = item.getParent(); parent != null; parent = parent.getParent()) {
            if (selectedItems.contains(parent)) {
                return true;
            }
        }
        return false;
    }

    private void syncSelectionFromManager() {
        Set<Entity> desired = new HashSet<>(selectionManager.getChildren());

        // Skip if the tree already reflects the manager's selection to avoid a sync loop.
        Set<Entity> current = new HashSet<>();
        getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != null && item.getValue() != null) {
                current.add(item.getValue());
            }
        });
        if (current.equals(desired)) {
            return;
        }

        updatingSelection = true;
        try {
            getSelectionModel().clearSelection();
            desired.forEach(entity -> {
                TreeItem<Entity> item = itemByEntity.get(entity);
                if (item != null) {
                    getSelectionModel().select(item);
                }
            });
        } finally {
            updatingSelection = false;
        }
    }
}
