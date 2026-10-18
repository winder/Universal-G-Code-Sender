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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shows an entity as an {@link EntityTreeRow}, the same information as the cell renderer of the
 * classic designer tree. A path that is not closed also gets a tooltip, as it cannot be pocketed
 * or cut inside/outside. The tree's disclosure arrow is hidden by the stylesheet; the row's folder
 * icon takes its place for groups and follows the expanded state of the tree item.
 */
class EntityTreeCell extends TreeCell<Entity> {

    private final Supplier<UnitUtils.Units> preferredUnits;
    private final EntityTreeRow row = new EntityTreeRow(this::toggleExpanded);
    private final Tooltip openPathTooltip = new Tooltip(Localization.getString("platform.plugin.designer.tree.open-path.tooltip"));
    private final ChangeListener<Boolean> expandedListener = (_, _, _) -> refreshRow();

    EntityTreeCell(Supplier<UnitUtils.Units> preferredUnits, Consumer<Entity> onDoubleClick) {
        this.preferredUnits = preferredUnits;
        setText(null);
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !isEmpty() && getItem() != null) {
                onDoubleClick.accept(getItem());
            }
        });
        treeItemProperty().addListener((_, previous, current) -> {
            if (previous != null) {
                previous.expandedProperty().removeListener(expandedListener);
            }
            if (current != null) {
                current.expandedProperty().addListener(expandedListener);
            }
        });
    }

    private void toggleExpanded() {
        TreeItem<Entity> item = getTreeItem();
        if (item != null) {
            item.setExpanded(!item.isExpanded());
        }
    }

    private void refreshRow() {
        if (!isEmpty() && getItem() != null) {
            row.update(getItem(), preferredUnits.get(), isExpanded());
        }
    }

    private boolean isExpanded() {
        return getTreeItem() != null && getTreeItem().isExpanded();
    }

    @Override
    protected void updateItem(Entity entity, boolean empty) {
        super.updateItem(entity, empty);
        if (empty || entity == null) {
            setGraphic(null);
            setTooltip(null);
            return;
        }

        row.update(entity, preferredUnits.get(), isExpanded());
        setGraphic(row);
        setTooltip(EntityTreeText.isOpenPath(entity) ? openPathTooltip : null);
    }
}
