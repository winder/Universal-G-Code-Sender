package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.universalgcodesender.fx.helper.SplitPaneDividerPersistence;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class InspectorPane extends VBox {
    private final SplitPane parent;
    private final SplitPane inspectorSplit;
    private boolean sectionsDividerPersisted;

    public InspectorPane(SplitPane parent) {
        this.parent = parent;

        ScrollPane entityScroll = new ScrollPane(new EntitySettingsPanel());
        entityScroll.getStyleClass().add("inspector-scroll");
        entityScroll.setFitToWidth(true);

        VBox settingsSection = section("Object properties", entityScroll);
        VBox treeSection = section("Objects", new EntityTreeView());

        // The entity tree is scrollable on its own; a vertical split lets the user resize the
        // space between the settings above and the design tree at the bottom.
        inspectorSplit = new SplitPane(settingsSection, treeSection);
        inspectorSplit.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(inspectorSplit, Priority.ALWAYS);

        getChildren().addAll(new DesignToolbar(), new DesignAlignToolbar(), new DesignOperationToolbar(), inspectorSplit);
        setMinWidth(200);
        SplitPane.setResizableWithParent(this, false);

        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                setDocked(workspace instanceof UgsdWorkspaceContext);
            }

            @Override
            public void onWorkspaceClosed() {
                setDocked(false);
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
            }
        });
    }

    /**
     * Wraps a section's content under a labeled header bar so the stacked inspector
     * sections are visually separated. The content fills the remaining height.
     */
    private static VBox section(String title, Region content) {
        Label header = new Label(title);
        header.getStyleClass().add("inspector-section-header");
        header.setMaxWidth(Double.MAX_VALUE);

        VBox.setVgrow(content, Priority.ALWAYS);
        VBox box = new VBox(header, content);
        box.setMinHeight(0);
        return box;
    }

    private void setDocked(boolean docked) {
        Platform.runLater(() -> {
            boolean shown = parent.getItems().contains(this);
            if (docked && !shown) {
                parent.getItems().add(this);

                if (!sectionsDividerPersisted) {
                    sectionsDividerPersisted = true;
                    Platform.runLater(() -> SplitPaneDividerPersistence.install(
                            inspectorSplit, 0, Settings.getInstance().windowDividerInspectorSectionsProperty()));
                }
            } else if (!docked && shown) {
                parent.getItems().remove(this);
            }
        });
    }
}
