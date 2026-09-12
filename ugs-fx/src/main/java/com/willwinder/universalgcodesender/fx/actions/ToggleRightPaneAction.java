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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;

/**
 * Collapses or expands the right pane to a rail. The icon points in the direction the pane will
 * move, so the same action can be shown both in the pane header and on the collapsed rail.
 */
public class ToggleRightPaneAction extends BaseAction {

    public static final String ICON_COLLAPSE = "icons/caret-double-right.svg";
    public static final String ICON_EXPAND = "icons/caret-double-left.svg";

    public ToggleRightPaneAction() {
        super(null, Localization.getString("platform.window.toggleRightPane"),
                Localization.getString("actions.category.view"), ICON_COLLAPSE);

        BooleanProperty collapsed = Settings.getInstance().windowRightPaneCollapsedProperty();
        iconProperty().bind(Bindings.when(collapsed).then(ICON_EXPAND).otherwise(ICON_COLLAPSE));
        selectedProperty().bind(collapsed.not());
        setMenuVisible(true);
        setMenuOrder(110);
    }

    @Override
    public void handleAction(ActionEvent event) {
        BooleanProperty collapsed = Settings.getInstance().windowRightPaneCollapsedProperty();
        collapsed.set(!collapsed.get());
    }
}
