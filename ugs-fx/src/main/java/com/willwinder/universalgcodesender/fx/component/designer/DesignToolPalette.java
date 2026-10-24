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

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.DesignCircleAction;
import com.willwinder.universalgcodesender.fx.actions.DesignClipartAction;
import com.willwinder.universalgcodesender.fx.actions.DesignImportAction;
import com.willwinder.universalgcodesender.fx.actions.DesignLineAction;
import com.willwinder.universalgcodesender.fx.actions.DesignPointAction;
import com.willwinder.universalgcodesender.fx.actions.DesignRectangleAction;
import com.willwinder.universalgcodesender.fx.actions.DesignSelectAction;
import com.willwinder.universalgcodesender.fx.actions.DesignTextAction;
import com.willwinder.universalgcodesender.fx.actions.DesignTraceImageAction;
import com.willwinder.universalgcodesender.fx.actions.DesignVertexAction;
import com.willwinder.universalgcodesender.fx.control.ActionMenuItem;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.Pos;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The drawing tools, floating along the left edge of the visualizer while a design workspace is
 * open: the select and vertex tools, the shape tools and an insert menu for files, clipart and
 * traced images. The tool buttons stay highlighted while their tool is active in the designer.
 */
public class DesignToolPalette extends VBox {
    private static final double SPACING = 2;
    private final int iconSize;

    /**
     * @param iconSize the icon size of the other toolbars floating over the visualizer, so the
     *                 palette's buttons match theirs
     */
    public DesignToolPalette(int iconSize) {
        super(SPACING);
        this.iconSize = iconSize;
        getStyleClass().add("design-tool-palette");
        setAlignment(Pos.CENTER);

        addTools(List.of(DesignSelectAction.class, DesignVertexAction.class));
        addDivider();
        addTools(List.of(DesignRectangleAction.class, DesignCircleAction.class, DesignPointAction.class,
                DesignTextAction.class, DesignLineAction.class));
        addDivider();
        getChildren().add(createInsertMenu());

        DesignWorkspaceVisibility.bind(this);
    }

    private void addTools(List<Class<? extends Action>> actionClasses) {
        actionClasses.stream()
                .map(DesignToolPalette::action)
                .flatMap(Optional::stream)
                .forEach(action -> getChildren().add(new ToggleActionButton(action, iconSize, false)));
    }

    private void addDivider() {
        getChildren().add(new Separator());
    }

    private MenuButton createInsertMenu() {
        MenuButton menu = new MenuButton();
        menu.getStyleClass().add("insert-menu");
        SvgLoader.loadImageIcon("icons/plus.svg", iconSize, Colors.BLACKISH).ifPresent(menu::setGraphic);
        Tooltip tooltip = new Tooltip(Localization.getString("platform.designer.insert"));
        tooltip.setShowDelay(Duration.millis(100));
        menu.setTooltip(tooltip);
        Stream.of(DesignImportAction.class, DesignClipartAction.class, DesignTraceImageAction.class)
                .map(DesignToolPalette::action)
                .flatMap(Optional::stream)
                .forEach(action -> menu.getItems().add(new ActionMenuItem(action)));
        return menu;
    }

    private static Optional<Action> action(Class<? extends Action> actionClass) {
        return ActionRegistry.getInstance().getAction(actionClass.getCanonicalName());
    }
}
