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

import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;

/**
 * Base class for the design toolbar actions that activate a drawing {@link Tool}. The
 * {@link #selectedProperty()} mirrors the currently active tool in the designer
 * {@link Controller}, so a bound toggle button stays highlighted while the tool is active.
 * <p>
 * Selecting a tool mutates the designer model, so — like the context aware edit actions
 * (see {@link AbstractDesignEditAction}) and the FX draw-gesture handlers — it is dispatched on
 * the JavaFX thread that owns the designer model in the FX visualizer. Keeping every model
 * mutation on the same thread keeps them serialized and race free.
 */
public abstract class AbstractDesignToolAction extends BaseAction {

    private final transient Controller controller;
    private final Tool tool;

    protected AbstractDesignToolAction(Tool tool, String title, String icon) {
        super(title, title, Localization.getString("actions.category.designer"), icon);
        this.tool = tool;
        this.controller = ControllerFactory.getController();
        controller.addListener(this::onControllerEvent);
        updateSelected();
    }

    private void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.TOOL_SELECTED) {
            Platform.runLater(this::updateSelected);
        }
    }

    private void updateSelected() {
        selectedProperty().set(controller.getTool() == tool);
    }

    @Override
    public void handleAction(ActionEvent event) {
        // Mutate the designer model on the JavaFX thread so it stays serialized with the draw
        // gesture handlers and the other context aware edit actions
        Platform.runLater(() -> controller.setTool(tool));
    }
}
