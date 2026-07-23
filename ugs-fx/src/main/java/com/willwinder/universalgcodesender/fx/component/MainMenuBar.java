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
package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A menu bar that is automatically populated from the registered {@link Action}s. Actions opt in by
 * setting {@code menuVisible} and are grouped into top level menus by their category. Within a menu
 * the items are ordered by their menu order; the top level menus themselves are ordered by the
 * lowest menu order found in each category.
 *
 * <p>On macOS the menu is rendered in the native system menu bar at the top of the screen, on other
 * platforms it is shown in-window at the top.
 */
public class MainMenuBar extends MenuBar {

    private static final int ICON_SIZE = 16;

    /**
     * Items are grouped into visually separated sections by the hundreds digit of their menu
     * order, so {@code 100, 110, 120} form one section and {@code 200} starts the next.
     */
    private static final int MENU_SECTION_SIZE = 100;

    public MainMenuBar() {
        setUseSystemMenuBar(true);
        build();
    }

    private void build() {
        Map<String, List<Action>> actionsByCategory = ActionRegistry.getInstance().getActions().stream()
                .filter(Action::isMenuVisible)
                .filter(action -> action.getCategory() != null && !action.getCategory().isBlank())
                .collect(Collectors.groupingBy(Action::getCategory));

        actionsByCategory.entrySet().stream()
                .sorted(Comparator
                        .comparingInt((Map.Entry<String, List<Action>> entry) -> lowestOrder(entry.getValue()))
                        .thenComparing(Map.Entry::getKey))
                .map(entry -> createMenu(entry.getKey(), entry.getValue()))
                .forEach(getMenus()::add);
    }

    private int lowestOrder(List<Action> actions) {
        return actions.stream().mapToInt(Action::getMenuOrder).min().orElse(Integer.MAX_VALUE);
    }

    private Menu createMenu(String category, List<Action> actions) {
        Menu menu = new Menu(category);
        List<Action> sortedActions = actions.stream()
                .sorted(Comparator.comparingInt(Action::getMenuOrder).thenComparing(Action::getTitle))
                .toList();

        Integer previousSection = null;
        for (Action action : sortedActions) {
            int section = action.getMenuOrder() / MENU_SECTION_SIZE;
            if (previousSection != null && section != previousSection) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            menu.getItems().add(createMenuItem(action));
            previousSection = section;
        }
        return menu;
    }

    private MenuItem createMenuItem(Action action) {
        MenuItem menuItem = new MenuItem();
        menuItem.textProperty().bind(action.titleProperty());
        menuItem.disableProperty().bind(action.enabledProperty().not());
        setIcon(menuItem, action.getIcon());
        action.iconProperty().addListener((observable, oldValue, newValue) -> setIcon(menuItem, newValue));

        // Use the menu bar as the event source so actions that resolve the owning window from the
        // event (open, save, settings) keep working when triggered from the menu.
        menuItem.setOnAction(event -> action.handle(new ActionEvent(this, null)));
        return menuItem;
    }

    private void setIcon(MenuItem menuItem, String icon) {
        if (icon == null || icon.isBlank()) {
            menuItem.setGraphic(null);
            return;
        }
        SvgLoader.loadImageIcon(icon, ICON_SIZE)
                .ifPresent(menuItem::setGraphic);
    }
}
