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
package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.component.designer.InspectorPane;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/**
 * Provides the tools for the active workspace as content for a side pane: the designer inspector
 * for a design, and nothing yet for a G-code file. Binding a pane to this rather than swapping
 * panes keeps the collapsed state and width across workspaces.
 */
public class WorkspaceTools {
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final InspectorPane inspectorPane = new InspectorPane();

    public WorkspaceTools() {
        WorkspaceManager manager = WorkspaceManager.getInstance();
        showContentFor(manager.getActiveWorkspace().orElse(null));
        manager.addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                Platform.runLater(() -> showContentFor(workspace));
            }

            @Override
            public void onWorkspaceClosed() {
                Platform.runLater(() -> showContentFor(null));
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
            }
        });
    }

    /**
     * The name of the current content.
     */
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    /**
     * The tools for the active workspace, or null when it has none.
     */
    public ReadOnlyObjectProperty<Node> contentProperty() {
        return content;
    }

    private void showContentFor(WorkspaceContext workspace) {
        boolean design = workspace instanceof UgsdWorkspaceContext;
        title.set(Localization.getString(design ? "actions.category.designer" : "platform.window.programPane"));

        // Set the content to null when loaded a gcode program for now.
        content.set(design ? inspectorPane : null);
    }
}
