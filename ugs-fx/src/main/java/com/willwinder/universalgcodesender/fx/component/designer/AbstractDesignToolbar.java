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
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;

import java.util.Optional;

/**
 * Base class for the designer toolbars that lay out a set of registered actions as flowing icon
 * buttons. Subclasses add the actions they want to expose in their constructor using
 * {@link #addButton(Class)} or {@link #addToggleButton(Class)}.
 */
abstract class AbstractDesignToolbar extends FlowPane {
    protected static final int ICON_SIZE = 24;

    protected AbstractDesignToolbar(String styleClass) {
        getStyleClass().addAll("design-toolbar", styleClass);
        setHgap(4);
        setVgap(4);
        setPadding(new Insets(8));
    }

    protected void addToggleButton(Class<? extends Action> actionClass) {
        getAction(actionClass).ifPresent(action ->
                getChildren().add(new ToggleActionButton(action, ICON_SIZE, false)));
    }

    protected void addButton(Class<? extends Action> actionClass) {
        getAction(actionClass).ifPresent(action ->
                getChildren().add(new ActionButton(action, ICON_SIZE, false)));
    }

    private Optional<Action> getAction(Class<? extends Action> actionClass) {
        return ActionRegistry.getInstance().getAction(actionClass.getCanonicalName());
    }
}
