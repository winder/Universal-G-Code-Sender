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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.actions.CenterCameraAction;
import com.willwinder.universalgcodesender.fx.actions.DesignToggleSnapAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleProjectionAction;
import com.willwinder.universalgcodesender.fx.component.designer.DesignWorkspaceVisibility;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * A vertical stack of the orientation-related overlay buttons shown beneath the orientation
 * cube in the top-left corner of the {@link VisualizerPane}: the camera projection toggle, the
 * center-on-workspace action and, while a design is open, the snap to grid toggle.
 */
public class OrientationToolbar extends VBox {

    public OrientationToolbar() {
        super(VisualizerToolbar.SPACING);
        getStyleClass().add("visualizer-toolbar");
        setAlignment(Pos.TOP_CENTER);
        setPickOnBounds(false);

        getChildren().addAll(
                VisualizerToolbar.styleButton(
                        new ActionButton(new ToggleProjectionAction(), VisualizerToolbar.BUTTON_SIZE, false)),
                VisualizerToolbar.styleButton(
                        new ActionButton(new CenterCameraAction(), VisualizerToolbar.BUTTON_SIZE, false)));

        // The registered instance, so the button shares its state and shortcut with menus.
        ActionRegistry.getInstance().getAction(DesignToggleSnapAction.class.getCanonicalName())
                .ifPresent(action -> {
                    ToggleActionButton snapButton = new ToggleActionButton(action, VisualizerToolbar.BUTTON_SIZE, false);
                    getChildren().add(VisualizerToolbar.styleButton(snapButton));
                    DesignWorkspaceVisibility.bind(snapButton);
                });
    }
}
