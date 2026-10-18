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
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One row of the {@link EntityTreeView}: an icon beside the entity's name and a detail line. Every
 * entity type fills the same slots, so all rows get the same height. A group shows a folder that
 * is open or closed with the row, and clicking it toggles the group.
 */
class EntityTreeRow extends HBox {

    private static final int ICON_SIZE = 16;
    private static final int OPEN_PATH_ICON_SIZE = 12;
    private static final String GROUP_CLOSED_ICON = "icons/group.svg";
    private static final String GROUP_OPEN_ICON = "icons/folder-open.svg";
    private static final String HIDDEN_ICON = "icons/eye-slash.svg";
    private static final String OPEN_PATH_ICON = "img/open-path.svg";
    private static final String FALLBACK_ICON = "icons/cube.svg";

    private static final Map<String, Optional<Image>> ICON_CACHE = new HashMap<>();

    private final StackPane iconSlot = new StackPane();
    private final Label name = new Label();
    private final Label detail = new Label();
    private boolean group;

    EntityTreeRow(Runnable onGroupIconClicked) {
        super(6);
        setAlignment(Pos.CENTER_LEFT);
        iconSlot.setMinSize(ICON_SIZE, ICON_SIZE);
        iconSlot.setPrefSize(ICON_SIZE, ICON_SIZE);
        iconSlot.setMaxSize(ICON_SIZE, ICON_SIZE);
        iconSlot.setOnMouseClicked(event -> {
            if (group && event.getClickCount() == 1) {
                onGroupIconClicked.run();
            }
            if (group) {
                event.consume();
            }
        });
        name.setContentDisplay(ContentDisplay.RIGHT);
        name.setGraphicTextGap(6);
        detail.getStyleClass().add("entity-tree-detail");
        getChildren().addAll(iconSlot, new VBox(name, detail));
    }

    void update(Entity entity, UnitUtils.Units units, boolean expanded) {
        group = entity instanceof EntityGroup;
        iconSlot.getChildren().setAll(icon(entity, expanded).stream().toList());
        name.setText(entity.getName());
        name.setGraphic(EntityTreeText.isOpenPath(entity) ? tintedIcon(OPEN_PATH_ICON, OPEN_PATH_ICON_SIZE).orElse(null) : null);
        detail.setText(EntityTreeText.detail(entity, units));
    }

    private static Optional<ImageView> icon(Entity entity, boolean expanded) {
        if (entity instanceof EntityGroup) {
            return tintedIcon(expanded ? GROUP_OPEN_ICON : GROUP_CLOSED_ICON, ICON_SIZE);
        }
        if (entity instanceof Cuttable cuttable) {
            return cuttable.isHidden()
                    ? tintedIcon(HIDDEN_ICON, ICON_SIZE)
                    : EntitySettingIcons.cutType(cuttable.getCutType());
        }
        return tintedIcon(FALLBACK_ICON, ICON_SIZE);
    }

    private static Optional<ImageView> tintedIcon(String resource, int size) {
        return ICON_CACHE.computeIfAbsent(resource + "@" + size, _ -> SvgLoader.loadIcon(resource, size))
                .map(image -> SvgLoader.tinted(image, Colors.BLACKISH));
    }
}
