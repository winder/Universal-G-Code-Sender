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
package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

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
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;

import java.util.Optional;

/**
 * A toolbox of drawing actions (rectangle, ellipse, point, text, line) and import actions
 * (import file, clipart, trace image) laid out as flowing buttons. Tool actions are rendered
 * as toggle buttons that stay highlighted while the matching tool is active in the designer.
 */
public class DesignToolbar extends FlowPane {
    private static final int ICON_SIZE = 24;

    public DesignToolbar() {
        getStyleClass().add("design-toolbar");
        setHgap(4);
        setVgap(4);
        setPadding(new Insets(8));

        addToggleButton(DesignSelectAction.class);
        addToggleButton(DesignRectangleAction.class);
        addToggleButton(DesignCircleAction.class);
        addToggleButton(DesignPointAction.class);
        addToggleButton(DesignTextAction.class);
        addToggleButton(DesignLineAction.class);

        addButton(DesignImportAction.class);
        addButton(DesignClipartAction.class);
        addButton(DesignTraceImageAction.class);
    }

    private void addToggleButton(Class<? extends Action> actionClass) {
        getAction(actionClass).ifPresent(action ->
                getChildren().add(new ToggleActionButton(action, ICON_SIZE, false)));
    }

    private void addButton(Class<? extends Action> actionClass) {
        getAction(actionClass).ifPresent(action ->
                getChildren().add(new ActionButton(action, ICON_SIZE, false)));
    }

    private Optional<Action> getAction(Class<? extends Action> actionClass) {
        return ActionRegistry.getInstance().getAction(actionClass.getCanonicalName());
    }
}
