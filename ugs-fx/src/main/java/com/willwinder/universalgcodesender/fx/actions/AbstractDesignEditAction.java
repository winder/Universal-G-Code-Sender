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
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;

/**
 * Base class for the context aware design edit actions (undo/redo/copy/paste/delete) that operate
 * directly on the designer {@link Controller}, in the same spirit as {@link AbstractDesignToolAction}.
 * <p>
 * The action body runs on the JavaFX thread, which is the thread that owns the designer model in
 * the FX visualizer: the draw-gesture handlers mutate the drawing on the JavaFX thread and the
 * visualizer rebuilds the scene graph from it on the JavaFX thread. Running the edits on the same
 * thread keeps every mutation and the resulting scene refresh serialized and race free. The JavaFX
 * {@link #enabledProperty()} is updated by the subclasses as the relevant designer state (the
 * current selection or the undo history) changes.
 */
public abstract class AbstractDesignEditAction extends BaseAction {

    protected final transient Controller controller;

    protected AbstractDesignEditAction(String title, String icon) {
        super(title, title, Localization.getString("actions.category.designer"), icon);
        this.controller = ControllerFactory.getController();
    }

    @Override
    public void handleAction(ActionEvent event) {
        // Mutate the designer model on the JavaFX thread so the visualizer refreshes immediately
        Platform.runLater(this::performAction);
    }

    /**
     * Performs the action on the designer model. Always invoked on the JavaFX thread.
     */
    protected abstract void performAction();

    /**
     * Updates the enabled state of the action on the JavaFX thread.
     */
    protected void setEnabledLater(boolean enabled) {
        Platform.runLater(() -> enabledProperty().set(enabled));
    }
}
