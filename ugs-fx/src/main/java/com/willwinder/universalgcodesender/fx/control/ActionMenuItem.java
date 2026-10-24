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
package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;

/**
 * A menu item bound to an action: its text, icon and enabled state follow the action, and
 * choosing it runs the action.
 */
public class ActionMenuItem extends MenuItem {
    private static final int ICON_SIZE = 16;

    public ActionMenuItem(Action action) {
        textProperty().bind(action.titleProperty());
        disableProperty().bind(action.enabledProperty().not());
        setOnAction(_ -> action.handle(new ActionEvent()));
        setIcon(action.getIcon());
        action.iconProperty().addListener((_, _, newValue) -> setIcon(newValue));
    }

    private void setIcon(String icon) {
        if (icon == null || icon.isBlank()) {
            setGraphic(null);
            return;
        }
        SvgLoader.loadImageIcon(icon, ICON_SIZE).ifPresent(this::setGraphic);
    }
}
