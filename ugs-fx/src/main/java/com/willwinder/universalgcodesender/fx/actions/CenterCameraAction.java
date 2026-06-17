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

import com.willwinder.universalgcodesender.fx.model.WorkspaceBounds;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;

/**
 * Recenters and fits the visualizer view on the active workspace bounds (gcode extents or the
 * designer drawing). Does nothing when no bounds are available.
 */
public class CenterCameraAction extends BaseAction {

    public static final String ICON = "icons/center-camera.svg";

    public CenterCameraAction() {
        super(null, Localization.getString("platform.visualizer.centerCamera"),
                Localization.getString("actions.category.visualizer"), ICON);
    }

    @Override
    public void handleAction(ActionEvent event) {
        WorkspaceBounds workspaceBounds = WorkspaceManager.getInstance().getActiveWorkspace()
                .flatMap(WorkspaceContext::getBounds)
                .orElse(new WorkspaceBounds(0, 0, 100, 100));
        VisualizerService.getInstance().centerOnBounds(workspaceBounds);
    }
}
