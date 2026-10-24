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

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import javafx.application.Platform;
import javafx.scene.Node;

/**
 * Shows a node only while a design ({@code .ugsd}) workspace is active. The node is also
 * unmanaged while hidden, so it takes no space in its parent's layout.
 */
public final class DesignWorkspaceVisibility {

    private DesignWorkspaceVisibility() {
    }

    public static void bind(Node node) {
        WorkspaceManager manager = WorkspaceManager.getInstance();
        apply(node, manager.getActiveWorkspace().orElse(null));
        manager.addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                Platform.runLater(() -> apply(node, workspace));
            }

            @Override
            public void onWorkspaceClosed() {
                Platform.runLater(() -> apply(node, null));
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
            }
        });
    }

    private static void apply(Node node, WorkspaceContext workspace) {
        boolean design = workspace instanceof UgsdWorkspaceContext;
        node.setVisible(design);
        node.setManaged(design);
    }
}
