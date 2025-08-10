/*
    Copyright 2025 Joacim Breiler

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

import com.willwinder.ugs.nbp.core.actions.HomingAction;
import com.willwinder.ugs.nbp.core.actions.SoftResetAction;
import com.willwinder.ugs.nbp.core.actions.UnlockAction;
import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.ConnectDisconnectAction;
import com.willwinder.universalgcodesender.fx.actions.OpenSettingsAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleMachineVisualizationAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class ToolBarMenu extends VBox {
    public ToolBarMenu() {
        getStylesheets().add(getClass().getResource("/styles/toolbar-button.css").toExternalForm());

        List<Node> children = getChildren();
        createButton(ConnectDisconnectAction.class).ifPresent(children::add);
        children.add(new Separator());
        createButton(UnlockAction.class).ifPresent(children::add);
        createButton(SoftResetAction.class).ifPresent(children::add);
        createButton(HomingAction.class).ifPresent(children::add);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        children.add(spacer);

        createToggleButton(ToggleMachineVisualizationAction.class).ifPresent(children::add);
        children.add(new Separator());
        createButton(OpenSettingsAction.class).ifPresent(children::add);

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(children);

        getChildren().add(toolBar);
    }

    private Optional<Node> createButton(Class<?> actionClass) {
        return ActionRegistry
                .getInstance()
                .getAction(actionClass.getCanonicalName())
                .map(action -> {
                    ActionButton actionButton = new ActionButton(action, ActionButton.SIZE_NORMAL);
                    actionButton.setShowText(false);
                    actionButton.getStyleClass().add("toolbar-button");
                    return actionButton;
                });
    }

    private Optional<Node> createToggleButton(Class<? extends Action> actionClass) {
        return ActionRegistry
                .getInstance()
                .getAction(actionClass.getCanonicalName())
                .map(action -> {
                    ToggleActionButton actionButton = new ToggleActionButton(action, ActionButton.SIZE_NORMAL, false);
                    actionButton.setShowText(false);
                    actionButton.getStyleClass().add("toolbar-button");
                    return actionButton;
                });
    }
}
