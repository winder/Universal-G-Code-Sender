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

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;

/**
 * A small spinner shown while the active design's G-code is being regenerated after an edit, and
 * while the visualizer is still building the toolpath of a loaded program. Hidden once the
 * toolpath on screen is up to date.
 */
public class GcodeRegenerationIndicator extends ProgressIndicator implements WorkspaceManager.WorkspaceListener {
    private static final double SIZE = 32;

    public GcodeRegenerationIndicator() {
        super(INDETERMINATE_PROGRESS);
        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, SIZE);
        setTooltip(new Tooltip("Generating G-code"));
        setVisible(false);
        WorkspaceManager.getInstance().addListener(this);
        WorkspaceManager.getInstance().getActiveWorkspace().ifPresent(this::follow);
    }

    @Override
    public void onWorkspaceOpened(WorkspaceContext workspace) {
        Platform.runLater(() -> follow(workspace));
    }

    @Override
    public void onWorkspaceClosed() {
        Platform.runLater(() -> follow(null));
    }

    @Override
    public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
    }

    private void follow(WorkspaceContext workspace) {
        ReadOnlyBooleanProperty toolpathLoading = VisualizerService.getInstance().toolpathLoadingProperty();
        visibleProperty().unbind();
        if (workspace instanceof UgsdWorkspaceContext design) {
            visibleProperty().bind(design.gcodeBusyProperty().or(toolpathLoading));
        } else {
            visibleProperty().bind(toolpathLoading);
        }
    }
}
