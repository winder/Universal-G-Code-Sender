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

import com.willwinder.universalgcodesender.fx.actions.ToggleAxesAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleGcodeModelAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleGridAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleMachineVisualizationAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleRulerAction;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * A horizontal row of the visualizer feature toggles shown in the top-right corner of the
 * {@link Visualizer}: G-code model, grid, axes, ruler and machine visibility.
 */
public class VisualizerToolbar extends HBox {
    static final int BUTTON_SIZE = 24;
    static final double SPACING = 6;

    public VisualizerToolbar() {
        super(SPACING);
        getStyleClass().add("visualizer-toolbar");
        setPickOnBounds(false);

        getChildren().addAll(
                styleButton(new ToggleActionButton(new ToggleGcodeModelAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleGridAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleAxesAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleRulerAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleMachineVisualizationAction(), BUTTON_SIZE, false, Color.WHITE))
        );
    }

    static ButtonBase styleButton(ButtonBase button) {
        button.getStyleClass().add("visualizer-button");
        return button;
    }
}
