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
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.SelectionMode;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
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
 * {@link SelectionManager}.
 */
public class EntityTreeView extends TreeView<Entity> {

    private static final int ICON_SIZE = 16;

    // Entity-property events that should refresh the cell labels/icons without rebuilding.
    private static final EnumSet<EventType> REFRESH_EVENTS = EnumSet.of(
            EventType.SETTINGS_CHANGED, EventType.HIDDEN);

    private final Controller controller;
    private final SelectionManager selectionManager;
    private final EntityListener rootListener;
    private final SelectionListener selectionListener;

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
        setCellFactory(tv -> new EntityTreeCell());

        rebuild();

        this.rootListener = event -> Platform.runLater(() -> onEntityEvent(event.getType()));
        controller.getDrawing().getRootEntity().addListener(rootListener);

        controller.addListener(event -> {
            if (event == ControllerEventType.NEW_DRAWING) {
                Platform.runLater(this::rebuild);
            }
        });

        this.selectionListener = e -> Platform.runLater(this::syncSelectionFromManager);
        selectionManager.addSelectionListener(selectionListener);

        getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<TreeItem<Entity>>) c -> syncSelectionToManager());
    }

    private void onEntityEvent(EventType type) {
        if (type == EventType.CHILD_ADDED || type == EventType.CHILD_REMOVED) {
            rebuild();
        } else if (REFRESH_EVENTS.contains(type)) {
            refresh();
        }
    }

    private void rebuild() {
        Set<Entity> expanded = collectExpandedEntities();

        itemByEntity.clear();
        EntityGroup root = controller.getDrawing().getRootEntity();
        TreeItem<Entity> rootItem = buildItem(root, expanded, true);
        setRoot(rootItem);

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

    private void syncSelectionToManager() {
        if (updatingSelection) {
            return;
        }

        List<Entity> selected = new ArrayList<>();
        getSelectionModel().getSelectedItems().forEach(item -> {
            if (item != null && item.getValue() != null) {
                selected.add(item.getValue());
            }
        });

        // The selection lives in the Swing-based designer model; mutate it on the EDT.
        SwingUtilities.invokeLater(() -> selectionManager.setSelection(selected));
    }

    private void syncSelectionFromManager() {
        Set<Entity> desired = new HashSet<>(selectionManager.getSelection());

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

    /**
     * Renders an entity as an icon plus its name.
     */
    private static class EntityTreeCell extends TreeCell<Entity> {
        @Override
        protected void updateItem(Entity entity, boolean empty) {
            super.updateItem(entity, empty);
            if (empty || entity == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(entity.getName());
            setGraphic(SvgLoader.loadImageIcon(iconFor(entity), ICON_SIZE).orElse(null));
        }

        private String iconFor(Entity entity) {
            if (entity instanceof EntityGroup) {
                return "icons/group.svg";
            }
            if (entity instanceof Cuttable cuttable && cuttable.isHidden()) {
                return "icons/clear.svg";
            }
            if (entity instanceof Ellipse) {
                return "icons/circle.svg";
            }
            if (entity instanceof Rectangle) {
                return "icons/rectangle.svg";
            }
            if (entity instanceof Text) {
                return "icons/text.svg";
            }
            if (entity instanceof Point) {
                return "icons/position.svg";
            }
            if (entity instanceof Raster) {
                return "icons/clipart.svg";
            }
            if (entity instanceof Path) {
                return "icons/lines.svg";
            }
            return "icons/cube.svg";
        }
    }
}
