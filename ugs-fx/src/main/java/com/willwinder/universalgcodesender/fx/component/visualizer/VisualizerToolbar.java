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
import com.willwinder.universalgcodesender.fx.actions.ToggleDesignAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleGcodeModelAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleGridAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleMachineVisualizationAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleRulerAction;
import com.willwinder.universalgcodesender.fx.actions.ToggleToolAction;
import com.willwinder.universalgcodesender.fx.control.ToggleActionButton;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import javafx.application.Platform;
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

        ButtonBase designButton = styleButton(new ToggleActionButton(new ToggleDesignAction(), BUTTON_SIZE, false, Color.WHITE));

        getChildren().addAll(
                designButton,
                styleButton(new ToggleActionButton(new ToggleGcodeModelAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleGridAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleAxesAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleRulerAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleToolAction(), BUTTON_SIZE, false, Color.WHITE)),
                styleButton(new ToggleActionButton(new ToggleMachineVisualizationAction(), BUTTON_SIZE, false, Color.WHITE))
        );

        bindDesignButtonVisibility(designButton);
    }

    /**
     * Shows the design toggle only while a {@link UgsdWorkspaceContext} is active. When hidden it is
     * also unmanaged so it takes no space in the toolbar layout.
     */
    private void bindDesignButtonVisibility(ButtonBase designButton) {
        WorkspaceManager workspaceManager = WorkspaceManager.getInstance();
        updateDesignButtonVisibility(designButton, workspaceManager.getActiveWorkspace().orElse(null));

        workspaceManager.addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                Platform.runLater(() -> updateDesignButtonVisibility(designButton, workspace));
            }

            @Override
            public void onWorkspaceClosed() {
                Platform.runLater(() -> updateDesignButtonVisibility(designButton, null));
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                // Visibility only depends on the workspace type, not its dirty state.
            }
        });
    }

    private void updateDesignButtonVisibility(ButtonBase designButton, WorkspaceContext workspace) {
        boolean isDesign = workspace instanceof UgsdWorkspaceContext;
        designButton.setVisible(isDesign);
        designButton.setManaged(isDesign);
    }

    static ButtonBase styleButton(ButtonBase button) {
        button.getStyleClass().add("visualizer-button");
        return button;
    }
}
